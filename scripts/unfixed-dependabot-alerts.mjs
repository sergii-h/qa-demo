#!/usr/bin/env node
import { readFileSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';

const root = process.argv[2];
if (!root) {
  console.error('Usage: unfixed-dependabot-alerts.mjs <repo-root> < alerts.json');
  process.exit(2);
}

const alerts = JSON.parse(readFileSync(0, 'utf8'));

function cmpVersion(a, b) {
  const tokenize = (value) =>
    String(value)
      .replace(/^v/i, '')
      .split(/([^0-9]+)/)
      .filter(Boolean)
      .map((token) => (/^\d+$/.test(token) ? Number(token) : token.toLowerCase()));
  const left = tokenize(a);
  const right = tokenize(b);
  const len = Math.max(left.length, right.length);
  for (let i = 0; i < len; i += 1) {
    const x = left[i] ?? 0;
    const y = right[i] ?? 0;
    if (x === y) {
      continue;
    }
    if (typeof x === 'number' && typeof y === 'number') {
      return x < y ? -1 : 1;
    }
    return String(x) < String(y) ? -1 : 1;
  }
  return 0;
}

function isVulnerable(version, patched) {
  if (!patched || patched === 'none') {
    return true;
  }
  return cmpVersion(version, patched) < 0;
}

function read(path) {
  const abs = join(root, path);
  if (!existsSync(abs)) {
    return null;
  }
  return readFileSync(abs, 'utf8');
}

function npmLockKeyMatches(key, packageName) {
  const suffix = `node_modules/${packageName}`;
  return key === suffix || key.endsWith(`/${suffix}`);
}

function npmVersions(lockfilePath, packageName) {
  const raw = read(lockfilePath);
  if (raw == null) {
    return { found: false, versions: [] };
  }
  const lock = JSON.parse(raw);
  const versions = [];

  if (lock.packages && typeof lock.packages === 'object') {
    for (const [key, entry] of Object.entries(lock.packages)) {
      if (!entry || typeof entry !== 'object') {
        continue;
      }
      if (!npmLockKeyMatches(key, packageName)) {
        continue;
      }
      const actualName = entry.name || packageName;
      if (actualName !== packageName) {
        continue;
      }
      if (entry.version) {
        versions.push(entry.version);
      }
    }
  }

  return { found: true, versions };
}

function resolvePomProps(value, props, depth = 0) {
  if (!value || depth > 8) {
    return value;
  }
  return value.replace(/\$\{([^}]+)}/g, (_, key) => {
    if (!(key in props)) {
      return `\${${key}}`;
    }
    return resolvePomProps(props[key], props, depth + 1);
  });
}

function mavenVersions(pomPath, packageName) {
  const raw = read(pomPath);
  if (raw == null) {
    return { found: false, versions: [], complete: false };
  }

  const [groupId, artifactId] = packageName.split(':');
  if (!groupId || !artifactId) {
    return { found: true, versions: [], complete: false };
  }

  const props = {};
  const propertiesMatch = raw.match(/<properties>([\s\S]*?)<\/properties>/);
  if (propertiesMatch) {
    const propertyRe = /<([a-zA-Z0-9_.-]+)>([^<]*)<\/\1>/g;
    let match;
    while ((match = propertyRe.exec(propertiesMatch[1])) !== null) {
      props[match[1]] = match[2].trim();
    }
  }

  const versions = [];
  const depRe = /<dependency>([\s\S]*?)<\/dependency>/g;
  let depMatch;
  while ((depMatch = depRe.exec(raw)) !== null) {
    const block = depMatch[1];
    const g = block.match(/<groupId>\s*([^<]+?)\s*<\/groupId>/);
    const a = block.match(/<artifactId>\s*([^<]+?)\s*<\/artifactId>/);
    if (!g || !a) {
      continue;
    }
    if (g[1].trim() !== groupId || a[1].trim() !== artifactId) {
      continue;
    }
    const v = block.match(/<version>\s*([^<]+?)\s*<\/version>/);
    if (!v) {
      return { found: true, versions, complete: false };
    }
    const resolved = resolvePomProps(v[1].trim(), props);
    if (resolved.includes('${')) {
      return { found: true, versions, complete: false };
    }
    versions.push(resolved);
  }

  return { found: true, versions, complete: versions.length > 0 };
}

function gradleLockVersions(manifestPath, packageName) {
  const manifestDir = dirname(manifestPath);
  const candidates = [
    manifestPath.endsWith('.lockfile') ? manifestPath : null,
    join(manifestDir, 'gradle.lockfile'),
    join(manifestDir, 'app/gradle.lockfile'),
  ].filter(Boolean);

  let sawFile = false;
  const versions = [];
  for (const candidate of candidates) {
    const raw = read(candidate);
    if (raw == null) {
      continue;
    }
    sawFile = true;
    const prefix = `${packageName}:`;
    for (const line of raw.split('\n')) {
      if (!line.startsWith(prefix)) {
        continue;
      }
      const rest = line.slice(prefix.length);
      const version = rest.split('=')[0];
      if (version) {
        versions.push(version);
      }
    }
  }

  return { found: sawFile, versions };
}

function swiftVersions(manifestPath, packageName) {
  const raw = read(manifestPath);
  if (raw == null) {
    return { found: false, versions: [] };
  }

  if (manifestPath.endsWith('Package.resolved')) {
    const resolved = JSON.parse(raw);
    const pins = resolved.pins || resolved.object?.pins || [];
    const versions = [];
    for (const pin of pins) {
      const identity = pin.identity || pin.package;
      if (identity !== packageName && pin.location !== packageName) {
        continue;
      }
      const version = pin.state?.version;
      if (version) {
        versions.push(version);
      }
    }
    return { found: true, versions };
  }

  const versions = [];
  const blockRe = new RegExp(
    `(?:^|\\n)\\s*${packageName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}:\\s*\\n([\\s\\S]*?)(?=\\n\\s*\\S|$)`,
  );
  const block = raw.match(blockRe);
  if (block) {
    const from = block[1].match(/from:\\s*['"]?([0-9][^'"\s]*)/);
    const exact = block[1].match(/exact:\\s*['"]?([0-9][^'"\s]*)/);
    if (from) {
      versions.push(from[1]);
    }
    if (exact) {
      versions.push(exact[1]);
    }
  }
  return { found: true, versions };
}

function versionsFor(alert) {
  const ecosystem = (alert.dependency?.package?.ecosystem || '').toLowerCase();
  const packageName = alert.dependency?.package?.name;
  const manifestPath = alert.dependency?.manifest_path;
  if (!packageName || !manifestPath) {
    return { found: false, versions: [], complete: false };
  }

  if (ecosystem === 'npm') {
    const result = npmVersions(manifestPath, packageName);
    return { ...result, complete: result.found };
  }
  if (ecosystem === 'maven') {
    if (manifestPath.endsWith('.lockfile') || manifestPath.includes('gradle')) {
      const gradle = gradleLockVersions(manifestPath, packageName);
      return { ...gradle, complete: gradle.found };
    }
    return mavenVersions(manifestPath, packageName);
  }
  if (ecosystem === 'gradle') {
    const gradle = gradleLockVersions(manifestPath, packageName);
    return { ...gradle, complete: gradle.found };
  }
  if (ecosystem === 'swift' || ecosystem === 'swift_package_manager') {
    const swift = swiftVersions(manifestPath, packageName);
    return { ...swift, complete: swift.found };
  }
  return { found: false, versions: [], complete: false };
}

function stillUnfixed(alert) {
  const patched = alert.security_vulnerability?.first_patched_version?.identifier || null;
  const { found, versions, complete } = versionsFor(alert);

  if (!found || complete === false) {
    return { unfixed: true, detail: 'could not prove a patched version in this checkout' };
  }
  if (versions.length === 0) {
    return { unfixed: false, detail: 'package not present in this checkout' };
  }

  const vulnerable = versions.filter((version) => isVulnerable(version, patched));
  if (vulnerable.length === 0) {
    return { unfixed: false, detail: `resolved ${[...new Set(versions)].join(', ')}` };
  }
  return { unfixed: true, detail: `resolved ${[...new Set(vulnerable)].join(', ')}` };
}

const remaining = [];
for (const alert of alerts) {
  const { unfixed, detail } = stillUnfixed(alert);
  const number = alert.number;
  const name = alert.dependency?.package?.name;
  const ecosystem = alert.dependency?.package?.ecosystem;
  const manifest = alert.dependency?.manifest_path || 'n/a';
  const status = unfixed ? 'STILL PRESENT' : 'patched in tree';
  console.error(`#${number} | ${status} | ${name} ${ecosystem} | ${manifest} | ${detail}`);
  if (unfixed) {
    remaining.push(alert);
  }
}

process.stdout.write(`${JSON.stringify(remaining)}\n`);
