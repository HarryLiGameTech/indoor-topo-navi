"""Tests for import_toposcript_project.py. Run with: python3 -m unittest discover -s tools"""

import io
import json
import os
import sys
import tempfile
import threading
import unittest
import uuid
from contextlib import redirect_stderr, redirect_stdout
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parent))
import import_toposcript_project as importer  # noqa: E402

TOKEN = "unit-test-service-token-0123456789-abcdef"


class FakeProjectApi(BaseHTTPRequestHandler):
    """In-memory stand-in for the project API, including its Bearer check."""
    projects: dict = {}
    corrupt_reads = False

    def log_message(self, *args):
        pass

    def _send(self, status, payload=None):
        body = b"" if payload is None else json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _authorized(self):
        if self.headers.get("Authorization") != "Bearer " + TOKEN:
            self._send(401, {"error": {"code": "AUTHENTICATION_REQUIRED", "message": "Authentication is required.", "details": {}}})
            return False
        return True

    def _body(self):
        return json.loads(self.rfile.read(int(self.headers["Content-Length"])).decode("utf-8"))

    def _summary(self, project):
        return {key: project[key] for key in ("id", "buildingId", "name")}

    def do_GET(self):
        if not self._authorized():
            return
        if self.path.startswith("/api/v1/projects/by-building-id/"):
            building_id = self.path.rsplit("/", 1)[1]
            match = [p for p in self.projects.values() if p["buildingId"] == building_id]
            return self._send(200, {"status": "success", "project": match[0]}) if match else self._send(
                404, {"error": {"code": "PROJECT_NOT_FOUND", "message": "", "details": {}}})
        if self.path.startswith("/api/v1/projects?"):
            summaries = sorted((self._summary(p) for p in self.projects.values()), key=lambda p: p["buildingId"])
            return self._send(200, {"status": "success", "projects": summaries, "page": 0, "size": 200, "total": len(summaries)})
        project = self.projects.get(self.path.rsplit("/", 1)[1])
        if project is None:
            return self._send(404, {"error": {"code": "PROJECT_NOT_FOUND", "message": "", "details": {}}})
        if self.corrupt_reads:
            project = {**project, "files": {**project["files"], "extra": ""}}
        self._send(200, {"status": "success", "project": project})

    def do_POST(self):
        if not self._authorized():
            return
        body = self._body()
        if any(p["buildingId"].lower() == body["buildingId"].lower() for p in self.projects.values()):
            return self._send(409, {"error": {"code": "BUILDING_ID_CONFLICT", "message": "", "details": {"path": "/buildingId"}}})
        project = {"id": str(uuid.uuid4()), **body}
        self.projects[project["id"]] = project
        self._send(201, {"status": "success", "project": self._summary(project)})

    def do_PUT(self):
        if not self._authorized():
            return
        project = self.projects[self.path.rsplit("/", 1)[1]]
        project.update(self._body())
        self._send(200, {"status": "success", "project": self._summary(project)})


class ImporterTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name) / "LobbyDemo"
        (self.root / "floors").mkdir(parents=True)
        self.write("configuration", b"building-includes {\r\n submap Lobby\r\n}\r\n")
        self.write("Lobby.tmap", "﻿topo-map Lobby() {\n topo-node 入口  \n}\n".encode("utf-8"))
        self.write("floors/Upper", b"topo-map Upper() {\n")  # Incomplete sources are importable.
        self.write("README.md", b"# notes")
        self.write("map.json", b"{}")
        self.write(".DS_Store", b"\x00\x01")

    def tearDown(self):
        self.temporary.cleanup()

    def write(self, relative, data):
        path = self.root / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)

    def run_main(self, *arguments, token=TOKEN):
        stdout, stderr = io.StringIO(), io.StringIO()
        environment = {importer.TOKEN_ENVIRONMENT: token} if token is not None else {}
        with mock.patch.dict(os.environ, environment, clear=True), redirect_stdout(stdout), redirect_stderr(stderr):
            status = importer.main(list(arguments))
        return status, stdout.getvalue(), stderr.getvalue()

    def test_reads_sources_exactly_and_reports_skipped_files(self):
        manifest = importer.read_sources(self.root, 512, 4 * 1024 * 1024)
        self.assertEqual(sorted(manifest.files), ["Lobby.tmap", "configuration", "floors/Upper"])
        self.assertEqual(manifest.files["configuration"], "building-includes {\r\n submap Lobby\r\n}\r\n")
        self.assertTrue(manifest.files["Lobby.tmap"].startswith("﻿"))
        self.assertEqual(sorted(manifest.skipped), [".DS_Store", "README.md", "map.json"])
        self.assertEqual(manifest.digest(), importer.read_sources(self.root, 512, 4 * 1024 * 1024).digest())

    def test_rejects_symlinks_invalid_utf8_and_limits(self):
        os.symlink(self.root / "configuration", self.root / "Linked.tmap")
        with self.assertRaisesRegex(importer.ImportError_, "symbolic-linked source"):
            importer.read_sources(self.root, 512, 4 * 1024 * 1024)
        os.remove(self.root / "Linked.tmap")
        os.symlink(self.root / "floors", self.root / "linked-dir")
        with self.assertRaisesRegex(importer.ImportError_, "symbolic-linked directories"):
            importer.read_sources(self.root, 512, 4 * 1024 * 1024)
        os.remove(self.root / "linked-dir")
        link = Path(self.temporary.name) / "root-link"
        os.symlink(self.root, link)
        with self.assertRaisesRegex(importer.ImportError_, "input root"):
            importer.read_sources(link, 512, 4 * 1024 * 1024)
        with self.assertRaisesRegex(importer.ImportError_, "source files"):
            importer.read_sources(self.root, 2, 4 * 1024 * 1024)
        with self.assertRaisesRegex(importer.ImportError_, "exceeds"):
            importer.read_sources(self.root, 512, 10)
        self.write("Bad.tmap", b"topo-map Bad() {\xff}")
        with self.assertRaisesRegex(importer.ImportError_, "not valid UTF-8"):
            importer.read_sources(self.root, 512, 4 * 1024 * 1024)

    def test_path_and_identity_rules_match_the_server(self):
        for valid in ["configuration", "configuration.tcfg", "a/b.tmap", "x.ttr", "Ünïcode 层"]:
            self.assertIsNone(importer.path_problem(valid), valid)
        for invalid in ["", "/abs", "dir/", "a\\b", "a//b", "./a", "a/../b", "C:root", "file:x", "a\x01b", "map.json",
                        "Lobby.TMAP", "x" * 513]:
            self.assertIsNotNone(importer.path_problem(invalid), invalid)
        for building_id in ["", "-x", "has space", "ü", "a" * 256]:
            with self.assertRaises(importer.ImportError_):
                importer.validate_identity(building_id, "Name")
        with self.assertRaises(importer.ImportError_):
            importer.validate_identity("ok", "   ")
        importer.validate_identity("indigoBJ", "Indigo")

    def test_dry_run_needs_no_token_or_server(self):
        status, stdout, stderr = self.run_main("--directory", str(self.root), "--building-id", "LobbyDemo", "--dry-run", token=None)
        self.assertEqual(status, 0, stderr)
        record = json.loads(stdout)
        self.assertEqual((record["action"], record["fileCount"]), ("dry-run", 3))
        self.assertIn("skipped non-source file: README.md", stderr)
        self.assertNotIn("topo-map", stdout + stderr)

    def test_creates_replaces_and_verifies_read_back(self):
        FakeProjectApi.projects = {}
        FakeProjectApi.corrupt_reads = False
        server = ThreadingHTTPServer(("127.0.0.1", 0), FakeProjectApi)
        threading.Thread(target=server.serve_forever, daemon=True).start()
        base = f"http://127.0.0.1:{server.server_port}"
        manifest_file = Path(self.temporary.name) / "imports.jsonl"
        try:
            common = ("--base-url", base, "--directory", str(self.root), "--manifest", str(manifest_file))
            status, stdout, stderr = self.run_main(*common, "--building-id", "LobbyDemo", "--name", "Lobby")
            self.assertEqual(status, 0, stderr)
            created = json.loads(stdout)
            self.assertEqual(created["action"], "created")
            self.assertNotIn(TOKEN, stdout + stderr)
            self.assertNotIn("topo-map", stdout + stderr)

            status, _, stderr = self.run_main(*common, "--building-id", "LobbyDemo")
            self.assertEqual(status, 1)
            self.assertIn("--replace", stderr)
            status, _, stderr = self.run_main(*common, "--building-id", "lobbydemo")
            self.assertEqual(status, 1)
            self.assertIn("differs only by case", stderr)
            status, _, stderr = self.run_main(*common, "--building-id", "Absent", "--replace")
            self.assertEqual(status, 1)

            self.write("Lobby.tmap", b"topo-map Lobby() {\n}\n")
            status, stdout, stderr = self.run_main(*common, "--building-id", "LobbyDemo", "--name", "Lobby", "--replace")
            self.assertEqual(status, 0, stderr)
            replaced = json.loads(stdout)
            self.assertEqual((replaced["action"], replaced["id"]), ("replaced", created["id"]))
            self.assertNotEqual(replaced["digest"], created["digest"])
            self.assertEqual(len(manifest_file.read_text(encoding="utf-8").splitlines()), 2)

            FakeProjectApi.corrupt_reads = True
            status, _, stderr = self.run_main(*common, "--building-id", "LobbyDemo", "--name", "Lobby", "--replace")
            self.assertEqual(status, 1)
            self.assertIn("read-back path mismatch", stderr)

            status, _, stderr = self.run_main(*common, "--building-id", "Other", token="wrong-token-0123456789-0123456789")
            self.assertEqual(status, 1)
            self.assertIn("AUTHENTICATION_REQUIRED", stderr)
            status, _, stderr = self.run_main(*common, "--building-id", "Other", token="")
            self.assertEqual(status, 1)
            self.assertIn(importer.TOKEN_ENVIRONMENT, stderr)
        finally:
            server.shutdown()
            server.server_close()


if __name__ == "__main__":
    unittest.main()
