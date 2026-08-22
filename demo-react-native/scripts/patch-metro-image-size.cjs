'use strict';

const fs = require('fs');
const path = require('path');

const original = 'const getImageSize = require("image-size");';
const wrapper = `const imageSizeModule = require("image-size");
const imageSizeImpl = typeof imageSizeModule === "function" ? imageSizeModule : imageSizeModule.imageSize;
function getImageSize(input) {
  const payload = typeof input === "string" ? fs.readFileSync(input) : input;
  return imageSizeImpl(payload);
}`;

const assetsJs = path.join(__dirname, '..', 'node_modules', 'metro', 'src', 'Assets.js');
if (!fs.existsSync(assetsJs)) {
  process.exit(0);
}

const src = fs.readFileSync(assetsJs, 'utf8');
if (src.includes('imageSizeImpl')) {
  process.exit(0);
}

let next = src;
if (src.includes(original)) {
  next = src.replace(original, wrapper);
} else if (src.includes('const imageSizeModule = require("image-size");')) {
  next = src.replace(
    /const imageSizeModule = require\("image-size"\);\nconst getImageSize = typeof imageSizeModule === "function" \? imageSizeModule : imageSizeModule\.imageSize;/,
    wrapper,
  );
}

if (next === src) {
  console.error('metro Assets.js no longer uses require("image-size"); image-size-next Metro compat patch skipped');
  process.exit(1);
}

fs.writeFileSync(assetsJs, next);
