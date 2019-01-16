package xyz.kripthor.nfcexfil;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioCapturer implements Runnable {

    private int SAMPLES_PER_SECOND;
    private int TRANSMIT_MS;
    private int CHUNKS;

    private AudioRecord audioRecorder = null;
    private int bufferSize;
    private String LOG_TAG = "AudioCapturer";
    private Thread thread = null;

    private boolean isRecording;
    private static AudioCapturer audioCapturer;

    private IAudioReceiver iAudioReceiver;
    private int readbufsize;

    private AudioCapturer(IAudioReceiver audioReceiver, int samplesPerSec, int transmit_ms, int chunks)  {
        this.iAudioReceiver = audioReceiver;
        SAMPLES_PER_SECOND = samplesPerSec;
        TRANSMIT_MS = transmit_ms;
        CHUNKS = chunks;
    }

    public static AudioCapturer getInstance(IAudioReceiver audioReceiver, int samplesPerSec, int transmit_ms, int chunks) {
        if (audioCapturer == null) {
            audioCapturer = new AudioCapturer(audioReceiver,samplesPerSec,transmit_ms,chunks);
        }
        return audioCapturer;
    }

    public void start() {

        bufferSize = AudioRecord.getMinBufferSize(SAMPLES_PER_SECOND, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize != AudioRecord.ERROR_BAD_VALUE && bufferSize != AudioRecord.ERROR) {
            readbufsize = SAMPLES_PER_SECOND /(1000/ TRANSMIT_MS)/ CHUNKS;
            audioRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLES_PER_SECOND, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, readbufsize*2); // bufferSize


            if (audioRecorder != null && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                Log.i(LOG_TAG, "Audio Recorder created - buffer size = "+this.readbufsize);
                audioRecorder.startRecording();
                isRecording = true;
                thread = new Thread(this);
                thread.start();

            } else {
                Log.e(LOG_TAG, "Unable to create AudioRecord instance");
            }

        } else {
            Log.e(LOG_TAG, "Unable to get minimum buffer size");
        }
    }

    public void stop() {
        isRecording = false;
        if (audioRecorder != null) {
            if (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecorder.stop();
            }
            if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecorder.release();
            }
        }
        audioCapturer = null;
        audioRecorder = null;
        iAudioReceiver = null;
        thread = null;
    }

    public boolean isRecording() {
        return (audioRecorder != null) ? (audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) : false;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        short[] tempBuf = new short[readbufsize];
        while (isRecording && audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecorder.read(tempBuf,0, tempBuf.length);
            iAudioReceiver.capturedAudioReceived(tempBuf);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

    }

}