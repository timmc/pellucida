#!/bin/bash

cd -- "$(dirname "${BASH_SOURCE[0]}")/.."

APP=pellucida

# Create a tarball
mkdir -p target
tar czf target/heroku.tar.gz --exclude='.git/*' --exclude='target/*' .
commit_id=$(git show --format=oneline HEAD | head -n1 | cut -d' ' -f1,1)

# Authorize to .netrc
heroku auth:token > /dev/null

# Upload tarball
sources_json=$(
  curl -nsS -X POST -H 'Accept: application/vnd.heroku+json; version=3' \
    -- "https://api.heroku.com/apps/$APP/sources"
)

upload_dest="$(echo "$sources_json" | jq -r '.source_blob.put_url')"
curl -X PUT -H 'Content-Type:' --data-binary @target/heroku.tar.gz \
  -- "$upload_dest"

# Start build
download_url="$(echo "$sources_json" | jq -r '.source_blob.get_url')"
build_json=$(
  curl -nsS -X POST https://api.heroku.com/apps/$APP/builds \
    -d '{"source_blob":{"url":"'$download_url'", "version": "'$commit_id'"}}' \
    -H 'Accept: application/vnd.heroku+json; version=3' \
    -H "Content-Type: application/json"
)
echo "Started build $(echo "$build_json" | jq -r .id)"

# Watch build
stream_url="$(echo "$build_json" | jq -r '.output_stream_url')"
curl -nsS -H 'Accept: application/vnd.heroku+json; version=3' -- "$stream_url"
