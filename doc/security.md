# Security Scanning

This project uses GitHub-native security tooling for dependency, secret, and static analysis. No third-party SaaS is required for the baseline setup.

---

## 1. SCA — Dependabot (dependency scanning)

**What it catches:** Known CVEs in direct and transitive dependencies (Maven and npm).

**Repository config:** [`.github/dependabot.yml`](../.github/dependabot.yml) monitors:

| Ecosystem | Directories |
|-----------|-------------|
| Maven | `/demo-service`, `/notification-service`, `/e2e/selenide-junit5-selenium-grid`, `/` (root aggregator) |
| npm | `/demo-interface`, `/e2e/playwright-typescript` |

**One-time GitHub setup (repo admin):**

1. Open **Settings → Code security and analysis**
2. Enable **Dependabot alerts** (vulnerability notifications)
3. Enable **Dependabot security updates** (optional auto-fix PRs for advisories)

Dependabot opens weekly version-update PRs from the config file; security advisories appear under **Security → Dependabot**.

---

## 2. Secret scanning

**What it catches:** Committed API keys, tokens, private keys, and common credential patterns.

**Repository config:** None required — GitHub scans pushes and PRs when the feature is enabled on the repository.

**One-time GitHub setup (repo admin):**

1. **Settings → Code security and analysis**
2. Enable **Secret scanning** (included for public repos; private repos need GitHub Advanced Security)
3. Enable **Push protection** to block commits that contain detected secrets

If a secret is exposed, rotate it immediately; removing the commit alone is not sufficient.

---

## 3. SAST — CodeQL

**What it catches:** Code patterns linked to injection, unsafe deserialization, XSS, path traversal, and similar issues in Java and TypeScript.

**Repository config:** [`.github/workflows/codeql.yml`](../.github/workflows/codeql.yml) runs on:

- Push and pull requests to `master`
- Weekly schedule (Mondays 05:30 UTC)

Java analysis compiles all Maven modules; TypeScript analysis builds `demo-interface` and indexes `e2e/playwright-typescript`.

**One-time GitHub setup (repo admin):**

1. Enable **Code scanning** under **Settings → Code security and analysis** (default for public repos)
2. Review findings under **Security → Code scanning alerts**

---

## Developer workflow

| When | Action |
|------|--------|
| Dependabot opens a PR | Review changelog and run existing CI before merge |
| Secret scanning blocks a push | Remove the secret from the change, use GitHub Actions secrets or local env files (never committed) |
| CodeQL reports on a PR | Fix or document a false positive; use `// codeql[...]` suppressions only with justification |

For feature work, prefer fixing high/critical findings before merge; track lower-severity items in the backlog.
