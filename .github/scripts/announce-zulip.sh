#!/usr/bin/env bash
# Environment variables:
#   RELEASE_VERSION   Version of the release.
#   RELEASE_TAG       Git tag of the release.
#   ZULIP_BOT_EMAIL   Email of the bot.
#   ZULIP_API_KEY     API key of the bot.
#   ZULIP_CHANNEL_ID  ID of the channel.
#
# Setup:
#   1. Create a bot of type "Incoming webhook" in Zulip. The owner of the bot
#      must have permission to post in the channel.
#   2. Create the GitHub environment "announce" with:
#        variables: ZULIP_BOT_EMAIL, ZULIP_CHANNEL_ID
#        secret:    ZULIP_API_KEY

set -euo pipefail

: "${RELEASE_VERSION:?is not set}"
: "${RELEASE_TAG:?is not set}"
: "${ZULIP_BOT_EMAIL:?is not set}"
: "${ZULIP_API_KEY:?is not set}"
: "${ZULIP_CHANNEL_ID:?is not set}"

ZULIP_URL="https://asciidoctor.zulipchat.com/api/v1/messages"
REPOSITORY_URL="https://github.com/asciidoctor/asciidoctor-maven-plugin"

TOPIC=""
CONTENT=""

is_final_version() {
  [[ "$RELEASE_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]
}

build_message() {
  TOPIC="asciidoctor-maven-plugin $RELEASE_VERSION"
  CONTENT="Asciidoctor Maven Tools v$RELEASE_VERSION is released 🎉
Release notes: $REPOSITORY_URL/releases/tag/$RELEASE_TAG"
}

show_message() {
  printf 'Topic: %s\nContent:\n%s\n' "$TOPIC" "$CONTENT"
}

send_message() {
  printf 'user = "%s:%s"\n' "$ZULIP_BOT_EMAIL" "$ZULIP_API_KEY" \
    | curl --config - \
      --silent --show-error --fail-with-body \
      --max-time 60 \
      --data-urlencode "type=stream" \
      --data-urlencode "to=$ZULIP_CHANNEL_ID" \
      --data-urlencode "topic=$TOPIC" \
      --data-urlencode "content=$CONTENT" \
      "$ZULIP_URL"
}

# The message is already sent. A failure here must not fail the script,
# because a second run sends the message again.
add_link_to_summary() {
  local url

  url="$(jq -r '.message_url // empty' <<<"$1")" || url=""
  if [[ -z "$url" ]]; then
    echo "Warning: cannot get the link to the message."
    return
  fi

  echo "[Zulip announcement]($url)" >> "${GITHUB_STEP_SUMMARY:-/dev/null}" \
    || echo "Warning: cannot write the summary."
}

main() {
  local response

  if ! is_final_version; then
    echo "Version '$RELEASE_VERSION' is not final. No message is necessary."
    return
  fi

  build_message
  show_message

  if ! response="$(send_message)"; then
    printf '%s\n' "$response"
    exit 1
  fi
  printf '%s\n' "$response"

  add_link_to_summary "$response"
}

main
