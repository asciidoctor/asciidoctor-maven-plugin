#!/usr/bin/env bash
# Generate a new GPG key to sign releases.
#
# Usage: scripts/generate-gpg-release-key.sh [OUTPUT_DIR]
#
# The script asks for the name, email, expiry and passphrase.
# It writes these files to OUTPUT_DIR (default: ./gpg-release-keys):
#   private.asc          armored private key
#   private-oneline.txt  private key on one line, for the GPG_PRIVATE_KEY secret
#   public.asc           armored public key
#   revoke.asc           revocation certificate
#   fingerprint.txt      key fingerprint, for the GPG_KEYNAME secret
#
# Keep the output files secret. Do not commit them.

set -euo pipefail

OUTPUT_DIR="${1:-./gpg-release-keys}"

NAME=""
EMAIL=""
EXPIRY=""
PASSPHRASE=""
FINGERPRINT=""

fail() {
  echo "Error: $1" >&2
  exit 1
}

check_requirements() {
  command -v gpg >/dev/null 2>&1 || fail "gpg is not installed."
}

ask_details() {
  local passphrase_repeat

  read -r -p "Real name: " NAME
  read -r -p "Email: " EMAIL
  read -r -p "Expiry (for example 1y, 0 = never) [1y]: " EXPIRY
  EXPIRY="${EXPIRY:-1y}"
  read -r -s -p "Passphrase: " PASSPHRASE
  echo
  read -r -s -p "Repeat passphrase: " passphrase_repeat
  echo

  [[ -n "$NAME" && -n "$EMAIL" && -n "$PASSPHRASE" ]] \
    || fail "name, email and passphrase are required."
  [[ "$PASSPHRASE" == "$passphrase_repeat" ]] \
    || fail "the passphrases do not match."
}

generate_key() {
  echo "Generating key..."
  gpg --batch --quiet --generate-key <(cat <<EOF
Key-Type: RSA
Key-Length: 4096
Key-Usage: sign
Name-Real: $NAME
Name-Email: $EMAIL
Expire-Date: $EXPIRY
Passphrase: $PASSPHRASE
%commit
EOF
)
}

find_fingerprint() {
  FINGERPRINT="$(gpg --list-secret-keys --with-colons "$EMAIL" \
    | awk -F: '/^fpr/ {print $10}' | tail -n 1)"
  [[ -n "$FINGERPRINT" ]] || fail "cannot find the new key."
}

export_fingerprint() {
  echo "$FINGERPRINT" > "$OUTPUT_DIR/fingerprint.txt"
}

export_private_key() {
  gpg --batch --pinentry-mode loopback --passphrase "$PASSPHRASE" \
    --armor --export-secret-keys "$FINGERPRINT" > "$OUTPUT_DIR/private.asc"
  awk '{printf "%s\\n", $0}' "$OUTPUT_DIR/private.asc" > "$OUTPUT_DIR/private-oneline.txt"
}

export_public_key() {
  gpg --armor --export "$FINGERPRINT" > "$OUTPUT_DIR/public.asc"
}

export_revocation_certificate() {
  gpg --batch --pinentry-mode loopback --passphrase "$PASSPHRASE" \
    --yes --output "$OUTPUT_DIR/revoke.asc" --gen-revoke "$FINGERPRINT" >/dev/null 2>&1 <<EOF
y
0

y
EOF
}

export_all() {
  umask 077
  mkdir -p "$OUTPUT_DIR"
  export_fingerprint
  export_private_key
  export_public_key
  export_revocation_certificate
}

print_summary() {
  cat <<EOF

Done. Key fingerprint:
  $FINGERPRINT

Files in $OUTPUT_DIR:
  private.asc, private-oneline.txt, public.asc, revoke.asc, fingerprint.txt

Next steps:
  1. Send the public key to the keyservers:
       gpg --keyserver keyserver.ubuntu.com --send-keys $FINGERPRINT
       gpg --keyserver keys.openpgp.org --send-keys $FINGERPRINT
     keys.openpgp.org sends a verification email. Confirm it.
  2. Set the GitHub secrets:
       gh secret set GPG_KEYNAME --body "$FINGERPRINT"
       gh secret set GPG_PASSPHRASE
       gh secret set GPG_PRIVATE_KEY < $OUTPUT_DIR/private-oneline.txt
  3. Make a backup of the files. Then delete them from this machine:
       shred -u $OUTPUT_DIR/private.asc $OUTPUT_DIR/private-oneline.txt
EOF
}

main() {
  check_requirements
  ask_details
  generate_key
  find_fingerprint
  export_all
  PASSPHRASE=""
  print_summary
}

main
