---
name: set-minimal-maven-version
description: Set the minimal supported Maven version of this project to the newest release that is at least 12 months old. Use when the user asks to update, bump, or review the minimal Maven version.
---

# Set minimal Maven version

The project policy: support Maven releases that are 12 months old or older, plus the most recent minor.
The minimal version is the newest Maven release that is at least 12 months old. It must never be less than 12 months old.

## Steps

1. Find the version number. Run:

   ```
   .claude/skills/set-minimal-maven-version/scripts/find-min-maven-version.sh
   ```

   The last line of the output is the version. Use it as `<MIN>`.
2. Make a branch named `set-minimal-maven-version-to-<MIN>`, for example `set-minimal-maven-version-to-3.9.11`. Follow the other branch conventions (steps to make a branch) in the `CLAUDE.md` file in the repository root.
3. Replace the old minimal version with `<MIN>` in these files:
   - `pom.xml`: `requireMavenVersion` in the `maven-enforcer-plugin` configuration.
   - `asciidoctor-maven-plugin/pom.xml`: `<prerequisites><maven>`.
   - `README.adoc`: the "Supported version" sentence.
   - `docs/modules/plugin/pages/compatibility-matrix.adoc`: the list of compatible Maven versions.
   - `.github/workflows/build.yaml`: in the first `maven` matrix (the build job), replace the old minimal version with `<MIN>`.
   - `CHANGELOG.adoc`: in "Unreleased", under "Build / Infrastructure", add `Set minimal Maven version to <MIN>`.
4. Do NOT change these items. They are the "current" version and the user keeps them:
   - the `maven.version` property in `pom.xml`
   - `.mvn/wrapper/maven-wrapper.properties`
   - the current version entry in the CI matrix, and the `maven` values in the other CI jobs
5. Warn the user: if the current version (wrapper, `maven.version`, CI) is lower than `<MIN>`, the enforcer rule fails the build with that version. Ask how to handle it.
6. Run `./mvnw clean verify`. Never use `install`.
7. Do not commit unless the user asks.
