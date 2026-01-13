package biometric.entel;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.rsa.CryptoUtil;

import SecuGen.FDxSDKPro.JSGFPLib;
import SecuGen.FDxSDKPro.SGFDxDeviceName;
import SecuGen.FDxSDKPro.SGFDxErrorCode;
import android.os.Build;
import android.content.Context;
//import biometric.entel.R;
import biometric.entel.util.Globals;
import biometric.entel.util.Utils;
import com.outsystemsenterprise.entel.PEMayorista.R;

public class ScanActionCryptoActivity extends Activity {

    private String instructions;

    private static final String TAG = "ScanActionActivity";

    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    private String m_deviceName = "";
    private int eikon_step = 0;
    private JSGFPLib sgfplib;
    private String fingerprintBrand;
    private String hright = null;
    private String hleft = null;
    private Reader m_reader;
    private int flagFakeFinger = 0;

    private String bioversion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_scan);

    instructions = "1";
    fingerprintBrand = null;
    bioversion = Utils.fnVersion(this);

    Intent intent = getIntent();
    Bundle extras = (intent != null) ? intent.getExtras() : null;

    // LOG: ver qué está llegando
    if (extras == null) {
        Log.e(TAG, "Extras is NULL. Intent=" + intent);
    } else {
        for (String k : extras.keySet()) {
            Log.d(TAG, "EXTRA " + k + " = " + extras.get(k));
        }
    }

    boolean op = (intent != null) && intent.getBooleanExtra("op", false);
    Log.d(TAG, "op=" + op);

    if (!op) {
        String rawRight = (extras != null) ? extras.getString("hright") : null;
        String rawLeft  = (extras != null) ? extras.getString("hleft")  : null;
        flagFakeFinger = Utils.getFlagExtraCleanInt(getIntent(), "flagff");
        // Fallback por si Cordova los puso como StringExtra
        if (rawRight == null && intent != null) rawRight = intent.getStringExtra("hright");
        if (rawLeft  == null && intent != null) rawLeft  = intent.getStringExtra("hleft");

        if (rawRight == null || rawLeft == null) {
            Log.e(TAG, "Missing hright/hleft. rawRight=" + rawRight + " rawLeft=" + rawLeft);

            // Evita crash: retorna cancelado (si tu plugin maneja onActivityResult)
            Intent data = new Intent();
            data.putExtra("error", "Missing hright/hleft extras");
            setResult(RESULT_CANCELED, data);
            finish();
            return;
        }

        hright = clean(rawRight);
        hleft  = clean(rawLeft);
        Log.d(TAG, "ded: " + hright + " " + hleft);
    }

    initializeEikon();
}

private String clean(String s) {
    if (s == null) return "";
    return s.replace("[", "").replace("]", "").replace("\"", "").trim();
}



    private void initializeEikon() {
        fingerprintBrand = "Eikon";

        if (eikon_step == 0) {
            Intent i = new Intent(ScanActionCryptoActivity.this, GetReaderActivity.class);
            i.putExtra("device_name", m_deviceName);
            i.putExtra("parent_activity", "ScanActionActivity");
            startActivityForResult(i, eikon_step);
        } else if (eikon_step == 1) {
            Intent i = new Intent(ScanActionCryptoActivity.this, CaptureFingerprintActivity.class);
            i.putExtra("device_name", m_deviceName);
            i.putExtra("instructions", instructions);
            i.putExtra("right_finger", hright);
            i.putExtra("left_finger", hleft);
            i.putExtra("flag_ff", flagFakeFinger);
            startActivityForResult(i, eikon_step);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            Toast.makeText(getApplicationContext(), "No data on activity result", Toast.LENGTH_SHORT).show();
            return;
        }

        Globals.ClearLastBitmap();
        m_deviceName = (String) data.getExtras().get("device_name");


        switch (requestCode) {
            case 0:

                if ((m_deviceName != null) && !m_deviceName.isEmpty()) {
                    try {
                        Context applContext = getApplicationContext();
                        m_reader = Globals.getInstance().getReader(m_deviceName, applContext);

                        {
                            PendingIntent mPermissionIntent;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
                            } else {
                                mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT);
                            }
                            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                            //applContext.registerReceiver(mUsbReceiver, filter);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                // Para Android 13+ (API 33), usa el flag para mejorar la seguridad
                                applContext.registerReceiver(mUsbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                            } else {
                                // Para Android 9 a 12, registra el BroadcastReceiver sin el flag
                                applContext.registerReceiver(mUsbReceiver, filter);
                            }

                            if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName)) {
                                //CheckDevice();
                                eikon_step = 1;
                                initializeEikon();
                            }
                        }
                    } catch (UareUException e1) {
                        Toast.makeText(getApplicationContext(), e1.toString(), Toast.LENGTH_SHORT).show();
                    } catch (DPFPDDUsbException e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(getApplicationContext(), "El lector no ha sido detectado o no se ha otorgado los permisos USB,conectar el lector e intentar la operación nuevamente.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                break;
            case 1:
                Log.i(TAG, "ON RESULT OF SCAN");

                Intent intent = new Intent();

                CryptoUtil.loadKeys();
                String encriptedBase64;
                String keyEncripted;
                String scorePADEncrypted;
                try {

                    encriptedBase64 = CryptoUtil.encrypt_(data.getStringExtra("finger"));
                    keyEncripted= CryptoUtil.encrypt_(data.getStringExtra("finger").substring(0,10));
                    scorePADEncrypted =CryptoUtil.encrypt_(data.getStringExtra("extra"));

                    intent.putExtra("huellab64", encriptedBase64);
                    intent.putExtra("serialnumber", data.getStringExtra("serialnumber"));
                    intent.putExtra("fingerprint_brand", fingerprintBrand);
                    intent.putExtra("bioversion", bioversion);
                    intent.putExtra("error", data.getStringExtra("error"));
                    intent.putExtra("key",keyEncripted);
                    intent.putExtra("extra",scorePADEncrypted);
                    intent.putExtra("product",data.getStringExtra("product"));
                    intent.putExtra("vendor",data.getStringExtra("vendor"));
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    break;
                } catch (Exception e) {
                   intent.putExtra("serialnumber", data.getStringExtra("serialnumber"));
                    intent.putExtra("fingerprint_brand", fingerprintBrand);
                    intent.putExtra("bioversion", bioversion);
                    intent.putExtra("error", data.getStringExtra("error"));
                    intent.putExtra("product",data.getStringExtra("product"));
                    intent.putExtra("vendor",data.getStringExtra("vendor"));
                    setResult(Activity.RESULT_CANCELED, intent);
                    finish();
                    break;
                }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                            //CheckDevice();
                        }
                    } else {
                        //setButtonsEnabled(false);
                    }
                }
            }
        }
    };
}
