#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
FIXTURE_PATH="${FIXTURE_PATH:-$(dirname "$0")/../data/wikipedia-billboard-sample.json}"
API_BASE="$BASE_URL/api/v1/knowledge-sources"

command -v curl >/dev/null 2>&1 || { echo "curl is required" >&2; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "jq is required" >&2; exit 1; }

existing="$(curl --fail --silent --show-error "$API_BASE")"

while IFS= read -r source; do
    uri="$(jq -r '.uri' <<< "$source")"
    match="$(jq -c --arg uri "$uri" '[.[] | select(.uri == $uri)] | first // empty' <<< "$existing")"

    if [[ -z "$match" ]]; then
        match="$(curl --fail --silent --show-error \
            -X POST \
            -H 'Content-Type: application/json' \
            --data "$source" \
            "$API_BASE")"
        echo "Created source $(jq -r '.id' <<< "$match"): $(jq -r '.title' <<< "$match")"
    else
        echo "Source already exists $(jq -r '.id' <<< "$match"): $(jq -r '.title' <<< "$match")"
    fi

    source_id="$(jq -r '.id' <<< "$match")"
    curl --fail --silent --show-error \
        -X POST \
        "$API_BASE/$source_id/chunks" >/dev/null
    echo "Prepared chunks for source $source_id"
done < <(jq -c '.[]' "$FIXTURE_PATH")