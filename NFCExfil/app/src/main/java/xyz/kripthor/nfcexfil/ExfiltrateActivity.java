package xyz.kripthor.nfcexfil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.nfc.NfcAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class ExfiltrateActivity extends AppCompatActivity {

    private static int BARSIZE = 8;

    private NfcExfiltrateThread nfcex;
    private int gpos = 0;
    private ImageView iview;
    private Bitmap bitmap;
    private Canvas canvas;
    private SeekBar sb1,sb2;
    private EditText et3;
    private Button b;
    private TextView tv1,tv2;
    private int nfcModes[] = {1,2,4,8,16,128,256};
    private int currentMode = 0;
    private String LOG_TAG = "nfcexfil - "+this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sb1 = this.findViewById(R.id.seekBar);
        sb2 = this.findViewById(R.id.seekBar2);
        et3 = this.findViewById(R.id.editText3);
        tv1 = this.findViewById(R.id.textView);
        tv2 = this.findViewById(R.id.textView2);
        b = findViewById(R.id.button);
        sb2.setProgress(Config.TRANSMIT_MS);
        sb1.setProgress(Config.SLEEP_MS);
        int cycle = sb1.getProgress();
        int duration = sb2.getProgress();
        tv1.setText("Switch cycle:  "+cycle+"ms");
        tv2.setText("Bit duration:  "+duration+"ms");
        NfcAdapter.getDefaultAdapter(this.getBaseContext()).enableReaderMode(this, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);


        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                int cycle = sb1.getProgress();
                tv1.setText("Switch cycle:  "+cycle+"ms");

            }
        });

        sb2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                int duration = sb2.getProgress();
                tv2.setText("Bit duration:  "+duration+"ms");

            }
        });

    }

    public void cycleTestMode(View v) {
        iview= (ImageView) findViewById(R.id.imageView);
        bitmap = Bitmap.createBitmap(iview.getWidth(),iview.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        Log.d(LOG_TAG,"NFCMODE "+Config.NFCmodeToString(nfcModes[currentMode]));
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(60);
        canvas.drawText("NFC mode: "+Config.NFCmodeToString(nfcModes[currentMode]),50,iview.getHeight()/2,paint);
        iview.setImageBitmap(bitmap);
        iview.invalidate();
        Config.READERMODE = nfcModes[currentMode];
        NfcAdapter.getDefaultAdapter(this.getBaseContext()).enableReaderMode(this, null, Config.READERMODE, null);

        if (++currentMode>=nfcModes.length) currentMode = 0;

    }

    public void doNFCExfilt(View v) {
        iview= (ImageView) findViewById(R.id.imageView);
        bitmap = Bitmap.createBitmap(iview.getWidth(),iview.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        gpos = 0;
        try {

            int cycle = sb1.getProgress();

            int duration = sb2.getProgress();

            String data = et3.getText().toString();

            if (this.nfcex != null) {
                if (!nfcex.isLiving()) {
                    nfcex = new NfcExfiltrateThread(this, NfcAdapter.getDefaultAdapter(this.getBaseContext()),cycle,duration,data);
                    nfcex.start();
                    b.setText("Stop sending");
                } else {
                    nfcex.die();
                    b.setText("Send data");
                }
            } else {
                nfcex = new NfcExfiltrateThread(this, NfcAdapter.getDefaultAdapter(this.getBaseContext()),cycle,duration,data);
                nfcex.start();
                b.setText("Stop sending");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    protected void updateGraphics(int bit) {
        Paint paint = new Paint();
        if (bit > 0) paint.setColor(Color.BLACK);
        else paint.setColor(Color.LTGRAY);
        canvas.drawRect(gpos, 0, gpos+BARSIZE,canvas.getHeight()-40, paint);
        gpos+=BARSIZE;
        if (gpos > canvas.getWidth()) {
            gpos = 0;
            bitmap = Bitmap.createBitmap(iview.getWidth(),iview.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
        iview.setImageBitmap(bitmap);
        iview.invalidate();

    }

    protected void updateGraphics(String cchar) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        canvas.drawText(cchar,gpos,canvas.getHeight()-1,paint);
        iview.setImageBitmap(bitmap);
        iview.invalidate();

    }

}
