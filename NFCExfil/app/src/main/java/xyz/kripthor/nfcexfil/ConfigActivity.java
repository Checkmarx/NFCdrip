package xyz.kripthor.nfcexfil;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class ConfigActivity extends AppCompatActivity {

    EditText et1,et2,et3,et4,et5,et6,et7,et8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        et1 = this.findViewById(R.id.editText01);
        et2 = this.findViewById(R.id.editText02);
        et3 = this.findViewById(R.id.editText03);
        et4 = this.findViewById(R.id.editText04);
        et5 = this.findViewById(R.id.editText05);
        et6 = this.findViewById(R.id.editText06);
        et7 = this.findViewById(R.id.editText07);
        et8 = this.findViewById(R.id.editText08);
    }

    @Override
    protected void onResume() {
        super.onResume();

        et1.setText(""+Config.PREAMBLEINTERVAL);
        et2.setText(""+Config.PREAMBLE_1N);
        et3.setText(""+Config.PREAMBLE_0N);
        et4.setText(""+Config.TRAILER_0N);
        et5.setText(""+Config.TRANSMIT_MS);
        et6.setText(""+Config.SLEEP_MS);
        et7.setText(""+Config.SAMPLES_PER_SECOND);
        et8.setText(""+Config.CHUNKS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Config.PREAMBLEINTERVAL = limit(Integer.parseInt(et1.getText().toString()),8,1);
        Config.PREAMBLE_1N = limit(Integer.parseInt(et2.getText().toString()),24,4);
        Config.PREAMBLE_0N = limit(Integer.parseInt(et3.getText().toString()),24,1);
        Config.TRAILER_0N = limit(Integer.parseInt(et4.getText().toString()),24,0);
        Config.TRANSMIT_MS = limit(Integer.parseInt(et5.getText().toString()),200,20);
        Config.SLEEP_MS = limit(Integer.parseInt(et6.getText().toString()),10,0);
        Config.SAMPLES_PER_SECOND = limit(Integer.parseInt(et7.getText().toString()),44100,8000);
        Config.CHUNKS = limit(Integer.parseInt(et8.getText().toString()),11,1);
    }

    private int limit(int n, int max, int min) {
        if (n > max) return max;
        if (n < min) return min;
        return n;
    }

}
