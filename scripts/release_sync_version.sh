#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PYPROJECT_FILE="$ROOT_DIR/server_backend_django/pyproject.toml"
SETTINGS_FILE="$ROOT_DIR/server_backend_django/gss/gss/settings.py"

if [[ ! -f "$PYPROJECT_FILE" ]]; then
  echo "Unable to find pyproject.toml at $PYPROJECT_FILE" >&2
  exit 1
fi
if [[ ! -f "$SETTINGS_FILE" ]]; then
  echo "Unable to find settings.py at $SETTINGS_FILE" >&2
  exit 1
fi

RAW_TAG="${1:-}"
if [[ -z "$RAW_TAG" ]]; then
  RAW_TAG="$(git -C "$ROOT_DIR" describe --tags --exact-match 2>/dev/null || true)"
fi

if [[ -z "$RAW_TAG" ]]; then
  echo "No exact git tag found. Pass one explicitly: scripts/release_sync_version.sh <tag>" >&2
  exit 1
fi

# Common tag format is vX.Y[.Z][suffix]. Strip leading v for pyproject version.
VERSION="${RAW_TAG#v}"

# Keep this permissive enough for versions like 4.10, 4.10.1, 4.10rc1, 4.19+BB
if [[ ! "$VERSION" =~ ^[0-9]+(\.[0-9]+){1,2}([A-Za-z0-9._-]+)?(\+[A-Za-z0-9._-]+)?$ ]]; then
  echo "Tag '$RAW_TAG' is not a supported version format." >&2
  exit 1
fi

sed -i -E "s/^version = \"[^\"]+\"/version = \"$VERSION\"/" "$PYPROJECT_FILE"
sed -i -E "s/^[[:space:]]*GSS_VERSION[[:space:]]*=[[:space:]]*\"[^\"]+\"/GSS_VERSION = \"$VERSION\"/" "$SETTINGS_FILE"

echo "Updated $PYPROJECT_FILE to version $VERSION"
echo "Updated $SETTINGS_FILE GSS_VERSION to $VERSION"
echo "Build frontend with: --dart-define=GSS_GIT_TAG=$VERSION"
