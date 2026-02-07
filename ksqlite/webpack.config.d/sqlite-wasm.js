config.resolve = {
  fallback: {
    fs: false,
    path: false,
    crypto: false,
  }
};

const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');
/*config.plugins.push(
  new CopyWebpackPlugin({
    patterns: [
      './kotlin/esm64/sqlite3-64bit.wasm'
    ]
  })
);*/

config.plugins.push(
  new CopyWebpackPlugin({
    patterns: [
      {
        from: path.resolve(__dirname, './kotlin/esm64/sqlite3-64bit.wasm'),
        to: 'esm64/sqlite3-64bit.wasm',
        toType: 'file'
      }
    ]
  })
);

// Exclude sqlite3 wasm from webpack's asset processing
config.module = config.module || {};
config.module.rules = config.module.rules || [];
config.module.rules.push({
  test: /sqlite3-64bit\.wasm$/,
  type: 'asset/resource',
  generator: {
    filename: 'sqlite3-64bit.wasm' // Keep original filename
  }
});