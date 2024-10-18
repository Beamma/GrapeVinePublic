const path = require('path');

module.exports = {
    entry: './src/main/resources/static/js/livestream.js',
    output: {
        filename: 'bundledLivestream.js',
        path: path.resolve(__dirname, './src/main/resources/static/js'),
    },
};
