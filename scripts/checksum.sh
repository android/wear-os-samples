#
#  Copyright 2023 Google, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

#!/bin/bash
SAMPLE=$1
RESULT_FILE=$2

# Check if the result file exists and prompt for overwrite if necessary
if [ -f "$RESULT_FILE" ]; then
  read -p "Result file already exists. Overwrite? (y/n): " overwrite
  if [[ $overwrite != "y" ]]; then
    exit 1
  fi
fi

# Create an empty result file
> "$RESULT_FILE"

# Function to calculate the MD5 checksum of a file
checksum_file() {
  echo $(openssl md5 "$1" | awk '{print $2}')
}

# Find files matching specific patterns and store them in the FILES array
mapfile -d '' FILES < <(find "$SAMPLE" -type f \( -name "build.gradle*" -o -name "gradle-wrapper.properties" -o -name "robolectric.properties" \) -print0)

# Loop through files and append the MD5 checksums to the result file
for FILE in "${FILES[@]}"; do
  echo "$(checksum_file "$FILE")" >> "$RESULT_FILE"
done

# Sort the result file in-place for idempotent results
sort -o "$RESULT_FILE" "$RESULT_FILE"
