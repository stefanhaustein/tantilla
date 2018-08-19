package org.kobjects.sound;


public class Sound {

    String s;
    int pos;
    SampleManager manager;

    public Sound(SampleManager manager, String notes) {
        this.manager = manager;
        this.s = notes;
    }

    public void play() {
        play(false);
    }

    void play(boolean mayBlock) {
        while (pos < s.length()) {
            final int codePoint = Character.codePointAt(s, pos);
            pos += Character.charCount(codePoint);
            final double f;
            switch (codePoint) {
                case 'C': f = 261.63; break;
                case 'D': f = 293.66; break;
                case 'E': f = 329.63; break;
                case 'F': f = 349.23; break;
                case 'G': f = 392; break;
                case 'A': f = 440; break;
                case 'B': f = 493.88; break;
                case 'c': f = 523.25; break;
                case 'd': f = 587.33; break;
                case 'e': f = 659.26; break;
                case 'f': f = 698.46; break;
                case 'g': f = 783.99; break;
                case 'a': f = 880; break;
                case 'b': f = 987.77; break;
                default:
                    manager.play(Integer.toHexString(codePoint), new Runnable() {
                        public void run() {
                            play(false);
                        }

                    });
                    return;
            }

            double duration = 0.25;
            if (pos < s.length()) {
                char c = s.charAt(pos);
                boolean divide = false;
                if (c == '/') {
                    pos++;
                    divide = true;
                }
                int len = 0;
                while (pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
                    len = len * 10 + (s.charAt(pos++) - '0');
                }
                if (divide) {
                    duration /= len == 0 ? 2 : len;
                } else if (len != 0) {
                    duration *= len;
                }
            }
            if (mayBlock) {
                Tone.play(f, duration);
            } else {
                final double finalDuration = duration;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Tone.play(f, finalDuration);
                        play(true);
                    }
                }).start();
                return;
            }
        }
    }
}
