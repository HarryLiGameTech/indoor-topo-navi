import { readFile, readdir, writeFile } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const referenceRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const outputPath = path.join(referenceRoot, "assets", "js", "search-index.js");

function decodeEntities(value) {
  const named = {
    amp: "&",
    apos: "'",
    gt: ">",
    lt: "<",
    nbsp: " ",
    quot: '"'
  };
  return value
    .replace(/&#x([0-9a-f]+);/gi, (_, digits) => String.fromCodePoint(Number.parseInt(digits, 16)))
    .replace(/&#([0-9]+);/g, (_, digits) => String.fromCodePoint(Number.parseInt(digits, 10)))
    .replace(/&([a-z]+);/gi, (match, name) => named[name.toLowerCase()] ?? match);
}

function plainText(value) {
  return decodeEntities(value)
    .replace(/<script\b[\s\S]*?<\/script>/gi, " ")
    .replace(/<style\b[\s\S]*?<\/style>/gi, " ")
    .replace(/<[^>]+>/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function attributeValue(attributes, name) {
  const match = attributes.match(new RegExp("\\b" + name + "=[\"']([^\"']+)[\"']", "i"));
  return match ? match[1] : null;
}

async function htmlFiles(directory) {
  const entries = await readdir(directory, { withFileTypes: true });
  const files = [];
  for (const entry of entries) {
    if (entry.name === "assets" || entry.name === "tools") continue;
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...await htmlFiles(absolute));
    else if (entry.isFile() && entry.name.endsWith(".html") && entry.name !== "index.html") files.push(absolute);
  }
  return files;
}

function entriesFor(html, href) {
  const h1 = html.match(/<h1\b[^>]*>([\s\S]*?)<\/h1>/i);
  const page = h1 ? plainText(h1[1]) : href;
  const entries = [{
    page,
    title: page,
    href,
    text: plainText((html.match(/<header\b[^>]*>([\s\S]*?)<\/header>/i) || ["", ""])[1])
  }];

  const headings = [];
  const expression = /<(h2|h3|dt)\b([^>]*)>([\s\S]*?)<\/\1>/gi;
  let match;
  while ((match = expression.exec(html)) !== null) {
    let id = attributeValue(match[2], "id");
    if (!id) {
      const preceding = html.slice(0, match.index);
      const sections = Array.from(preceding.matchAll(/<section\b([^>]*)>/gi));
      const nearest = sections.at(-1);
      id = nearest ? attributeValue(nearest[1], "id") : null;
    }
    if (!id) continue;
    headings.push({
      index: match.index,
      end: expression.lastIndex,
      id,
      title: plainText(match[3])
    });
  }

  headings.forEach((heading, index) => {
    const end = index + 1 < headings.length ? headings[index + 1].index : html.length;
    entries.push({
      page,
      title: heading.title,
      href: href + "#" + heading.id,
      text: plainText(html.slice(heading.end, end)).slice(0, 2400)
    });
  });

  const seen = new Set();
  return entries.filter((entry) => {
    const key = entry.href + "\u0000" + entry.title;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

const files = (await htmlFiles(referenceRoot)).sort();
const index = [];
for (const file of files) {
  const href = path.relative(referenceRoot, file).split(path.sep).join("/");
  index.push(...entriesFor(await readFile(file, "utf8"), href));
}

await writeFile(
  outputPath,
  "window.TOPO_DOC_SEARCH_INDEX = " + JSON.stringify(index) + ";\n",
  "utf8"
);
console.log("Wrote " + index.length + " search entries to " + path.relative(referenceRoot, outputPath));
