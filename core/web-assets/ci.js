#!/usr/bin/env node

const child_process = require('child_process');
const fs    = require('fs');
const path  = require('path');

var doUpdate = false;

const touchfile = path.join(__dirname, 'target', 'ci.done');

if (fs.existsSync(touchfile)) {
  const touchstats = fs.statSync(touchfile);
  const packagestats = fs.statSync(path.join(__dirname, 'package-lock.json'));

  const touchtime = (touchstats && touchstats.mtimeMs) ? touchstats.mtimeMs : 0;
  const packagetime = (packagestats && packagestats.mtimeMs) ? packagestats.mtimeMs : 0;

  if (touchtime > packagetime) {
    console.debug('package-lock.json has not changed since the last "npm ci" run');
  } else {
    doUpdate = true;
  }
} else {
  doUpdate = true;
}

if (doUpdate) {
  console.info('node_modules is out of date compared to package-lock.json');
  const child = child_process.execFile(path.join(__dirname, 'target', 'node', 'npm'), [ '--prefer-offline', '--no-progress', 'ci' ]);
  child.stdout.pipe(process.stdout);
  child.stderr.pipe(process.stderr);
  child.on('error', (err) => {
    console.error('npm ci failed: ' + err);
    process.exit(1);
  });
  fs.mkdirSync(path.join(__dirname, 'target'), { recursive: true });
  fs.writeFileSync(touchfile, 'Kilroy Was Here m OvO m\n');
}