package xyz.kripthor.nfcexfil;

/**
 * Created by kripthor on 23-02-2018.
 */

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.os.Process;
import android.util.Log;


public class NfcExfiltrateThread extends Thread {


    private NfcAdapter nfcAdapter;
    private Activity parentActivity;
    private boolean alive;
    private String dataToSend;
    private int wordsSent = 0;
    private long timeTransmitting = Config.TRANSMIT_MS;

    public NfcExfiltrateThread(Activity act, NfcAdapter nfca, int cycle, int duration, String data) {
        nfcAdapter = nfca;
        parentActivity = act;
        dataToSend = data;
        Config.SLEEP_MS = cycle;
        Config.TRANSMIT_MS = duration;
        nfcAdapter.enableReaderMode(parentActivity, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    @Override
    public void run() {
        alive = true;
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
        //JUMPSTART?
        sendTest();

        while (alive) {
            //sendTest();
            this.sendString(dataToSend,false);
        }
        nfcAdapter.enableReaderMode(parentActivity, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
    }

    public void sendTest() {
        sendBit(1);
        sendBit(0);

        sendBit(1);
        sendBit(1);
        sendBit(0);
        sendBit(0);

        sendBit(1);
        sendBit(1);
        sendBit(1);
        sendBit(0);
        sendBit(0);
        sendBit(0);
    }

    public void die() {
        alive = false;
    }

    public boolean isLiving() {
        return alive;
    }


    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private void sendBit(int bit) {
        long start;
        if (!isLiving()) return;
        updateUiBit(bit);
        long tt;
        try {
            start = System.currentTimeMillis();
            if (bit == 1) {
                while (System.currentTimeMillis() - start < Config.TRANSMIT_MS-12) {
                    nfcAdapter.enableReaderMode(parentActivity, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
                    sleep(Config.SLEEP_MS);
                    if (System.currentTimeMillis() - start >= Config.TRANSMIT_MS-12) break;
                    nfcAdapter.enableReaderMode(parentActivity, null, Config.READERMODE, null);
                    sleep(Config.SLEEP_MS);
                }
                while (System.currentTimeMillis() - start < Config.TRANSMIT_MS) {
                    sleep(1);
                }
              } else {
                nfcAdapter.enableReaderMode(parentActivity, null, NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
                while (System.currentTimeMillis() - start < Config.TRANSMIT_MS) {
                    sleep(1);
                }

            }
           // tt = System.currentTimeMillis() - start;
            //Log.d("LOST","Bit "+bit+" - took "+ tt+"ms, should be "+TRANSMIT_MS+"ms");
            //WAS THERE A FIX? RESET
            /*if (timeTransmitting <= TRANSMIT_MS) {
                timeTransmitting = TRANSMIT_MS;
            } else {
                timeTransmitting += (tt-TRANSMIT_MS);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendBitManchesterEncoding(int bit) {
        if (!alive) return;
        try {
            if (bit == 1) {
                sendBit(1);
                sendBit(0);
            } else {
                sendBit(0);
                sendBit(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendString(String stringToSend, boolean manchesterEncoding) {
        for (int i = 0; i < stringToSend.length(); i++) {
            if (!isLiving()) return;
            char cchar = stringToSend.charAt(i);
            this.updateUiChar(""+cchar);
            sendByteHamming(cchar, manchesterEncoding);
        }
    }

    private void sendByte(int byteToSend, boolean manchesterEncoding) {
        sendPreamble(9,2);

        if (!manchesterEncoding) {
            if ((byteToSend & 1) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 2) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 4) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 8) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 16) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 32) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 64) > 0) sendBit(1);
            else sendBit(0);
            if ((byteToSend & 128) > 0) sendBit(1);
            else sendBit(0);
        } else {
            if ((byteToSend & 1) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 2) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 4) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 8) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 16) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 32) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 64) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);
            if ((byteToSend & 128) > 0) sendBitManchesterEncoding(1);
            else sendBitManchesterEncoding(0);

        }
        sendTrailer(1);
    }

    private void sendByteHamming(int bytetosend, boolean manchesterEncoding) {
        int nibble1, nibble2;
        nibble1 = ((char) (bytetosend & 0x0f));
        nibble2 = ((char) ((bytetosend & 0xf0) >> 4));
        //Log.d("Hamming: ", "Byte: "+ Integer.toBinaryString(bytetosend)+ "| nibble1: "+Integer.toBinaryString(nibble1)+ "| nibble2: "+Integer.toBinaryString(nibble2));
        nibble1 = HammingCodes.encode((char) nibble1);
        nibble2 = HammingCodes.encode((char) nibble2);
       // Log.d("Hamming: ", "Byte: "+ Integer.toBinaryString(bytetosend)+ "| nibble1: "+Integer.toBinaryString(nibble1)+ "| nibble2: "+Integer.toBinaryString(nibble2));

        if (wordsSent % Config.PREAMBLEINTERVAL == 0) {
            sendTrailer(Config.TRAILER_0N);
            sendPreamble(Config.PREAMBLE_1N, Config.PREAMBLE_0N);
        }

        int b = 1;
        if (!manchesterEncoding) {
            while (b <= 64) {
                if ((nibble1 & b) > 0) sendBit(1);
                else sendBit(0);
                b = (b << 1);
            }
        } else {
            while (b <= 64) {
                if ((nibble1 & b) > 0) sendBitManchesterEncoding(1);
                else sendBitManchesterEncoding(0);
                b = (b << 1);
            }
        }

        b = 1;
        if (!manchesterEncoding) {
            while (b <= 64) {
                if ((nibble2 & b) > 0) sendBit(1);
                else sendBit(0);
                b = (b << 1);
            }
        } else {
            while (b <= 64) {
                if ((nibble2 & b) > 0) sendBitManchesterEncoding(1);
                else sendBitManchesterEncoding(0);
                b = (b << 1);
            }
        }
        wordsSent++;
    }

    private void sendPreamble(int onelength, int zerolength) {
        if (!isLiving()) return;
        long start;
        try {
            start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < Config.TRANSMIT_MS*onelength) {
                sendBit(1);
            }
            start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < Config.TRANSMIT_MS*zerolength) {
                sendBit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendTrailer( int zerolength) {
        if (!isLiving()) return;
        long start;
        try {
            start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < Config.TRANSMIT_MS*zerolength) {
                sendBit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateUiBit(final int bit) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                ((ExfiltrateActivity) parentActivity).updateGraphics(bit);
            }
        });
    }
    private void updateUiChar(final String cchar) {
        parentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                ((ExfiltrateActivity) parentActivity).updateGraphics(cchar);
            }
        });
    }
}
