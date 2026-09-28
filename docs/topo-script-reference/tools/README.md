# Documentation tools

The language-reference search index is generated from the HTML pages in this directory tree.

After adding or changing reference content, rebuild the index from the repository root:

```sh
node docs/topo-script-reference/tools/build-search-index.mjs
```

Commit the regenerated `assets/js/search-index.js` together with the documentation changes. The published reference does not require Node.js or a server-side search service; search runs entirely in the reader's browser.
