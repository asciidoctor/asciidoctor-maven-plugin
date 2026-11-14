# CLAUDE.md

## Setup

The local copy must have these git remotes:

- `origin`: points to a personal fork of the project.
- `upstream`: points to the official repository, https://github.com/asciidoctor/asciidoctor-maven-plugin.

Never push to `upstream`. Always push to `origin`.

To add the `upstream` remote, run:

```
git remote add upstream git@github.com:asciidoctor/asciidoctor-maven-plugin.git
```

## Tests

Run the full test suite to validate each change:

```
./mvnw clean verify -Prun-its
```

Run it before you report that a change is done. Never use `install`.
