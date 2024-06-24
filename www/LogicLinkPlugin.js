var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'LogicLinkPlugin', 'coolMethod', [arg0]);
};

exports.saludar = function (arg0, success, error) {
    exec(success, error, 'LogicLinkPlugin', 'saludar', [arg0]);
};
