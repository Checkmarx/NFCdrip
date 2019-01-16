package xyz.kripthor.nfcexfil;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;

/**
 * Created by kripthor on 23-11-2017.
 */

public class ReceiverThread extends Thread implements IAudioReceiver{

    private String LOG_TAG = "nfcexfil - "+this.getClass().getName();
    private boolean alive;

    private AudioCapturer audioCapturer;
    private Activity parentActivity;

    private long lastPrint = 0;
    private long lastDataTime = 0;
    private String lastDataString;

    private double zerob = Double.MAX_VALUE;
    private double oneb = 0;
    private String statusString;
    private String dataString;
    private int totalBits = 0;
    private int totalErrors = 0;

    public static int currentBit;
    public static double avg[];

    private ArrayList<DataPoint> rawData;


    private boolean wasFixedForDoubleGlitch = false;


    public ReceiverThread(Activity parent, int transmit_ms) {
        lastPrint = System.currentTimeMillis();
        Config.TRANSMIT_MS = transmit_ms;
        statusString = new String();
        dataString = new String();
        lastDataString = new String();
        parentActivity = parent;
        alive = true;

        zeroStats();
    }

    public void zeroStats() {
        zerob = 0;
        oneb = 255;
        rawData = new ArrayList<>();
    }


    public void run() {
        alive = true;
        audioCapturer = AudioCapturer.getInstance(this, Config.SAMPLES_PER_SECOND, Config.TRANSMIT_MS, Config.CHUNKS);
        audioCapturer.start();
        while (alive) {
            try {
                Thread.sleep(500);
                if (isAlive()) parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run () { ((ReceiverActivity) parentActivity).updateStatus(dataString,statusString);  }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        audioCapturer.stop();
    }


    public void die() {
        alive = false;
    }

    @Override
    public void capturedAudioReceived(final short[] tempBuf) {
        onDataReceived(tempBuf);
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                ((ReceiverActivity) parentActivity).updateGraphics(tempBuf);
            }
        });


    }

    public void onDataReceived(short[] bytes) {

        long now = System.currentTimeMillis();

        if (lastDataString.equals(dataString)) {
               if (now - lastDataTime > 8000) {
                   this.zeroStats();
                   lastDataTime = now;
                   lastDataString = dataString;
               }
        } else {
                lastDataTime = now;
                lastDataString = dataString;
        }

        avg = bruteForceAutoCorrelation(bytes);
        //double avg[] = patternCorrelation(bytes);
        zerob = Math.min(zerob,avg[0]);
        oneb = Math.max(oneb,avg[0]);
        currentBit = decode(avg);
        if (currentBit > -1) {
            rawData.add(new DataPoint(now,currentBit));
            //QUICK AND DIRTY FILTER, 101 becomes 111 and 010 becomes 000
            int s = rawData.size();
            if (s > 3) {
                //it was 11 001 -> 11 110. if a 1 comes, it was fixed by first if. but if a zero comes, maybe the original two 0s were ok. FIX
                if (wasFixedForDoubleGlitch) {
                    if ((rawData.get(s-1).bit == rawData.get(s-2).bit)) {
                        rawData.get(s-3).bit = rawData.get(s-1).bit;
                        rawData.get(s-4).bit = rawData.get(s-1).bit;
                    }
                    wasFixedForDoubleGlitch = false;
                }
                if ((rawData.get(s-1).bit != rawData.get(s-2).bit) && (rawData.get(s-1).bit == rawData.get(s-3).bit)) rawData.get(s-2).bit = rawData.get(s-1).bit;
                else if ((rawData.get(s-1).bit != rawData.get(s-2).bit) && (rawData.get(s-1).bit != rawData.get(s-3).bit) && (rawData.get(s-1).bit == rawData.get(s-4).bit)){
                    //eg . 11 001 -> 11 110 . two zeros lost, compensated on next run if next bit is zero. Otherwise is fixed by previous if on next run
                    rawData.get(s-1).bit = rawData.get(s-2).bit;
                    rawData.get(s-3).bit = rawData.get(s-4).bit;
                    rawData.get(s-2).bit = rawData.get(s-4).bit;
                    wasFixedForDoubleGlitch = true;
                }


            }
        }

        if (now-lastPrint > Config.PRINTINTERVAL && rawData.size() > 2 && isAlive()) {
            String decodedData = decodeDataHamming();
            String decodedAsciiData = decodeAsciiHamming(decodedData);
            int bps = (int)((rawData.size()/ Config.CHUNKS) / ((rawData.get(rawData.size()-1).arrived - rawData.get(0).arrived)/1000.0) ) ;
        //    Log.d("Svals","Min: "+ zerob+" Max: "+ oneb+"  -- bit: " + currentBit);
          //  Log.d("RGB rawData", rawData());
            //Log.d("RGB data", decodedData);
           // Log.d("RGB ascii", decodedAsciiData);
            synchronized (statusString) {
                statusString = (bps+"bps | "+totalErrors+" errors | " +(int)((totalErrors*1.0/totalBits)*100)+"% detected error rate");
            }
            synchronized (dataString) {
                dataString = decodedAsciiData.substring(Math.max(decodedAsciiData.length()-40,0));
            }
            lastPrint = now;
        }


    }


    // see Wiener-Khinchin theorem for speed improve here
    public double[] bruteForceAutoCorrelation(short x[]) {
        double ac[] = new double[x.length];
        double maxV = Double.MIN_VALUE;
        double result[] = new double[3];
        int n = x.length;
        double avg = 0;
        for (int j = 0; j < n; j++) {
            avg += Math.abs(x[j]);
            for (int i = 0; (i < n); i++) {
                ac[j] += x[i] * x[(n + i - j)%n];
            }
            if (ac[j] > maxV) {
                maxV = ac[j];
                result[1] = maxV;
            }
        }
        result[2] = 0;
        for (int h=0;h<ac.length;h++) {
            if (ac[h] > 0.8 * maxV) result[2]++;
        }
        result [0] = maxV/avg;
        return result;
    }


    private int decode(double val[]) {
        if (oneb < 3000) return -1;
        if (Math.abs(zerob - oneb) < 0) return -1;
        if (val[2]<2) return 0;
        //if ((val[0] - oneb*.9 > zerob - val[0]) && val[2] > 2) return 1;
        return 1;
    }

    private String rawData() {
        StringBuilder sb = new StringBuilder();
        for (DataPoint d: rawData) sb.append(d.bit);
        return sb.toString();
    }

    private String decodeDataHamming() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        if (rawData == null || rawData.size() < 2) return "";
        DataPoint d;

        long last = 0;
        int bit = 0;
        int counter = 0;
        int i = 0;

        while (i < rawData.size()) {


            boolean foundPreamble = false;

            while (!foundPreamble) {
                long preambleStart, preambleHalf, preambleStop;
                //Find preamble

                //SEARCH FOR FIRST 1s
                while (i < rawData.size() && rawData.get(i).bit == 0) i++;
                if (i >= rawData.size()) return sb.toString();
                preambleStart = rawData.get(i).arrived;

                //SEARCH FOR 0s after 1s
                while (i < rawData.size() && rawData.get(i).bit == 1) i++;
                if (i >= rawData.size())  return sb.toString();
                preambleHalf = rawData.get(i).arrived;

                // CHECK 1s duration
                if (preambleHalf - preambleStart > Config.TRANSMIT_MS* Config.PREAMBLE_1N-(0.4* Config.TRANSMIT_MS)) {
                    sb.append(" F");
                    //found first part of 1111s with 700ms duration, search for 000s with 500ms duration
                    while (i < rawData.size() && (rawData.get(i).bit == 0 && rawData.get(i).arrived - preambleHalf <  Config.TRANSMIT_MS* Config.PREAMBLE_0N)) i++;
                    if (i >= rawData.size()) return sb.toString();
                    preambleStop = rawData.get(i).arrived;
                    if (preambleStop - preambleHalf  > Config.TRANSMIT_MS* Config.PREAMBLE_0N-(0.4* Config.TRANSMIT_MS)) {
                        sb.append("S");
                        foundPreamble = true;
                    }
                }
            }


            int framebits = 0;
            while (framebits++ < Config.PREAMBLEINTERVAL && i < rawData.size()) {
                sb.append(" ");
                counter = 0;
                //Aligned to preamble, decode 14 bits
                long startByte = rawData.get(i).arrived;
                last = startByte;
                bit = 0;
                int bitWindow = 1;
                //Decode two 7 bit words
                while (i < rawData.size() && rawData.get(i).arrived < startByte + 14 * Config.TRANSMIT_MS) {
                    d = rawData.get(i);
                    if (d.arrived > startByte + bitWindow * Config.TRANSMIT_MS) {
                        sb.append(Math.round(bit * 1.0 / counter));
                        last = d.arrived;
                        bit = 0;
                        counter = 0;
                        bitWindow++;
                        continue;
                    } else {
                        bit += d.bit;
                        counter++;
                    }
                    i++;
                }
                sb.append(Math.round(bit * 1.0 / counter));
            }
        }
        return sb.toString();
    }

    private String decodeAsciiHamming(String decodedData) {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        String chars[] = decodedData.split(" ");
        if (chars.length < 1) return sb.toString();

        totalBits = 0;
        totalErrors = 0;
        int finalcode = 0;
        /*for (String c : chars) {
            if (c.length() < 6) continue;
            int code = 0;
            for (int i = 0; i < c.length(); i++) {
                if (c.charAt(i) == '1') {
                    code += 1 << i;
                }
            }
            if (firstPart) {
                finalcode = HammingCodes.decode((char)code);
                firstPart = false;
            } else {
                finalcode += (HammingCodes.decode((char)code)<<4);
                sb.append((char) finalcode);
                firstPart = true;
                finalcode = 0;
            }

        }*/
        for (String c : chars) {
            if (c.length() < 14) continue;
            int code = 0;
            for (int i = 0; i < 7; i++) {
                if (c.charAt(i) == '1') {
                    code += 1 << i;
                }
            }
            finalcode = HammingCodes.decode((char)code);
            if (HammingCodes.hasError((char)code)) totalErrors++;

            code = 0;
            for (int i = 7; i < 14; i++) {
                if (c.charAt(i) == '1') {
                    code += 1 << (i-7);
                }
            }
            finalcode += (HammingCodes.decode((char)code)<<4);
            if (HammingCodes.hasError((char)code)) totalErrors++;
            sb.append((char) finalcode);
            totalBits+=8;
        }
        return sb.toString();
    }



}
