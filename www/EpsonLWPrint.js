var exec = require('cordova/exec');

var EpsonLWPrint = {
  startDiscover: function(fnSuccess, fnError){
    exec(fnSuccess, fnError, "EpsonLWPrint", "startDiscover", []);
  },
  getDeviceList: function(fnSuccess, fnError){
    exec(fnSuccess, fnError, "EpsonLWPrint", "getDeviceList", []);
  }
};

module.exports = ToastyPlugin;