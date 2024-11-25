var exec = require('cordova/exec');

function EpsonLWPrint() { }

EpsonLWPrint.prototype.startDiscover = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "startDiscover", []);
}

EpsonLWPrint.prototype.getDeviceList = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "getDeviceList", []);
}

EpsonLWPrint.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.epsonLWPrint = new EpsonLWPrint();
  return window.plugins.epsonLWPrint;
};