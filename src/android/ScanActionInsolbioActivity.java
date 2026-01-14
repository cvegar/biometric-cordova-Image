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


public class ScanActionInsolbioActivity extends Activity {

    private String instructions;

    private static final String TAG = "ScanActionActivity";

    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    private String m_deviceName = "";
    private int eikon_step = 0;

    private String fingerprintBrand;
    private String hright = null;
    private String hleft = null;
    private int flagFakeFinger=0;
    private Reader m_reader;

    private String bioversion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_scan);

        Intent intent = getIntent();
        instructions = intent.getStringExtra("file");
        
        if (instructions != null) {
            instructions = instructions.substring(2, instructions.length() - 2);
        } else {
            instructions = "1";
        }

        fingerprintBrand = null;
        bioversion=Utils.fnVersion(this);

        if (!getIntent().getBooleanExtra("op", false)) {
            //called from oustystems from callactivity
            hright = Utils.getFlagExtraClean(getIntent(), "hright");
            hleft = Utils.getFlagExtraClean(getIntent(), "hleft");
            flagFakeFinger = Utils.getFlagExtraCleanInt(getIntent(), "flagff");

            Log.d(TAG, "ded: " + hright + hleft);
        }

        initializeEikon();
          
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    private void initializeEikon() {
        fingerprintBrand = "Eikon";

        if (eikon_step == 0) {
            Intent i = new Intent(ScanActionInsolbioActivity.this, GetReaderActivity.class);
            i.putExtra("device_name", m_deviceName);
            i.putExtra("parent_activity", "ScanActionActivity");
            startActivityForResult(i, eikon_step);
        } else if (eikon_step == 1) {
            Intent i = new Intent(ScanActionInsolbioActivity.this, CaptureFingerprintActivity.class);
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
                Log.i(TAG, "bien huella");
                Log.i(TAG, "bien serialnumber" + data.getStringExtra("serialnumber"));
                Log.i(TAG, "bien fingerprint_brand" + fingerprintBrand);
                Log.i(TAG, "bien bioversion" + bioversion);
                Log.i(TAG, "bien minutia" + data.getStringExtra("minutia"));
                Log.i(TAG, "bien error" + data.getStringExtra("error"));
                Log.i(TAG, "bien key"   + data.getStringExtra("finger").substring(0,10));
                Log.i(TAG, "bien huella" + data.getStringExtra("extra"));
                Log.i(TAG, "bien product"   + data.getStringExtra("product"));
                Log.i(TAG, "bien vendor" + data.getStringExtra("vendor"));
                Intent intent = new Intent();
                Log.i(TAG, "LLEGA Intent" );        

                CryptoUtil.loadKeys();
                Log.i(TAG, "LLEGA loadKeys" );   
                String encriptedBase64;
                String encriptedMinutia;
                String keyEncripted;
                String scorePADEncrypted;
                String imgBase64String;
                try {

                    encriptedBase64 = CryptoUtil.encrypt_(data.getStringExtra("finger"));
                    imgBase64String = CryptoUtil.encrypt_(data.getStringExtra("finger_png_b64"));
                    encriptedMinutia = CryptoUtil.encrypt_(data.getStringExtra("minutia"));
                    keyEncripted= CryptoUtil.encrypt_(data.getStringExtra("finger").substring(0,10));
                    scorePADEncrypted =CryptoUtil.encrypt_(data.getStringExtra("extra"));

                    intent.putExtra("huellab64", encriptedBase64);
                    Log.i(TAG, "bien huella");
                    //intent.putExtra("finger_png_b64", imgBase64String);
                    intent.putExtra("serialnumber", data.getStringExtra("serialnumber"));
                    Log.i(TAG, "bien serialnumber" + data.getStringExtra("serialnumber"));
                    intent.putExtra("fingerprint_brand", fingerprintBrand);
                    Log.i(TAG, "bien fingerprint_brand" + fingerprintBrand);
                    intent.putExtra("bioversion", bioversion);
                    Log.i(TAG, "bien bioversion" + bioversion);
                    intent.putExtra("minutia", encriptedMinutia);
                    Log.i(TAG, "bien minutia" + encriptedMinutia);
                    intent.putExtra("error",data.getStringExtra("error") );
                    Log.i(TAG, "bien error" + data.getStringExtra("error"));
                    intent.putExtra("key",keyEncripted);
                    Log.i(TAG, "bien key"   + keyEncripted);
                    intent.putExtra("extra",scorePADEncrypted);
                    Log.i(TAG, "bien huella" + scorePADEncrypted);
                    intent.putExtra("product",data.getStringExtra("product"));
                    Log.i(TAG, "bien product"   + data.getStringExtra("product"));
                    intent.putExtra("vendor",data.getStringExtra("vendor"));
                    Log.i(TAG, "bien vendor" + data.getStringExtra("vendor"));

/*                    Log.d(TAG, "finish OK serial=" + data.getStringExtra("serialnumber")
                    Log.d(TAG, "finish OK serial=" + data.getStringExtra("serialnumber")
                    + " brand=" + fingerprintBrand
                    + " bioversion=" + bioversion
                    + " huellab64_len=" + (encriptedBase64 == null ? 0 : encriptedBase64.length())
                    + " imagenPNG=" + (imgBase64String == null ? 0 : imgBase64String.length()));    
*/

                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    break;
                } catch (Exception e) {
                    Log.i(TAG, "CAE LOAD KEYS" );
                    intent.putExtra("serialnumber", data.getStringExtra("serialnumber"));
                    intent.putExtra("fingerprint_brand", fingerprintBrand);
                    intent.putExtra("bioversion", bioversion);
                    intent.putExtra("error",data.getStringExtra("error") );
                    intent.putExtra("product",data.getStringExtra("product"));
                    intent.putExtra("vendor",data.getStringExtra("vendor"));
                    intent.putExtra("debug", "SI");
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