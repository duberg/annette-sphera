#!/usr/bin/env bash
tsc gen.ts
node gen.js def/language.js
node gen.js def/user.js
node gen.js def/property.js
