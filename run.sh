#!/bin/bash
set -a && source /opt/toponavi/.env && set +a
export GITHUB_APP_PRIVATE_KEY="$(cat /opt/toponavi/github-app.pem)"
./gradlew :toponavi-web:bootRun
