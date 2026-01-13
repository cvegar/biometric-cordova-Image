package biometric.entel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.outsystemsenterprise.entel.PEMayorista.R;
//import biometric.entel.R;
import biometric.entel.util.Globals;

public class GetReaderActivity extends Activity {
    private final String TAG = "GetReaderActivity";

    private Button m_back;
    private String m_deviceName = "";
    private String parent_activity;

    private ListView m_readers;
    private ReaderCollection readers;
    private final Bundle savedInstanceState = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parent_activity = getIntent().getExtras().getString("parent_activity");

        if (parent_activity == null) {
            Log.i(TAG, "GetReaderActivity called without parent activity");
            finish();
        } else {

            setContentView(R.layout.activity_get_list);

            m_deviceName = getIntent().getExtras().getString("device_name");
            m_back = findViewById(R.id.back);
            m_back.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            Log.i(TAG, "device name: " + m_deviceName);

            // initialize dp sdk
            try {
                Context applContext = getApplicationContext();
                readers = Globals.getInstance().getReaders(applContext);

                Log.i(TAG, "GetReaders");
            } catch (UareUException e) {
                onBackPressed();
            } catch (Exception e) {
                Log.i(TAG, e.getMessage());
                Toast.makeText(this, "No se detectó el huellero, verifique que se concedieron los permisos", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            int nSize = readers.size();
            if (nSize > 1) {
                String[] values = null;
                values = new String[nSize];
                for (int nCount = 0; nCount < nSize; nCount++) {
                    values[nCount] = readers.get(nCount).GetDescription().name;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
                m_readers = findViewById(R.id.list);
                m_readers.setAdapter(adapter);
                m_readers.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent i = new Intent();
                        i.putExtra("device_name", readers.get(position).GetDescription().name);
                        setResult(Activity.RESULT_OK, i);

                        InitDevice(position);

                        finish();
                    }
                });
            } else {
                Intent i = new Intent();
                i.putExtra("device_name", (nSize == 0 ? "" : readers.get(0).GetDescription().name));

                if (getIntent() != null &&
                        getIntent().getAction() != null &&
                        getIntent().getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                    InitDevice(0);
                }
                setResult(Activity.RESULT_OK, i);

                finish();
            }

        }

    }

    private void InitDevice(int position) {

        try {
            byte[] params = new byte[1];
            params[0] = (byte) 1;
            readers.get(position).SetParameter(Reader.ParamId.DPFPDD_PARMID_PAD_ENABLE, params);
            readers.get(position).Open(Reader.Priority.COOPERATIVE);
            readers.get(position).Close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("device_name", m_deviceName);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        onCreate(savedInstanceState);
        super.onConfigurationChanged(newConfig);
    }
}
