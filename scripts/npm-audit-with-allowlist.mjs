import { spawnSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const FAIL_SEVERITIES = new Set(['high', 'critical']);
const REPORT_SEVERITIES = new Set(['moderate', 'high', 'critical']);

export function loadAllowlist(allowlistPath) {
  const parsed = JSON.parse(readFileSync(allowlistPath, 'utf8'));
  return Array.isArray(parsed.noUpstreamFix) ? parsed.noUpstreamFix : [];
}

function ghsaFrom(via) {
  const match = String(via?.url || '').match(/GHSA-[0-9a-z-]+/i);
  return match ? match[0] : null;
}

function entryFor(allowlist, ghsa, packageName) {
  return allowlist.find((entry) => {
    if (!entry || entry.ghsa !== ghsa) {
      return false;
    }
    if (entry.package && entry.package !== packageName) {
      return false;
    }
    return !entry.ecosystem || String(entry.ecosystem).toLowerCase() === 'npm';
  }) || null;
}

function hasNonBreakingFix(vuln) {
  const fix = vuln.fixAvailable;
  if (fix === true) {
    return true;
  }
  return Boolean(fix && fix.name === vuln.name && !fix.isSemVerMajor);
}

function advisoryDetail(vuln) {
  const parts = (vuln.via || [])
    .map((via) => (typeof via === 'string' ? via : via.url || via.title))
    .filter(Boolean);
  return parts.join(', ') || 'vulnerable';
}

export function classifyAudit(vulnerabilities = {}, allowlist = []) {
  const decisions = new Map();

  function isAllowed(name, stack) {
    if (decisions.has(name)) {
      return decisions.get(name).allowed;
    }
    const vuln = vulnerabilities[name];
    if (!vuln || stack.has(name)) {
      return false;
    }
    const vias = vuln.via || [];
    if (vias.length === 0) {
      decisions.set(name, { allowed: false, detail: 'vulnerable' });
      return false;
    }

    stack.add(name);
    const dependsOn = [];
    let reason = null;
    const allowed = vias.every((via) => {
      if (typeof via === 'string') {
        dependsOn.push(via);
        return isAllowed(via, stack);
      }
      const entry = entryFor(allowlist, ghsaFrom(via), name);
      if (!entry || hasNonBreakingFix(vuln)) {
        return false;
      }
      reason = entry.reason || entry.ghsa;
      return true;
    });
    stack.delete(name);

    const decision = allowed
      ? { allowed: true, detail: reason || `depends on ${dependsOn.join(', ')}` }
      : { allowed: false, detail: advisoryDetail(vuln) };
    decisions.set(name, decision);
    return decision.allowed;
  }

  const findings = Object.values(vulnerabilities)
    .filter((vuln) => REPORT_SEVERITIES.has(vuln.severity))
    .map((vuln) => {
      isAllowed(vuln.name, new Set());
      return { name: vuln.name, severity: vuln.severity, ...decisions.get(vuln.name) };
    })
    .sort((left, right) => Number(left.allowed) - Number(right.allowed));

  return {
    findings,
    unfixedHigh: findings.filter((finding) => !finding.allowed && FAIL_SEVERITIES.has(finding.severity)),
  };
}

function readAudit() {
  const audit = spawnSync('npx', ['--yes', 'npm@11.6.2', 'audit', '--json'], {
    encoding: 'utf8',
    cwd: process.cwd(),
  });
  if (audit.error) {
    throw audit.error;
  }
  const stdout = audit.stdout || '';
  const jsonStart = stdout.indexOf('{');
  if (jsonStart === -1) {
    process.stderr.write(audit.stderr || '');
    process.stderr.write(stdout);
    process.exit(audit.status || 1);
  }
  const report = JSON.parse(stdout.slice(jsonStart));
  if (report.error) {
    process.stderr.write(`${report.error.summary || report.error}\n`);
    process.exit(typeof audit.status === 'number' && audit.status !== 0 ? audit.status : 1);
  }
  return report.vulnerabilities || {};
}

function main() {
  const allowlistPath = join(dirname(fileURLToPath(import.meta.url)), 'dependabot-allowlist.json');
  const { findings, unfixedHigh } = classifyAudit(readAudit(), loadAllowlist(allowlistPath));
  for (const finding of findings) {
    const status = finding.allowed ? 'allowed, no upstream fix' : 'STILL PRESENT';
    console.log(`${finding.name} | ${finding.severity} | ${status} | ${finding.detail}`);
  }
  if (unfixedHigh.length > 0) {
    console.error(`Found ${unfixedHigh.length} high or critical vulnerability(ies) not covered by scripts/dependabot-allowlist.json.`);
    process.exit(1);
  }
  console.log('No unfixed high or critical vulnerabilities. Allowed entries in scripts/dependabot-allowlist.json stay until upstream publishes a fix.');
}

const entry = process.argv[1];
if (entry && import.meta.url === pathToFileURL(entry).href) {
  main();
}
