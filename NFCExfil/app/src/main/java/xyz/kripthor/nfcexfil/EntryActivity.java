package xyz.kripthor.nfcexfil;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        try {
        NfcAdapter.getDefaultAdapter(this.getBaseContext()).enableReaderMode(this, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        for (int i=0;i< HammingCodes.hammingCodes.length;i++) {
            int c = HammingCodes.hammingCodes[i];
            //Log.d("Ham", i+"\t\thex: "+Integer.toHexString(c)+"\tb: "+Integer.toBinaryString(c));

        }

    }

    public void startEmitter(View v) {

        startActivity(new Intent(this, ExfiltrateActivity.class));

    }

    public void startReceiver(View v) {

        startActivity(new Intent(this, ReceiverActivity.class));

    }

    public void startConfig(View v) {

        startActivity(new Intent(this, ConfigActivity.class));

    }

}
