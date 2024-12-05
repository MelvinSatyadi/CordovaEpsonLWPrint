var exec = require('cordova/exec');

function EpsonLWPrint() { }

EpsonLWPrint.prototype.initialize = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "initialize", []);
}

EpsonLWPrint.prototype.startDiscover = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "startDiscover", []);
}

EpsonLWPrint.prototype.stopDiscover = function (fnSuccess, fnError) {
  exec(fnSuccess, fnError, "EpsonLWPrint", "stopDiscover", []);
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

EpsonLWPrint.prototype.printText = function(fnSuccess, fnError, textToPrint){
  exec(fnSuccess, fnError, "EpsonLWPrint", "printText",[textToPrint]);
}

EpsonLWPrint.prototype.debugLog = function(fnSuccess, fnError, debugString){
  exec(fnSuccess, fnError, "EpsonLWPrint", "debugLog",[debugString]);
}

EpsonLWPrint.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.epsonLWPrint = new EpsonLWPrint();
  return window.plugins.epsonLWPrint;
};

cordova.addConstructor(EpsonLWPrint.install);