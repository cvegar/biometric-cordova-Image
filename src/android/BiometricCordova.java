package biometric.entel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import biometric.entel.ScanActionCryptoActivity;
import biometric.entel.ScanActionInsolbioActivity;

public class BiometricCordova extends CordovaPlugin {

    private static final String TAG = "EntelBiometricPlugin-Cordova";
    private static final int REQ_SCAN_CRYPTO   = 11001;
    private static final int REQ_SCAN_INSOLBIO = 11002;

    // Para soportar múltiples acciones sin pisar callback
    private final Map<Integer, CallbackContext> pendingCallbacks = new HashMap<>();

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        try {
            JSONObject options = (args != null && args.length() > 0) ? args.getJSONObject(0) : new JSONObject();

            if ("scanCrypto".equals(action)) {
                return launchScanCrypto(options, callbackContext);
            }

            if ("scanInsolbio".equals(action)) {
                return launchScanInsolbio(options, callbackContext);
            }

            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;

        } catch (JSONException e) {
            callbackContext.error("JSON error: " + e.getMessage());
            return true;
        } catch (Exception e) {
            callbackContext.error("Unexpected error: " + e.getMessage());
            return true;
        }
    }

    private boolean launchScanCrypto(JSONObject options, CallbackContext callbackContext) {
        // guarda callback
        pendingCallbacks.put(REQ_SCAN_CRYPTO, callbackContext);

        // Valores “limpios”
        String hright = optFirstNonEmpty(options, "hright", "rightFingerCode", "right_finger");
        String hleft  = optFirstNonEmpty(options, "hleft",  "leftFingerCode",  "left_finger");
        boolean op = options.optBoolean("op", false);

        Intent intent = new Intent(cordova.getActivity(), ScanActionCryptoActivity.class);

        // Si op=false, tu Activity lee hright/hleft
        if (!op) {
            intent.putExtra("hright", normalizeToBracketedString(hright));
            intent.putExtra("hleft",  normalizeToBracketedString(hleft));
        }

        Log.d(TAG, "Launching ScanActionCryptoActivity. op=" + op);
        cordova.startActivityForResult(this, intent, REQ_SCAN_CRYPTO);
        return true;
    }

    private boolean launchScanInsolbio(JSONObject options, CallbackContext callbackContext) {
        // guarda callback
        pendingCallbacks.put(REQ_SCAN_INSOLBIO, callbackContext);

        // --- Tus 3 inputs (define los nombres que quieras) ---
        // Ejemplo: p1, p2, p3 (puedes renombrarlos a lo que tu Activity espere)
        String hright = optFirstNonEmpty(options, "hright", "param1", "documentType");
        String hleft = optFirstNonEmpty(options, "hleft", "param2", "documentNumber");
        String flagFakeFinger = optFirstNonEmpty(options, "flagff", "param3", "fingerCode");
        boolean op = options.optBoolean("op", false);

        Intent intent = new Intent(cordova.getActivity(), ScanActionInsolbioActivity.class);

        if (!op) {
            intent.putExtra("hright", normalizeToBracketedString(hright));
            intent.putExtra("hleft",  normalizeToBracketedString(hleft));
        }
        intent.putExtra("flagff", flagFakeFinger);

        // Si quieres pasar más cosas opcionales, puedes hacerlo aquí también
        // intent.putExtra("op", options.optBoolean("op", false));

        Log.d(TAG, "Launching ScanActionInsolbioActivity with hright=" + hright + ", hleft=" + hleft + ", flagff=" + flagFakeFinger);
        cordova.startActivityForResult(this, intent, REQ_SCAN_INSOLBIO);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CallbackContext cb = pendingCallbacks.remove(requestCode);
        if (cb == null) {
            Log.w(TAG, "No pending callback for requestCode=" + requestCode);
            return;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                // ✅ Devuelve TODOS los extras como JSON (ideal para “más outputs”)
                JSONObject resp = intentExtrasToJson(data);
                cb.success(resp);
            } catch (Exception e) {
                cb.error("ERROR building response: " + e.getMessage());
            }
            return;
        }

        // Si tu activity devuelve mensaje de error en extras, lo puedes devolver aquí
        if (data != null && data.getExtras() != null && data.getExtras().containsKey("error")) {
            cb.error(String.valueOf(data.getExtras().get("error")));
        } else {
            cb.error("CANCEL");
        }
    }

    // ---------------- helpers ----------------

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Mantengo tu formato ["valor"] por compatibilidad
    private static String normalizeToBracketedString(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.startsWith("[") && v.endsWith("]")) return v;
        v = v.replace("\"", "\\\"");
        return "[\"" + v + "\"]";
    }

    private static String optFirstNonEmpty(JSONObject obj, String... keys) {
        for (String k : keys) {
            if (k == null) continue;
            String v = obj.optString(k, null);
            if (!isNullOrEmpty(v)) return v;
        }
        return null;
    }

    private static JSONObject intentExtrasToJson(Intent data) throws JSONException {
        JSONObject resp = new JSONObject();
        Bundle extras = data.getExtras();
        if (extras == null) return resp;

        for (String key : extras.keySet()) {
            Object val = extras.get(key);

            if (val == null) {
                resp.put(key, JSONObject.NULL);
            } else if (val instanceof Integer || val instanceof Long || val instanceof Double
                    || val instanceof Boolean || val instanceof String) {
                resp.put(key, val);
            } else {
                // cualquier cosa rara -> String
                resp.put(key, String.valueOf(val));
            }
        }
        return resp;
    }
}


 /*private static final String TAG = "EntelBiometricPlugin-Cordova";
    private static final int REQ_SCAN_CRYPTO = 11001;

    private CallbackContext pendingCallback;

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
        if (!"scanCrypto".equals(action)) {
            callbackContext.error("\"" + action + "\" is not a recognized action.");
            return false;
        }

        pendingCallback = callbackContext;

        try {
            JSONObject options = (args != null && args.length() > 0) ? args.getJSONObject(0) : new JSONObject();

            // Valores “limpios” (los normalizamos para ScanActionCryptoActivity)
            String hright = optFirstNonEmpty(options, "hright", "rightFingerCode", "right_finger");
            String hleft  = optFirstNonEmpty(options, "hleft", "leftFingerCode", "left_finger");
            boolean op = options.optBoolean("op", false);

            Context appCtx = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(appCtx, ScanActionCryptoActivity.class);

            // IMPORTANT: ScanActionCryptoActivity hace substring(2, len-2) sobre "file"
            // Así que lo enviamos como ["valor"] para que sea seguro.
            //intent.putExtra("file", normalizeToBracketedString(instructions));
            

            if (!op) {
                intent.putExtra("hright", normalizeToBracketedString(hright));
                intent.putExtra("hleft", normalizeToBracketedString(hleft));
            }

            Log.d(TAG, "Launching ScanActionCryptoActivity. op=" + op);

            cordova.startActivityForResult(this, intent, REQ_SCAN_CRYPTO);
            return true;

        } catch (JSONException e) {
            callbackContext.error("JSON error: " + e.getMessage());
            return true;
        } catch (Exception e) {
            callbackContext.error("Unexpected error: " + e.getMessage());
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQ_SCAN_CRYPTO) {
            return;
        }

        if (pendingCallback == null) {
            Log.w(TAG, "No pending callback (maybe already resolved/rejected).");
            return;
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                JSONObject resp = new JSONObject();
                resp.put("huellab64", data.getStringExtra("huellab64"));
                resp.put("serialnumber", data.getStringExtra("serialnumber"));
                resp.put("fingerprint_brand", data.getStringExtra("fingerprint_brand"));
                resp.put("bioversion", data.getStringExtra("bioversion"));

                pendingCallback.success(resp);
            } catch (JSONException e) {
                pendingCallback.error("ERROR: " + e.getMessage());
            } finally {
                pendingCallback = null;
            }
        } else {
            pendingCallback.error("CANCEL");
            pendingCallback = null;
        }
    }

    // ---------------- helpers ----------------

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }*/

    /**
     * ScanActionCryptoActivity espera strings “tipo array” para luego limpiarlos:
     * - file: substring(2, len-2)
     * - hright/hleft: replace([, ], ")
     * Si ya te llega en formato [""], lo dejamos tal cual.
     */
    /*private static String normalizeToBracketedString(String value) {
        if (value == null) return null;

        String v = value.trim();
        if (v.startsWith("[") && v.endsWith("]")) {
            return v;
        }

        // Escapamos comillas por si el valor las trae
        v = v.replace("\"", "\\\"");
        return "[\"" + v + "\"]";
    }

    private static String optFirstNonEmpty(JSONObject obj, String... keys) {
        for (String k : keys) {
            if (k == null) continue;
            String v = obj.optString(k, null);
            if (!isNullOrEmpty(v)) return v;
        }
        return null;
    }

*/

