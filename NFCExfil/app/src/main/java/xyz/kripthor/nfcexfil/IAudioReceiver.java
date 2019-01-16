package xyz.kripthor.nfcexfil;

/**
 * Created by kripthor on 28-02-2018.
 */

public  interface IAudioReceiver {

    public void capturedAudioReceived(short[] tempBuf);

}
