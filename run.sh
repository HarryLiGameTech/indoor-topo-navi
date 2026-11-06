#!/bin/bash
# Loads SPRING_DATASOURCE_* and TOPONAVI_SERVICE_TOKEN (see .env.example); no GitHub credentials are needed.
set -a && source /opt/toponavi/.env && set +a
./gradlew :toponavi-web:bootRun
