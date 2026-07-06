#!/usr/bin/env bash
set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "setup-docker-macos-ci.sh is macOS-only." >&2
  exit 1
fi

if colima status >/dev/null 2>&1; then
  echo "Colima is already running."
  docker info >/dev/null
  exit 0
fi

export HOMEBREW_NO_AUTO_UPDATE=1
export HOMEBREW_NO_INSTALL_UPGRADE=1
export HOMEBREW_NO_INSTALLED_DEPENDENTS_CHECK=1

brew install colima docker docker-compose docker-buildx

mkdir -p ~/.docker/cli-plugins
ln -sfn "$(brew --prefix docker-compose)/bin/docker-compose" ~/.docker/cli-plugins/docker-compose
ln -sfn "$(brew --prefix docker-buildx)/bin/docker-buildx" ~/.docker/cli-plugins/docker-buildx

cpu_count="$(sysctl -n hw.ncpu)"
memory_gb="$(( $(sysctl -n hw.memsize) / 1024 / 1024 / 1024 ))"
colima_cpus=$(( cpu_count > 2 ? cpu_count - 1 : cpu_count ))
colima_memory=$(( memory_gb > 4 ? memory_gb - 2 : 4 ))

arch_name="$(uname -m)"
if [[ "$arch_name" == "arm64" ]]; then
  colima_arch="aarch64"
else
  colima_arch="x86_64"
fi

echo "Starting Colima (arch=${colima_arch}, cpus=${colima_cpus}, memory=${colima_memory}GiB)..."
colima start \
  --cpu "$colima_cpus" \
  --memory "$colima_memory" \
  --arch "$colima_arch" \
  --vm-type vz \
  --mount-type virtiofs

docker info
