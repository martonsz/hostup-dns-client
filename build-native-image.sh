#!/bin/bash
# This script builds a native image of a Java application using GraalVM's native-image tool inside a Docker container.
set -e

docker_hub_username="martonsz"
image_name="hostup-dns-client"
if [[ $1 == "releaseVersion" ]]; then
  sed -i 's/-SNAPSHOT//g' gradle.properties
fi
version=$(grep '^version=' < gradle.properties | cut -d'=' -f2 | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')

full_image_name="${docker_hub_username}/${image_name}:${version}"

CLEAN_CACHE=false
for arg in "$@"; do
  if [ "$arg" = "--clean-cache" ]; then
    CLEAN_CACHE=true
  fi
done

cleanup() {
  echo "Cleaning up..."
  docker rm -f hostup-dns-client-extract || true
}
trap cleanup EXIT

# Build uses BuildKit cache mounts. No host path volumes are required.
if [ "$CLEAN_CACHE" = true ]; then
  echo "Cleaning BuildKit cache (requires Docker 25.0+ with buildx prune)"
  docker builder prune -af || true
fi

echo "Building native image for version $version"
DOCKER_BUILDKIT=1 docker build . -t "$full_image_name" --build-arg VERSION="$version"

docker rm -f hostup-dns-client-extract 2>/dev/null || true
docker create --name hostup-dns-client-extract "$full_image_name"
mkdir -p dist/
docker cp hostup-dns-client-extract:/hostup-dns-client dist/.
chmod +x dist/hostup-dns-client
echo "Native image built and available at dist/hostup-dns-client/hostup-dns-client"
