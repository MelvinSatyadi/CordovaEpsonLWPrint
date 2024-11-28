var exec = require('cordova/exec');

function EpsonLWPrint() { }

EpsonLWPrint.prototype.startDiscover = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "startDiscover", []);
}

EpsonLWPrint.prototype.getDeviceList = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "getDeviceList", []);
}

EpsonLWPrint.prototype.checkBT = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "checkBT", []);
}

EpsonLWPrint.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.epsonLWPrint = new EpsonLWPrint();
  return window.plugins.epsonLWPrint;
};

cordova.addConstructor(EpsonLWPrint.install);