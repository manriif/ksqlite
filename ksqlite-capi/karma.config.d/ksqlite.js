config.files.push({
  pattern: "kotlin/ksqlite.wasm",
  included: false,
  served: true,
  watched: false
});

// Large timeout for debugging, keeping karma serving files in devtools
config.client = config.client || {};
config.client.mocha = config.client.mocha || {};
config.client.mocha.timeout = 60 * 60 * 6;