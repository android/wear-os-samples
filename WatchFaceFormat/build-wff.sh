#!/bin/bash
# Copyright 2023 Google LLC

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     https://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
set -e

JAVA="java"
INPUT_ROOT="${1:-./}"
DEST_AAB="${INPUT_ROOT}/out/mybundle.aab"
DEST_APK="${INPUT_ROOT}/out/mybundle.apk"

BUNDLETOOL="$(which bundletool)"
PACKAGE_NAME=$(xmllint --xpath 'string(//manifest/@package)' "${INPUT_ROOT}/AndroidManifest.xml")

if [[ -z "${ANDROID_HOME}" ]]; then
  echo "Error: ANDROID_HOME not defined, please set and try again"
  exit 1
else
  ANDROID_HOME="${ANDROID_HOME}"
fi

if [[ -z "${AAPT2}" ]]; then
  echo "Error: AAPT2 not defined, please set and try again"
  echo "e.g. <sdk-path>/build-tools/<version>/aapt2"
  exit 1
else
  AAPT2="${AAPT2}"
fi

if [[ -z "${ANDROID_JAR}" ]]; then
  echo "Error: ANDROID_JAR not defined, please set and try again"
  echo "e.g. <sdk-path>/platforms/android-<version>/android.jar"
  exit 1
else
  ANDROID_JAR="${ANDROID_JAR}"
fi

if [[ -z "${BUNDLETOOL}" ]]; then
  echo "Error: Bundletool is required to run this script"
  echo "See: https://developer.android.com/tools/bundletool for more details on bundletool"
  echo "or on a Mac, use 'brew install bundletool'"
  exit 1
fi

rm -rf "${INPUT_ROOT}/out"
mkdir "${INPUT_ROOT}/out"

mkdir -p "${INPUT_ROOT}/out/compiled_resources"
"${AAPT2}" compile --dir "${INPUT_ROOT}/res" -o "${INPUT_ROOT}/out/compiled_resources/"

"${AAPT2}" link --proto-format -o "${INPUT_ROOT}/out/base.apk" \
-I "${ANDROID_JAR}" \
--manifest "${INPUT_ROOT}/AndroidManifest.xml" \
-R "${INPUT_ROOT}"/out/compiled_resources/*.flat \
--auto-add-overlay \
--rename-manifest-package "${PACKAGE_NAME}" \
--rename-resources-package "${PACKAGE_NAME}" \

unzip -q "${INPUT_ROOT}/out/base.apk" -d "${INPUT_ROOT}/out/base-apk/"

mkdir -p "${INPUT_ROOT}/out/aab-root/base/manifest/"

cp "${INPUT_ROOT}/out/base-apk/AndroidManifest.xml" "${INPUT_ROOT}/out/aab-root/base/manifest/"
cp -r "${INPUT_ROOT}/out/base-apk/res" "${INPUT_ROOT}/out/aab-root/base"
cp "${INPUT_ROOT}/out/base-apk/resources.pb" "${INPUT_ROOT}/out/aab-root/base"

(cd "${INPUT_ROOT}/out/aab-root/base" && zip ../base.zip -q -r -X .)

"${BUNDLETOOL}" build-bundle --modules="${INPUT_ROOT}/out/aab-root/base.zip" --output="${DEST_AAB}"

if [[ ! -z "${DEST_APK}" ]]; then
  if [[ -f "${INPUT_ROOT}/out/result.apks" ]]; then
    rm "${INPUT_ROOT}/out/result.apks"
  fi

  "${BUNDLETOOL}" build-apks --bundle="${DEST_AAB}" --output="${INPUT_ROOT}/out/mybundle.apks" --mode=universal

  unzip "${INPUT_ROOT}/out/mybundle.apks" -d "${INPUT_ROOT}/out/result_apks/"

  cp "${INPUT_ROOT}/out/result_apks/universal.apk" "${DEST_APK}"
else
  echo "Not building apks"
fi

