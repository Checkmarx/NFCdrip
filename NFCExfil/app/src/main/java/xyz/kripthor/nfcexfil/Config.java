package xyz.kripthor.nfcexfil;

import android.nfc.NfcAdapter;

/**
 * Created by kripthor on 13-03-2018.
 */

public class Config {

    public static final int MY_PERMISSIONS_REQUEST = 31;
    public static final long PRINTINTERVAL = 500;

    public static int PREAMBLEINTERVAL = 3;
    public static int PREAMBLE_1N = 8;
    public static int PREAMBLE_0N = 1;
    public static int TRAILER_0N = 1;
    public static int TRANSMIT_MS = 100;
    public static int SLEEP_MS = 1;
    public static int SAMPLES_PER_SECOND = 8000;
    public static int CHUNKS = 5;
    public static int READERMODE = NfcAdapter.FLAG_READER_NFC_A;

    public static String NFCmodeToString (int mode) {
        if (mode == NfcAdapter.FLAG_READER_NFC_A) return "NFC_A";
        if (mode == NfcAdapter.FLAG_READER_NFC_B) return "NFC_B";
        if (mode == NfcAdapter.FLAG_READER_NFC_F) return "NFC_F";
        if (mode == NfcAdapter.FLAG_READER_NFC_V) return "NFC_V";
        if (mode == NfcAdapter.FLAG_READER_NFC_BARCODE) return "NFC_BARCODE";
        if (mode == NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK) return "NFC_SKIPNDEF";
        if (mode == NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS) return "NFC_NOPSOUNDS";
        return "UKNOWN";
    }

}
