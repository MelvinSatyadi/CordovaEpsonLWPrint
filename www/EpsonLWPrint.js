var exec = require('cordova/exec');

function EpsonLWPrint() { }

EpsonLWPrint.prototype.startDiscover = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "startDiscover", []);
}

EpsonLWPrint.prototype.getDeviceList = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "getDeviceList", []);
}

EpsonLWPrint.prototype.checkBT = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "checkPermissions", []);
}

EpsonLWPrint.prototype.setPrinterInfo = function(fnSuccess, fnError,printerInfoJSON){
  exec(fnSuccess, fnError, "EpsonLWPrint", "setPrinterInfo",[printerInfoJSON]);
}

EpsonLWPrint.prototype.printImage = function(fnSuccess, fnError, imageBase64){
  exec(fnSuccess, fnError, "EpsonLWPrint", "printImage",[imageBase64]);
}

EpsonLWPrint.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.epsonLWPrint = new EpsonLWPrint();
  return window.plugins.epsonLWPrint;
};

cordova.addConstructor(EpsonLWPrint.install);