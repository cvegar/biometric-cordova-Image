package biometric.entel.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

//import biometrico.entel.pe.BuildConfig;

public class Utils {

    private static final String LOG_TAG = "Utils";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static String formatWsqToBase64(byte[] wsq) {
        if (wsq == null) {
            return null;
        }
        int idx;
        byte[] data = new byte[(wsq.length + 2)];
        System.arraycopy(wsq, 0, data, 0, wsq.length);
        byte[] dest = new byte[((data.length / 3) * 4)];
        int sidx = 0;
        int didx = 0;
        while (sidx < wsq.length) {
            dest[didx] = (byte) ((data[sidx] >>> 2) & 63);
            dest[didx + 1] = (byte) (((data[sidx + 1] >>> 4) & 15) | ((data[sidx] << 4) & 63));
            dest[didx + 2] = (byte) (((data[sidx + 2] >>> 6) & 3) | ((data[sidx + 1] << 2) & 63));
            dest[didx + 3] = (byte) (data[sidx + 2] & 63);
            sidx += 3;
            didx += 4;
        }
        for (idx = 0; idx < dest.length; idx++) {
            if (dest[idx] < (byte) 26) {
                dest[idx] = (byte) (dest[idx] + 65);
            } else if (dest[idx] < (byte) 52) {
                dest[idx] = (byte) ((dest[idx] + 97) - 26);
            } else if (dest[idx] < (byte) 62) {
                dest[idx] = (byte) ((dest[idx] + 48) - 52);
            } else if (dest[idx] < (byte) 63) {
                dest[idx] = (byte) 43;
            } else {
                dest[idx] = (byte) 47;
            }
        }
        for (idx = dest.length - 1; idx > (wsq.length * 4) / 3; idx--) {
            dest[idx] = (byte) 61;
        }
        return new String(dest);
    }

/*
    public static String cualBuild() {
        String respuesta = "NIGUNO";

        if (BuildConfig.FLAVOR.equals("dev")) {
            respuesta = "DESARROLLO";
        } else if (BuildConfig.FLAVOR.equals("tst")) {
            respuesta = "TEST";
        } else if (BuildConfig.FLAVOR.equals("pp")) {
            respuesta = "PRE PRODUCCION";
        } else if (BuildConfig.FLAVOR.equals("prod")) {
            respuesta = "PRODUCCION";
        }

        return respuesta;
    }
*/
    public static String fnVersion(Context poContext) {
        String lsVersion = "";
        PackageInfo loPackageInfo = null;
        try {
            loPackageInfo = poContext.getPackageManager().getPackageInfo(
                    poContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.v(LOG_TAG, "Verificar el manifest: " + e.getMessage());
            return "ERROR VERSION";
        }

        lsVersion = loPackageInfo.versionName;

        return lsVersion;
    }

    public static String getFingerName(String idDedo) {
        String name;
        switch (idDedo) {
            case "01":
                name = "PULGAR DERECHO";
                break;
            case "02":
                name = "ÍNDICE DERECHO";
                break;
            case "03":
                name = "MEDIO DERECHO";
                break;
            case "04":
                name = "ANULAR DERECHO";
                break;
            case "05":
                name = "MEÑIQUE DERECHO";
                break;
            case "06":
                name = "PULGAR IZQUIERDO";
                break;
            case "07":
                name = "ÍNDICE IZQUIERDO";
                break;
            case "08":
                name = "MEDIO IZQUIERDO";
                break;
            case "09":
                name = "ANULAR IZQUIERDO";
                break;
            case "10":
                name = "MEÑIQUE IZQUIERDO";
                break;
            default:
                name = "ÍNDICE DERECHO";
        }

        return name;
    }

     //obtener flag limpio desde el intent
 
    public static String getFlagExtraClean(Intent intent, String key) {
        try {
            String raw = intent.getStringExtra(key);
            if (raw == null) return "0";
            return raw.replace("[", "").replace("]", "").replace("\"", "");
        } catch (Exception e) {
            return "0";
        }
    }

    //obtener flag limpio desde el intent como entero
    public static int getFlagExtraCleanInt(Intent intent, String key) {
        try {
            String raw = intent.getStringExtra(key);
            if (raw == null) return 0;
            String clean = raw.replace("[", "").replace("]", "").replace("\"", "");
            return Integer.parseInt(clean);
        } catch (Exception e) {
            return 0;
        }
    }
}
