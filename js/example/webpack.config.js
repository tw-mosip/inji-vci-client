const path = require('path');

module.exports = {
  entry: './main.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'public')
  },
  module: {
    rules: [
      {
        test: /\.m?js$/,
        exclude: /(node_modules|bower_components)/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: [['@babel/preset-env', { modules: false }]]
          }
        }
      }
    ]
  },
  resolve: {
    extensions: ['.js'],
    alias: {
      'inji-vci-client': path.resolve(__dirname, '.yalc/inji-vci-client/src/index.js')
    }
  },
  mode: 'development'
};
