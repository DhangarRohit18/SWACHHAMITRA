package com.example.civsetu.report;

import android.content.Context;
import android.media.MediaRecorder;
import java.io.File;
import java.io.IOException;

public class VoiceRecorderHelper {
    private MediaRecorder recorder;
    private File audioFile;

    public void startRecording(Context context) throws IOException {
        audioFile = new File(context.getExternalCacheDir(), "voice_report_" + System.currentTimeMillis() + ".3gp");
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(audioFile.getAbsolutePath());
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.prepare();
        recorder.start();
    }

    public File stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        return audioFile;
    }

    public void cancelRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
        if (audioFile != null && audioFile.exists()) {
            audioFile.delete();
        }
    }
}
