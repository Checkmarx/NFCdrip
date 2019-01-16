package xyz.kripthor.nfcexfil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.nfc.NfcAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class ReceiverActivity extends AppCompatActivity {

    private String LOG_TAG = "nfcexfil - "+this.getClass().getName();
    private ImageView iview;
    private Bitmap bitmap;
    private Canvas canvas;
    private SeekBar sb;
    private TextView tv;
    private TextView statustv;
    private int gpos,cpos;
    private Button b;
    private ReceiverThread rt;
    private boolean stopUpdates = false;
    private int current = Color.GREEN;
    private int lastBit = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);
        sb = this.findViewById(R.id.seekBar);
        tv = this.findViewById(R.id.textView);
        statustv = this.findViewById(R.id.textView4);
        b = this.findViewById(R.id.button);
        iview= (ImageView) findViewById(R.id.imageView);
        sb.setProgress(Config.TRANSMIT_MS);
        int duration = sb.getProgress();
        tv.setText("Bit duration:  "+duration+"ms");

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                int duration = sb.getProgress();
                tv.setText("Bit duration:  "+duration+"ms");

            }
        });

        try {
            NfcAdapter.getDefaultAdapter(this.getBaseContext()).enableReaderMode(this, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, Config.MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Config.MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    this.finishActivity(0);
                }
                return;
            }
        }
    }



    public void doReceive(View v) {

        try {

            int duration = sb.getProgress();

            if (this.rt != null) {
                if (!rt.isAlive()) {
                    stopUpdates = false;
                    gpos = 0;
                    cpos = 0;
                    cleanGraphics();
                    rt = new ReceiverThread(this,duration);
                    rt.start();
                    b.setText("Stop Receiving");
                } else {
                    stopUpdates = true;
                    b.setText("Start Receiving");
                }
            } else {
                stopUpdates = false;
                gpos = 0;
                cpos = 0;
                cleanGraphics();
                rt = new ReceiverThread(this,duration);
                rt.start();
                b.setText("Stop Receiving");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updateGraphics(short samples[]) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(current);
        if (current == Color.GREEN) current = Color.RED;
        else current = Color.GREEN;

        int lastY0 = Integer.MAX_VALUE;
        int step = 40;

        int lastY1 = Integer.MAX_VALUE;
        double v1[] = rt.avg;
        int bitguess = rt.currentBit;

        int y1 = (int)((v1[0]/20000 )*(canvas.getHeight()/2) +  (canvas.getHeight()/2));
        int y2 = (int)((v1[2]/50 )*(canvas.getHeight()/2) +  (canvas.getHeight()/2));
        canvas.drawRect(gpos, canvas.getHeight()/2, gpos + samples.length/step/2, y1, paint);
        canvas.drawRect(gpos+samples.length/step/2, canvas.getHeight()/2, gpos + samples.length/step, y2, paint);

        if (gpos > lastBit + 20) {
            int oldc = paint.getColor();
            paint.setColor(Color.BLACK);
            paint.setTextSize(30);
            canvas.drawText(""+bitguess, gpos-6, canvas.getHeight()/2-4, paint);
            lastBit = gpos;
            paint.setColor(oldc);
        }

       //Log.d(LOG_TAG,"autocorr: "+v1[0] + " pos: "+v1[2]+" adj:"+y1);


        for (int i=0;i < samples.length-1;i+=step) {

            short v0 = samples[i];

            int y0 = (int) ((v0 * 1.0 / (Short.MAX_VALUE*2)) * (canvas.getHeight() / 2) + (canvas.getHeight() / 4));
            if (lastY0 == Integer.MAX_VALUE) {
                canvas.drawLine(gpos, y0, gpos + 1, y0, paint);
            } else {
                canvas.drawLine(gpos, lastY0, gpos + 1, y0, paint);
            }
            lastY0 = y0;

            if (gpos >= canvas.getWidth()) {
                if (stopUpdates) {
                    rt.die();
                    return;
                }
                gpos = 0;
                lastBit = 0;
                //paint.setColor(Color.WHITE);
                //canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight() / 2,paint);
                cleanGraphics();
            }
            gpos++;



        }

        iview.setImageBitmap(bitmap);
        iview.invalidate();

    }

    private void cleanGraphics() {
        bitmap = Bitmap.createBitmap(iview.getWidth(), iview.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }


    public void updateStatus(String dataString, String statusString) {
        statustv.setText("Status: "+statusString+"\nData: "+dataString);
    }
}
