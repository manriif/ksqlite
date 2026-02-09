const path = require("path");
const os = require("os");
const wasm = path.resolve("./kotlin/sqlite3-64bit.wasm")

config.files.push({
  pattern: wasm,
  served: true,
  watched: false,
  included: false,
  nocache: false,
});

config.proxies["/sqlite3-64bit.wasm"] = `/absolute${wasm}`

// Adapted from: https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818
const output = {
  path: path.join(os.tmpdir(), '_karma_webpack_') + Math.floor(Math.random() * 1000000),
}
config.set({
  webpack: {...config.webpack, output}
});
config.files.push({
  pattern: `${output.path}/**/*`,
  watched: false,
  included: false,
});

// TODO: Figure out why on earth this is necessary. Presumably a karma-webpack bug???
delete config.webpack.optimization;

config.client = config.client || {};
config.client.mocha = config.client.mocha || {};
config.client.mocha.timeout = 6000000000; // No timeout