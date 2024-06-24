var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'LogicLinkPlugin', 'coolMethod', [arg0]);
};

exports.saludarMethod = function (arg0, success, error) {
    exec(success, error, 'LogicLinkPlugin', 'saludarMethod', [arg0]);
};
