#!/usr/bin/env python3
"""Import one native TopoScript source directory into a TopoNavi project store through its HTTP API.

This is an explicit operator/developer utility. It is not a runtime data provider: the TopoNavi service never
scans directories, seeds data, or falls back to a mounted source tree.

The service credential is read from the TOPONAVI_SERVICE_TOKEN environment variable and is never accepted as an
argument or printed. File contents are never printed either.

Examples:
    python3 tools/import_toposcript_project.py --directory examples/swfc --building-id swfc \\
        --name "Shanghai World Financial Center" --dry-run
    TOPONAVI_SERVICE_TOKEN=... python3 tools/import_toposcript_project.py --base-url http://127.0.0.1:8080 \\
        --directory examples/swfc --building-id swfc --name "Shanghai World Financial Center"
    TOPONAVI_SERVICE_TOKEN=... python3 tools/import_toposcript_project.py ... --replace --manifest imports.jsonl

Exit status: 0 on success, 1 on a validation, HTTP, or read-back failure, 2 on invalid usage.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import stat
import sys
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path

TOKEN_ENVIRONMENT = "TOPONAVI_SERVICE_TOKEN"
BUILDING_ID = re.compile(r"[A-Za-z0-9][A-Za-z0-9._-]{0,254}")
DRIVE_OR_URI_PREFIX = re.compile(r"^[A-Za-z][A-Za-z0-9+.-]*:")
SOURCE_SUFFIXES = (".tmap", ".ttr", ".tcfg")
DEFAULT_MAX_FILES = 512
DEFAULT_MAX_SOURCE_BYTES = 4 * 1024 * 1024
MAX_PATH_BYTES = 512
MAX_NAME_CHARACTERS = 255
PAGE_SIZE = 200


class ImportError_(Exception):
    """A reportable import failure (named to avoid shadowing the builtin ImportError)."""


@dataclass
class Manifest:
    files: dict[str, str] = field(default_factory=dict)
    sizes: dict[str, int] = field(default_factory=dict)
    skipped: list[str] = field(default_factory=list)

    @property
    def source_bytes(self) -> int:
        return sum(self.sizes.values())

    def digest(self) -> str:
        return content_digest(self.files)


def content_digest(files: dict[str, str]) -> str:
    """Deterministic SHA-256 over sorted paths and exact contents, each length-prefixed."""
    digest = hashlib.sha256()
    for path in sorted(files):
        for part in (path, files[path]):
            data = part.encode("utf-8")
            digest.update(len(data).to_bytes(4, "big"))
            digest.update(data)
    return digest.hexdigest()


def is_source_name(basename: str) -> bool:
    return "." not in basename or basename.endswith(SOURCE_SUFFIXES)


def text_problem(value: str) -> str | None:
    if "\0" in value:
        return "contains a NUL character"
    if any(0xD800 <= ord(character) <= 0xDFFF for character in value):
        return "is not valid Unicode text"
    return None


def path_problem(path: str) -> str | None:
    """Mirrors the server's storage path rules; the server remains authoritative."""
    size = len(path.encode("utf-8", "surrogatepass"))
    if not 1 <= size <= MAX_PATH_BYTES:
        return "must be 1 to 512 UTF-8 bytes"
    if text_problem(path):
        return "must be valid Unicode text without NUL characters"
    if any(ord(c) < 0x20 or 0x7F <= ord(c) <= 0x9F for c in path):
        return "must not contain control characters"
    if "\\" in path:
        return "must use '/' as the separator"
    if path.startswith("/") or path.endswith("/"):
        return "must be a relative file path"
    if DRIVE_OR_URI_PREFIX.match(path):
        return "must not start with a drive or URI prefix"
    segments = path.split("/")
    if any(segment in ("", ".", "..") for segment in segments):
        return "must not contain empty, '.' or '..' segments"
    if not is_source_name(segments[-1]):
        return "must be an extensionless source or end in .tmap, .ttr, or .tcfg"
    return None


def validate_identity(building_id: str, name: str) -> None:
    if not BUILDING_ID.fullmatch(building_id):
        raise ImportError_("--building-id must be 1-255 ASCII letters, digits, '.', '_' or '-', starting with a letter or digit")
    if not name.strip():
        raise ImportError_("--name must not be blank")
    if len(name) > MAX_NAME_CHARACTERS:
        raise ImportError_("--name must be at most 255 characters")
    if text_problem(name):
        raise ImportError_("--name " + text_problem(name))


def read_sources(directory: Path, max_files: int, max_source_bytes: int) -> Manifest:
    """Recursively reads accepted source files without following symlinks or converting newlines."""
    if directory.is_symlink():
        raise ImportError_(f"{directory}: the input root must not be a symbolic link")
    if not directory.is_dir():
        raise ImportError_(f"{directory}: not a directory")
    manifest = Manifest()
    for current, directories, filenames in os.walk(directory, followlinks=False):
        current_path = Path(current)
        for name in sorted(directories):
            if (current_path / name).is_symlink():
                raise ImportError_(f"{(current_path / name).relative_to(directory).as_posix()}: symbolic-linked directories are not imported")
        directories.sort()
        for name in sorted(filenames):
            file = current_path / name
            relative = file.relative_to(directory).as_posix()
            mode = os.lstat(file).st_mode
            if not is_source_name(name):
                manifest.skipped.append(relative)
                continue
            if stat.S_ISLNK(mode):
                raise ImportError_(f"{relative}: symbolic-linked source files are not imported")
            if not stat.S_ISREG(mode):
                manifest.skipped.append(relative)
                continue
            problem = path_problem(relative)
            if problem:
                raise ImportError_(f"{relative}: path {problem}")
            data = file.read_bytes()
            try:
                content = data.decode("utf-8")  # Strict; keeps a BOM as U+FEFF and every line ending as-is.
            except UnicodeDecodeError as error:
                raise ImportError_(f"{relative}: not valid UTF-8 ({error.reason} at byte {error.start})") from None
            problem = text_problem(content)
            if problem:
                raise ImportError_(f"{relative}: source text {problem}")
            manifest.files[relative] = content
            manifest.sizes[relative] = len(data)
            if len(manifest.files) > max_files:
                raise ImportError_(f"more than {max_files} source files (configured project limit)")
            if manifest.source_bytes > max_source_bytes:
                raise ImportError_(f"source text exceeds {max_source_bytes} bytes (configured project limit)")
    return manifest


class Api:
    def __init__(self, base_url: str, token: str, timeout: float):
        self.base_url = base_url.rstrip("/")
        self.token = token
        self.timeout = timeout

    def request(self, method: str, path: str, body: object | None = None) -> tuple[int, dict]:
        data = None if body is None else json.dumps(body, ensure_ascii=False).encode("utf-8")
        request = urllib.request.Request(self.base_url + path, data=data, method=method)
        request.add_header("Authorization", "Bearer " + self.token)
        request.add_header("Accept", "application/json")
        if data is not None:
            request.add_header("Content-Type", "application/json")
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                return response.status, json.loads(response.read().decode("utf-8") or "{}")
        except urllib.error.HTTPError as error:
            try:
                payload = json.loads(error.read().decode("utf-8") or "{}")
            except ValueError:
                payload = {}
            return error.code, payload
        except urllib.error.URLError as error:
            raise ImportError_(f"cannot reach {self.base_url}: {error.reason}") from None

    def expect(self, expected: int, method: str, path: str, body: object | None = None) -> dict:
        status, payload = self.request(method, path, body)
        if status != expected:
            raise ImportError_(describe_failure(method, path, status, payload))
        return payload


def describe_failure(method: str, path: str, status: int, payload: dict) -> str:
    error = payload.get("error", {}) if isinstance(payload, dict) else {}
    detail = f"{error.get('code', 'HTTP_ERROR')}: {error.get('message', '')}".strip()
    pointer = (error.get("details") or {}).get("path") if isinstance(error.get("details"), dict) else None
    return f"{method} {path} returned {status} ({detail}{', at ' + pointer if pointer else ''})"


def find_case_collision(api: Api, building_id: str) -> str | None:
    """The store rejects IDs that differ only by case; report that before attempting a create."""
    page = 0
    while True:
        payload = api.expect(200, "GET", f"/api/v1/projects?page={page}&size={PAGE_SIZE}")
        for project in payload.get("projects", []):
            existing = project.get("buildingId", "")
            if existing != building_id and existing.lower() == building_id.lower():
                return existing
        if (page + 1) * PAGE_SIZE >= payload.get("total", 0):
            return None
        page += 1


def import_project(api: Api, manifest: Manifest, building_id: str, name: str, replace: bool) -> dict:
    quoted = urllib.parse.quote(building_id, safe="")
    status, payload = api.request("GET", f"/api/v1/projects/by-building-id/{quoted}")
    if status == 200:
        if not replace:
            raise ImportError_(f"building ID '{building_id}' already exists; pass --replace to replace its name and complete source set")
        project_id = payload["project"]["id"]
        api.expect(200, "PUT", f"/api/v1/projects/{project_id}", {"name": name, "files": manifest.files})
        action = "replaced"
    elif status == 404:
        if replace:
            raise ImportError_(f"--replace was requested, but no project has building ID '{building_id}'")
        collision = find_case_collision(api, building_id)
        if collision:
            raise ImportError_(f"building ID '{building_id}' differs only by case from existing project '{collision}'")
        created = api.expect(201, "POST", "/api/v1/projects", {"buildingId": building_id, "name": name, "files": manifest.files})
        project_id = created["project"]["id"]
        action = "created"
    else:
        raise ImportError_(describe_failure("GET", f"/api/v1/projects/by-building-id/{quoted}", status, payload))

    stored = api.expect(200, "GET", f"/api/v1/projects/{project_id}")["project"]
    if stored.get("buildingId") != building_id or stored.get("name") != name:
        raise ImportError_(f"read-back metadata mismatch for project {project_id}")
    stored_files = stored.get("files", {})
    if set(stored_files) != set(manifest.files):
        missing = sorted(set(manifest.files) - set(stored_files))
        extra = sorted(set(stored_files) - set(manifest.files))
        raise ImportError_(f"read-back path mismatch for project {project_id}: missing {missing}, unexpected {extra}")
    differing = sorted(path for path in manifest.files if stored_files[path] != manifest.files[path])
    if differing:
        raise ImportError_(f"read-back content mismatch for project {project_id}: {differing}")
    return {"action": action, "id": project_id}


def parse_arguments(argv: list[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    parser.add_argument("--base-url", default="http://127.0.0.1:8080", help="TopoNavi service URL (default: %(default)s)")
    parser.add_argument("--directory", required=True, type=Path, help="native TopoScript source directory to import")
    parser.add_argument("--building-id", required=True, help="exact stable building ID, e.g. swfc (never renamed)")
    parser.add_argument("--name", help="display name (default: the building ID)")
    parser.add_argument("--replace", action="store_true", help="fully replace an existing project with this building ID")
    parser.add_argument("--dry-run", action="store_true", help="validate locally and report the manifest without HTTP")
    parser.add_argument("--manifest", type=Path, help="append the successful import record to this JSON Lines file")
    parser.add_argument("--max-files", type=int, default=DEFAULT_MAX_FILES, help="deployment's platform.projects.max-files")
    parser.add_argument("--max-source-bytes", type=int, default=DEFAULT_MAX_SOURCE_BYTES,
                        help="deployment's platform.projects.max-source-bytes")
    parser.add_argument("--timeout", type=float, default=60.0, help="HTTP timeout in seconds (default: %(default)s)")
    return parser.parse_args(argv)


def main(argv: list[str] | None = None) -> int:
    arguments = parse_arguments(sys.argv[1:] if argv is None else argv)
    name = arguments.name if arguments.name is not None else arguments.building_id
    try:
        validate_identity(arguments.building_id, name)
        manifest = read_sources(arguments.directory, arguments.max_files, arguments.max_source_bytes)
        record = {
            "buildingId": arguments.building_id,
            "name": name,
            "fileCount": len(manifest.files),
            "sourceBytes": manifest.source_bytes,
            "digest": manifest.digest(),
        }
        for skipped in manifest.skipped:
            print(f"skipped non-source file: {skipped}", file=sys.stderr)
        if arguments.dry_run:
            for path in sorted(manifest.files):
                print(f"{manifest.sizes[path]:>10}  {path}", file=sys.stderr)
            print(json.dumps({"action": "dry-run", **record}, ensure_ascii=False))
            return 0
        token = os.environ.get(TOKEN_ENVIRONMENT, "")
        if not token.strip():
            raise ImportError_(f"set {TOKEN_ENVIRONMENT} to the deployment's service credential")
        result = import_project(Api(arguments.base_url, token, arguments.timeout), manifest, arguments.building_id, name,
                                arguments.replace)
        record = {"action": result["action"], "id": result["id"], **record}
        line = json.dumps(record, ensure_ascii=False)
        if arguments.manifest:
            with arguments.manifest.open("a", encoding="utf-8") as output:
                output.write(line + "\n")
        print(line)
        return 0
    except ImportError_ as error:
        print(f"import failed: {error}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
