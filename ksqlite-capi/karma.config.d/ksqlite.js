const path = require("path");
const os = require("os");
const kotlin = path.resolve("./kotlin")
const ksqliteWasm = path.resolve(kotlin, "ksqlite.wasm")

config.files.push({
  pattern: ksqliteWasm,
  served: true,
  watched: false,
  included: false,
  nocache: false,
});

config.proxies["/sqlite3-64bit.wasm"] = `/absolute${ksqliteWasm}`

// Large timeout for debugging, keeping karma serving files in devtools
config.client = config.client || {};
config.client.mocha = config.client.mocha || {};
config.client.mocha.timeout = 6000000000;