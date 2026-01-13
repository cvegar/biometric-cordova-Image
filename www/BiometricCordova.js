var exec = require("cordova/exec");

function asBracketArrayString(value) {
  if (value === null || value === undefined) return '[""]';
  var s = String(value).trim().replace(/"/g, '\\"');
  // tu nativo hace replace de [,],"
  return '["' + s + '"]';
}

var BiometricCordova = {
  // Firma EXACTA a como lo llamas en OutSystems:
  // scanCrypto("01","07", successCb, errorCb)
  scanCrypto: function (rightFingerCode, leftFingerCode, successCallback, errorCallback) {
    var options = {
      hright: asBracketArrayString(rightFingerCode),
      hleft: asBracketArrayString(leftFingerCode)
      // file eliminado
      // op eliminado
    };

    exec(successCallback, errorCallback, "BiometricCordova", "scanCrypto", [options]);
  },
  // Firma EXACTA a como lo llamas en OutSystems:
  // scanInsolbio("01","07", true, successCb, errorCb)
  scanInsolbio: function (rightFingerCode, leftFingerCode, flagFakeFinger, successCallback, errorCallback) {
    var options = {
      hright: asBracketArrayString(rightFingerCode),
      hleft: asBracketArrayString(leftFingerCode),
      flagff: flagFakeFinger ? "1" : "0"
      // op eliminado
    };
    exec(successCallback, errorCallback, "BiometricCordova", "scanInsolbio", [options]);
  }
};

module.exports = BiometricCordova;

// opcional: alias window.plugins por si tu app lo busca así
document.addEventListener("deviceready", function () {
  window.plugins = window.plugins || {};
  window.plugins.BiometricCordova = BiometricCordova;
}, false);
