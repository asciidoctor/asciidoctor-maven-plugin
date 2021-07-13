#!/bin/bash

# https://medium.com/geekculture/how-to-publish-artifacts-on-maven-central-24342fd286cd

readonly MAVEN_GPG_PASSPHRASE=kdA57DhsbyV78
readonly MAVEN_GPG_PRIVATE_KEY=$(cat private.gpg)
readonly OSSRH_USERNAME=abelsromero
readonly OSSRH_TOKEN=d6asFSDd324sFSD

# Automatic commit from branch
# https://github.community/t/how-does-one-commit-from-an-action/16127/4
