#!/usr/bin/env node

const fs = require('fs');
const path = require('path');

const { writeMergedAllureEnvironmentInfo } = require('../support/allure/environmentInfo');

const targetDir = path.resolve(process.argv[2] || 'allure-results');
const sourceDirs = process.argv.slice(3).map((dir) => path.resolve(dir));

if (sourceDirs.length === 0) {
  process.exit(0);
}

if (fs.existsSync(targetDir)) {
  for (const entry of fs.readdirSync(targetDir, { withFileTypes: true })) {
    if (entry.isFile()) {
      fs.unlinkSync(path.join(targetDir, entry.name));
    }
  }
} else {
  fs.mkdirSync(targetDir, { recursive: true });
}

for (const sourceDir of sourceDirs) {
  if (!fs.existsSync(sourceDir)) {
    continue;
  }

  for (const file of fs.readdirSync(sourceDir)) {
    const sourcePath = path.join(sourceDir, file);
    if (!fs.statSync(sourcePath).isFile()) {
      continue;
    }

    fs.copyFileSync(sourcePath, path.join(targetDir, file));
  }
}

if (sourceDirs.length > 1) {
  writeMergedAllureEnvironmentInfo(targetDir, sourceDirs);
}

for (const sourceDir of sourceDirs) {
  fs.rmSync(sourceDir, { recursive: true, force: true });
}
