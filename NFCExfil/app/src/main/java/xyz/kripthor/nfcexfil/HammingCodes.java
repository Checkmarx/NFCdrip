package xyz.kripthor.nfcexfil;

/**
 * Created by kripthor on 05-03-2018.
 */

public class HammingCodes {
    public static final char hammingCodes[] =
    {
                0x00,   /* 0 */
                0x71,   /* 1 */
                0x62,   /* 2 */
                0x13,   /* 3 */
                0x54,   /* 4 */
                0x25,   /* 5 */
                0x36,   /* 6 */
                0x47,   /* 7 */
                0x38,   /* 8 */
                0x49,   /* 9 */
                0x5A,   /* A */
                0x2B,   /* B */
                0x6C,   /* C */
                0x1D,   /* D */
                0x0E,   /* E */
                0x7F    /* F */
    };


    /* table convering encoded value (with error) to original data */
    /* hammingDecodeValues[code] = original data */
    public static final char hammingDecodeValues[] =
    {
                0x00, 0x00, 0x00, 0x03, 0x00, 0x05, 0x0E, 0x07,     /* 0x00 to 0x07 */
                0x00, 0x09, 0x0E, 0x0B, 0x0E, 0x0D, 0x0E, 0x0E,     /* 0x08 to 0x0F */
                0x00, 0x03, 0x03, 0x03, 0x04, 0x0D, 0x06, 0x03,     /* 0x10 to 0x17 */
                0x08, 0x0D, 0x0A, 0x03, 0x0D, 0x0D, 0x0E, 0x0D,     /* 0x18 to 0x1F */
                0x00, 0x05, 0x02, 0x0B, 0x05, 0x05, 0x06, 0x05,     /* 0x20 to 0x27 */
                0x08, 0x0B, 0x0B, 0x0B, 0x0C, 0x05, 0x0E, 0x0B,     /* 0x28 to 0x2F */
                0x08, 0x01, 0x06, 0x03, 0x06, 0x05, 0x06, 0x06,     /* 0x30 to 0x37 */
                0x08, 0x08, 0x08, 0x0B, 0x08, 0x0D, 0x06, 0x0F,     /* 0x38 to 0x3F */
                0x00, 0x09, 0x02, 0x07, 0x04, 0x07, 0x07, 0x07,     /* 0x40 to 0x47 */
                0x09, 0x09, 0x0A, 0x09, 0x0C, 0x09, 0x0E, 0x07,     /* 0x48 to 0x4F */
                0x04, 0x01, 0x0A, 0x03, 0x04, 0x04, 0x04, 0x07,     /* 0x50 to 0x57 */
                0x0A, 0x09, 0x0A, 0x0A, 0x04, 0x0D, 0x0A, 0x0F,     /* 0x58 to 0x5F */
                0x02, 0x01, 0x02, 0x02, 0x0C, 0x05, 0x02, 0x07,     /* 0x60 to 0x67 */
                0x0C, 0x09, 0x02, 0x0B, 0x0C, 0x0C, 0x0C, 0x0F,     /* 0x68 to 0x6F */
                0x01, 0x01, 0x02, 0x01, 0x04, 0x01, 0x06, 0x0F,     /* 0x70 to 0x77 */
                0x08, 0x01, 0x0A, 0x0F, 0x0C, 0x0F, 0x0F, 0x0F      /* 0x78 to 0x7F */
    };

    public static char encode(char nibble) {
        if (nibble <= 0x0f) return hammingCodes[nibble];
        return 0xff;

    }

    public static char decode(char bits) {
        if (bits <= 0x7f) return hammingDecodeValues[bits];
        return 0xff;
    }

    public static boolean hasError(char bits) {
        for (int i = 0;i< hammingCodes.length;i++) if (hammingCodes[i] == bits) return false;
        return true;
    }

}
