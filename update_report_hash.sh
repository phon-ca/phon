#!/bin/bash
# Recreate app/src/main/resources/report/reports.hash

REPORT_DIR="app/src/main/resources/report"
ROOT_PATH="app/src/main/resources"
HASH_FILE="$REPORT_DIR/reports.hash"

# Find all .xml files, sort them, and compute SHA-1 hashes
find "$REPORT_DIR" -type f -name '*.xml' | sort | while read -r file; do
  relpath="${file#$ROOT_PATH/}"
  hash=$(shasum "$file" | awk '{print $1}')
  echo "$relpath=$hash"
done > "$HASH_FILE"

