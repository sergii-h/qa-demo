#!/usr/bin/env bash
set -euo pipefail

ANDROID_APP="${1:?android/app directory path is required}"
FONTS_DIR="${ANDROID_APP}/src/main/assets/fonts"
SOURCE_FONT="${FONTS_DIR}/MaterialCommunityIcons.ttf"
TARGET_FONT="${FONTS_DIR}/material-community.ttf"

if [[ ! -f "$SOURCE_FONT" ]]; then
  echo "MaterialCommunityIcons.ttf not found at ${SOURCE_FONT}; run expo prebuild first" >&2
  exit 1
fi

cp "$SOURCE_FONT" "$TARGET_FONT"
echo "Copied icon font to assets/fonts/material-community.ttf"
