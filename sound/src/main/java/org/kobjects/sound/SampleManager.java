package org.kobjects.sound;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.HashSet;

public class SampleManager {
    private final Context context;
    private final HashSet<String> sounds = new HashSet<>();

    public SampleManager(Context context) {
        this.context = context;
        try {
            for (String s : context.getAssets().list("sound")) {
                sounds.add(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public boolean play(String name, final Runnable callback) {
        if (sounds.contains(name + ".mp3")) {
            name += ".mp3";
        } else if (sounds.contains(name + ".wav")) {
            name += ".wav";
        } else {
            callback.run();
            return false;
        }
        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd("sound/" + name);
            final MediaPlayer m = new MediaPlayer();
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    m.release();
                    callback.run();
                }
            });

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(false);
            m.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            callback.run();
            return false;
        }
    }

}
