package org.kobjects.sound;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Tone {
    private static final int SAMPLE_RATE = 44100;

    public static void play(double freqOfTone, double duration) {
        if (freqOfTone > SAMPLE_RATE / 3 || freqOfTone < 8) {
            throw new IllegalArgumentException("frequency out of range (8..." + SAMPLE_RATE/3 + ")");
        }

        int length = (int) (SAMPLE_RATE * duration);
        short[] sample = new short[length];
        int ramp = Math.min(length / 20, 800);

        for (int i = 0; i < ramp; i++) {
            sample[i] = (short) ((Math.sin(freqOfTone * 2 * Math.PI * i / SAMPLE_RATE) * 0.9 * Short.MAX_VALUE * i)/ramp);
        }
        for (int i = ramp; i < length - ramp; ++i) {
            sample[i] = (short) (Math.sin(freqOfTone * 2 * Math.PI * i / SAMPLE_RATE) * 0.9 * Short.MAX_VALUE);
        }
        for (int i = length - ramp; i < length; ++i) {
            sample[i] = (short) ((Math.sin(freqOfTone * 2 * Math.PI * i / SAMPLE_RATE) * 0.9 * Short.MAX_VALUE * (length-i))/ramp);
        }

        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 2 * sample.length,
                AudioTrack.MODE_STATIC);


        System.err.println("************* SAMPLE RATE: " + audioTrack.getSampleRate());
        try {
            audioTrack.write(sample, 0, sample.length);
            audioTrack.play();

            while (audioTrack.getPlaybackHeadPosition() < sample.length) {
                Thread.sleep(10);
            }

            audioTrack.release();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
