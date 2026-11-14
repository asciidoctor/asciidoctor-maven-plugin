#!/usr/bin/env bash
# Print the newest Apache Maven release that is at least N months old.
# Usage: find-min-maven-version.sh [months]   (default: 12)
set -euo pipefail

months="${1:-12}"
base="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven"
cutoff=$(date -u -d "${months} months ago" +%s)

versions=$(curl -fsS "${base}/maven-metadata.xml" \
  | grep -oE '<version>[^<]+</version>' \
  | sed -E 's/<\/?version>//g' \
  | grep -E '^[0-9]+\.[0-9]+\.[0-9]+$' \
  | sort -rV)

for v in $versions; do
  modified=$(curl -fsSI "${base}/${v}/apache-maven-${v}.pom" \
    | tr -d '\r' | awk -F': ' 'tolower($1)=="last-modified" {print $2}')
  [ -z "$modified" ] && continue
  ts=$(date -u -d "$modified" +%s)
  if [ "$ts" -le "$cutoff" ]; then
    echo "Selected: ${v} (released $(date -u -d "$modified" +%F), cutoff $(date -u -d "@${cutoff}" +%F))" >&2
    echo "$v"
    exit 0
  fi
done

echo "No Maven release found that is ${months} months old or older" >&2
exit 1
