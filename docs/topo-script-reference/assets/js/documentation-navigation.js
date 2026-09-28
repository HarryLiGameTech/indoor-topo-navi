(function () {
  "use strict";

  const groups = [
    {
      title: "Language",
      pages: [
        ["Language and Execution Model", "language/language-and-execution-model.html"],
        ["Lexical Structure", "language/lexical-structure.html"]
      ]
    },
    {
      title: "Building",
      pages: [
        ["Whole-Building Composition", "building/building-includes.html"],
        ["Global Declarations", "building/global-declarations.html"]
      ]
    },
    {
      title: "Topology",
      pages: [
        ["Topo Maps", "topology/topomap.html"],
        ["Nodes", "topology/nodes.html"],
        ["Paths and Local Movement", "topology/paths.html"],
        ["Map Selection and Reuse", "topology/submaps.html"]
      ]
    },
    {
      title: "Transport",
      pages: [
        ["Cross-Map Transportation", "transport/transport.html"],
        ["Elevators", "transport/elevators.html"],
        ["Escalators", "transport/escalators.html"],
        ["Stairs", "transport/stairs.html"]
      ]
    },
    {
      title: "Routing",
      pages: [
        ["Routing Semantics", "routing/routing-semantics.html"],
        ["Constraints and Parameters", "routing/constraints-and-parameters.html"],
        ["Modes and Preferences", "routing/preferences-and-modes.html"],
        ["Compiled and Runtime Outputs", "routing/route-results.html"]
      ]
    },
    {
      title: "Validation",
      pages: [
        ["Validation and Error Reporting", "validation/validation-and-error-reporting.html"]
      ]
    },
    {
      title: "Examples",
      pages: [
        ["Feature-by-Feature Tutorials", "examples/feature-by-feature-tutorials.html"],
        ["SkyrimTower", "examples/skyrim-tower.html"]
      ]
    },
    {
      title: "Reference",
      pages: [
        ["Glossary", "glossary.html"]
      ]
    }
  ];

  const scriptUrl = document.currentScript && document.currentScript.src
    ? new URL(document.currentScript.src)
    : new URL("assets/js/documentation-navigation.js", document.location.href);
  const rootUrl = new URL("../../", scriptUrl);
  const currentUrl = new URL(document.location.href);

  function sameDocument(target) {
    const normalizedTarget = decodeURIComponent(target.pathname).replace(/\/+$/, "");
    const normalizedCurrent = decodeURIComponent(currentUrl.pathname).replace(/\/+$/, "");
    return normalizedTarget === normalizedCurrent;
  }

  function element(name, className, text) {
    const node = document.createElement(name);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
  }

  function buildHeader() {
    const header = element("header", "docs-site-header");
    const menuButton = element("button", "docs-menu-toggle", "Contents");
    menuButton.type = "button";
    menuButton.setAttribute("aria-expanded", "false");
    menuButton.setAttribute("aria-controls", "docs-sidebar");

    const brand = element("a", "docs-site-name");
    brand.href = new URL("language/language-and-execution-model.html", rootUrl).href;
    brand.append(document.createTextNode("TopoScript"), element("span", "", "Language Reference"));

    const actions = element("div", "docs-header-actions");
    const searchButton = element("button", "docs-search-trigger");
    searchButton.type = "button";
    searchButton.setAttribute("aria-haspopup", "dialog");
    searchButton.append(document.createTextNode("Search documentation"), element("kbd", "", "/"));
    actions.append(searchButton);
    header.append(menuButton, brand, actions);

    menuButton.addEventListener("click", function () {
      const open = document.body.classList.toggle("docs-sidebar-open");
      menuButton.setAttribute("aria-expanded", String(open));
    });

    return { header, menuButton, searchButton };
  }

  function buildSidebar(menuButton) {
    const aside = element("aside", "docs-sidebar");
    aside.id = "docs-sidebar";
    const nav = element("nav");
    nav.setAttribute("aria-label", "TopoScript reference");

    groups.forEach(function (group) {
      const section = element("section", "docs-sidebar-group");
      section.append(element("h2", "docs-sidebar-title", group.title));
      const list = element("ul");

      group.pages.forEach(function (page) {
        const target = new URL(page[1], rootUrl);
        const item = element("li");
        const link = element("a", "", page[0]);
        link.href = target.href;
        if (sameDocument(target)) link.setAttribute("aria-current", "page");
        link.addEventListener("click", function () {
          document.body.classList.remove("docs-sidebar-open");
          menuButton.setAttribute("aria-expanded", "false");
        });
        item.append(link);
        list.append(item);
      });

      section.append(list);
      nav.append(section);
    });

    aside.append(nav);
    return aside;
  }

  function buildPageToc(article) {
    const sections = Array.from(article.querySelectorAll(":scope > section[id]"))
      .map(function (section) {
        const heading = section.querySelector(":scope > h2");
        return heading ? { id: section.id, title: heading.textContent.trim() } : null;
      })
      .filter(Boolean);

    if (sections.length < 2) return null;

    const aside = element("aside", "docs-page-toc");
    const nav = element("nav");
    nav.setAttribute("aria-label", "On this page");
    nav.append(element("p", "docs-page-toc-title", "On this page"));
    const list = element("ul");

    sections.forEach(function (section) {
      const item = element("li");
      const link = element("a", "", section.title);
      link.href = "#" + section.id;
      item.append(link);
      list.append(item);
    });

    nav.append(list);
    aside.append(nav);
    return aside;
  }

  function normalized(value) {
    return value.toLocaleLowerCase().normalize("NFKD");
  }

  function scoreEntry(entry, tokens, query) {
    const title = normalized(entry.title);
    const page = normalized(entry.page);
    const text = normalized(entry.text);
    let score = 0;

    if (title === query) score += 120;
    if (title.startsWith(query)) score += 55;
    if (title.includes(query)) score += 35;
    if (page.includes(query)) score += 18;

    for (const token of tokens) {
      if (!title.includes(token) && !page.includes(token) && !text.includes(token)) return -1;
      if (title === token) score += 35;
      else if (title.startsWith(token)) score += 22;
      else if (title.includes(token)) score += 14;
      if (page.includes(token)) score += 7;
      const occurrences = text.split(token).length - 1;
      score += Math.min(occurrences, 6);
    }

    return score;
  }

  function excerptFor(entry, tokens) {
    const raw = entry.text || "";
    const lower = normalized(raw);
    const positions = tokens
      .map(function (token) { return lower.indexOf(token); })
      .filter(function (position) { return position >= 0; });
    const start = positions.length ? Math.max(0, Math.min.apply(null, positions) - 70) : 0;
    const end = Math.min(raw.length, start + 220);
    return (start > 0 ? "…" : "") + raw.slice(start, end).trim() + (end < raw.length ? "…" : "");
  }

  function highlightedFragment(text, tokens) {
    const fragment = document.createDocumentFragment();
    if (!tokens.length) {
      fragment.append(document.createTextNode(text));
      return fragment;
    }

    const escaped = tokens
      .map(function (token) { return token.replace(/[.*+?^$\{\}()|[\]\\]/g, "\\$&"); })
      .sort(function (a, b) { return b.length - a.length; });
    const expression = new RegExp("(" + escaped.join("|") + ")", "gi");
    let last = 0;
    let match;
    while ((match = expression.exec(text)) !== null) {
      fragment.append(document.createTextNode(text.slice(last, match.index)));
      const mark = document.createElement("mark");
      mark.textContent = match[0];
      fragment.append(mark);
      last = match.index + match[0].length;
    }
    fragment.append(document.createTextNode(text.slice(last)));
    return fragment;
  }

  function buildSearch(searchButton) {
    const dialog = element("dialog", "docs-search-dialog");
    dialog.setAttribute("aria-label", "Search TopoScript documentation");
    const form = element("form", "docs-search-form");
    form.method = "dialog";
    const input = element("input", "docs-search-input");
    input.type = "search";
    input.placeholder = "Search language terms, declarations, and behavior";
    input.autocomplete = "off";
    input.setAttribute("aria-label", "Search documentation");
    const close = element("button", "docs-search-close", "Close");
    close.type = "button";
    const status = element("p", "docs-search-status", "Type at least two characters.");
    status.setAttribute("aria-live", "polite");
    const results = element("ol", "docs-search-results");
    form.append(input, close);
    dialog.append(form, status, results);
    document.body.append(dialog);

    function open() {
      if (typeof dialog.showModal === "function") dialog.showModal();
      else dialog.setAttribute("open", "");
      window.setTimeout(function () { input.focus(); }, 0);
    }

    function closeDialog() {
      if (typeof dialog.close === "function") dialog.close();
      else dialog.removeAttribute("open");
      searchButton.focus();
    }

    function render() {
      const query = normalized(input.value.trim());
      const tokens = query.split(/\s+/).filter(function (token) { return token.length > 1; });
      results.replaceChildren();

      if (query.length < 2 || !tokens.length) {
        status.textContent = "Type at least two characters.";
        return;
      }

      const index = Array.isArray(window.TOPO_DOC_SEARCH_INDEX)
        ? window.TOPO_DOC_SEARCH_INDEX
        : [];
      const matches = index
        .map(function (entry) {
          return { entry, score: scoreEntry(entry, tokens, query) };
        })
        .filter(function (item) { return item.score >= 0; })
        .sort(function (a, b) {
          return b.score - a.score || a.entry.title.localeCompare(b.entry.title);
        })
        .slice(0, 15);

      status.textContent = matches.length
        ? matches.length + (matches.length === 1 ? " result" : " results")
        : "No matching documentation found.";

      matches.forEach(function (match) {
        const item = element("li", "docs-search-result");
        const link = element("a");
        link.href = new URL(match.entry.href, rootUrl).href;
        const title = element("span", "docs-search-result-title");
        title.append(highlightedFragment(match.entry.title, tokens));
        const page = element("span", "docs-search-result-page", match.entry.page);
        const excerpt = element("span", "docs-search-result-excerpt");
        excerpt.append(highlightedFragment(excerptFor(match.entry, tokens), tokens));
        link.append(title, page, excerpt);
        item.append(link);
        results.append(item);
      });
    }

    searchButton.addEventListener("click", open);
    close.addEventListener("click", closeDialog);
    input.addEventListener("input", render);
    form.addEventListener("submit", function (event) {
      event.preventDefault();
      const first = results.querySelector("a");
      if (first) document.location.href = first.href;
    });
    dialog.addEventListener("click", function (event) {
      if (event.target === dialog) closeDialog();
    });
    document.addEventListener("keydown", function (event) {
      const target = event.target;
      const isTyping = target instanceof HTMLInputElement ||
        target instanceof HTMLTextAreaElement ||
        target instanceof HTMLSelectElement ||
        (target && target.isContentEditable);
      if ((event.key === "/" && !isTyping) || ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k")) {
        event.preventDefault();
        open();
      }
      if (event.key === "Escape" && dialog.hasAttribute("open")) closeDialog();
    });
  }

  function initialize() {
    const main = document.querySelector("body > main");
    const article = main && main.querySelector(":scope > article");
    if (!main || !article) return;

    const controls = buildHeader();
    const layout = element("div", "docs-layout");
    const sidebar = buildSidebar(controls.menuButton);
    main.classList.add("docs-content");
    document.body.insertBefore(controls.header, main);
    document.body.insertBefore(layout, main);
    layout.append(sidebar, main);
    const toc = buildPageToc(article);
    if (toc) layout.append(toc);

    buildSearch(controls.searchButton);
    document.body.classList.add("docs-enhanced");
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initialize);
  } else {
    initialize();
  }
})();
