# Tiptap vendor bundle

Tiptap 3.32.2 (MIT), ProseMirror Markdown 1.13.4 (MIT).
Only open source packages; no cloud service, Pro extension, runtime CDN or React/Vue.
The application integration is maintained separately in `../webapp/discussion/jsp/javaScript/discussion-rich-editor.js`.

Rebuild with Node and npm:

```sh
npm ci --ignore-scripts
npm run build
```

The lockfile pins the exact npm tarball integrity for all dependencies.
The generated bundle is committed so ordinary Maven builds remain offline-capable.
All package copyright and license notices are preserved in
`../webapp/discussion/jsp/javaScript/vendor/tiptap-THIRD-PARTY-NOTICES.txt`.
Source: https://github.com/ueberdosis/tiptap and https://github.com/ProseMirror/prosemirror-markdown.

Bundle SHA-256: `dbc31226c5a5b636284afe6fca28e2ce37dd9bd343d3da17a04f643827568ef3`.
Bundle size: 554017 bytes (gzip: 180907 bytes).
