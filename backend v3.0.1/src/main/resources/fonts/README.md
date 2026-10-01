# Invoice PDF fonts

`InvoicePdfRenderer` looks for these two files on the classpath to render
Unicode/Devanagari (Marathi, Hindi, Konkani) invoice text correctly instead
of falling back to "?" placeholders:

- `NotoSansDevanagari-Regular.ttf`
- `NotoSansDevanagari-Bold.ttf`

They are **not checked into this repository** (binary font files don't
belong in a code diff, and I don't have network access to fetch them for
you from this environment). To enable proper rendering:

1. Download "Noto Sans Devanagari" from Google Fonts:
   https://fonts.google.com/noto/specimen/Noto+Sans+Devanagari
2. From the downloaded family, take the Regular and Bold static `.ttf`
   files and rename them to exactly the two filenames above.
3. Place both files directly in this folder
   (`bookworm-backend/src/main/resources/fonts/`).
4. Rebuild (`mvn clean package`) so they're bundled into the jar.

Until these files are present, `InvoicePdfRenderer` automatically falls
back to the previous Helvetica-based rendering (logs a WARN on first use,
non-Latin-1 characters render as "?") - the app will keep working, invoices
just won't have correct Devanagari text until the fonts are added.
