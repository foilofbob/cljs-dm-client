# DEVOXXGENIE.md

## Project Guidelines

### Build Commands

- **Build:** `mise run release`
- **Test:** `mise run clj-kondo` (for linting)
- **Single Test:** Not applicable (use specific test commands as needed)
- **Run:** `mise run shadow` (for development)
- **Clean:** `mise run prerelease` (platform-specific clean)
- **Format Check:** `mise run cljfmt-check`
- **Format Fix:** `mise run cljfmt-fix`

### Code Style

- **Formatting:** Use IDE or checkstyle for formatting
- **Naming:**
  - Use kebab-case for namespaces, functions and variables
  - Use SCREAMING_SNAKE_CASE for constants
- **Documentation:** Use clojure.doc for documentation
- **Dependencies:** Keep dependencies up to date

### Dependencies

The project uses the following main dependencies:

See project.clj or deps.edn for the complete dependency list.
