config.files.push({
  pattern: "kotlin/ksqlite/ksqlite.wasm",
  included: false,
  served: true,
  watched: false
});

config.client = config.client || {};
config.client.env = config.client.env || {};

config.client.env.ksqlite = {
    isTest: true,
    prefix: "base/kotlin/ksqlite"
}