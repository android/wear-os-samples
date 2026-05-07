#!/usr/bin/env bash

RENDERER_PKG="com.google.android.wearable.protolayout.renderer"
MIN_VERSION=100041159

# Check if the package is installed and get the version code
VERSION_CODE_STR=$(adb shell dumpsys package "$RENDERER_PKG" | grep "versionCode" | head -n 1 | awk '{print $1}' | cut -d= -f2)

if [ -z "$VERSION_CODE_STR" ]; then
  echo "Error: Package $RENDERER_PKG not found or version could not be determined."
  exit 1
fi

if [ "$VERSION_CODE_STR" -lt "$MIN_VERSION" ]; then
  echo "Error: $RENDERER_PKG version $VERSION_CODE_STR is older than required $MIN_VERSION."
  exit 1
fi

# Build and install the app
./gradlew :app:installDebug

COMPONENT_NAME="com.google.example.wear_widget/.HelloWidgetService"

# Add the tile and capture output
OUTPUT=$(adb shell am broadcast \
  -a com.google.android.wearable.app.DEBUG_SURFACE \
  --es operation add-tile \
  --ecn component "$COMPONENT_NAME" \
  --ei type 1)

# Extract Index (e.g., from Index=[0])
INDEX=$(echo "$OUTPUT" | grep -o 'Index=\[[0-9]*\]' | sed 's/Index=\[\([0-9]*\)\]/\1/')

if [ -n "$INDEX" ]; then
  echo "Added tile ID: $INDEX"
  # Show the tile
  adb shell am broadcast \
    -a com.google.android.wearable.app.DEBUG_SYSUI \
    --es operation show-tile \
    --ei index "$INDEX" >/dev/null
  echo "Tile is now visible on the device."
else
  echo "Failed to parse tile ID from output:"
  echo "$OUTPUT"
  exit 1
fi
