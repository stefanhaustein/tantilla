package org.kobjects.asde.android.ide;

import android.widget.LinearLayout;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.StartStopListener;

public class RunControlView extends LinearLayout {
    private final IconButton startButton;
    private final IconButton pauseButton;
    private final IconButton resumeButton;
    private final IconButton stopButton;
    private final IconButton stepButton;
    private final IconButton closeButton;
    private final MainActivity mainActivity;

    public RunControlView(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        startButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainInterpreter.start();
        });
        stopButton = new IconButton(mainActivity, R.drawable.baseline_stop_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainInterpreter.abort();
        });
        pauseButton = new IconButton(mainActivity, R.drawable.baseline_pause_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainInterpreter.pause();
        });
        resumeButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainInterpreter.resume();
        });
        stepButton = new IconButton(mainActivity, R.drawable.baseline_skip_next_24, item -> {
            mainActivity.shell.mainInterpreter.step();
        });
        closeButton = new IconButton(mainActivity, R.drawable.baseline_clear_24, item -> {
            hideControlButtons();
            startButton.setVisibility(VISIBLE);
            mainActivity.clearCanvas();
            mainActivity.fullScreenMode = false;
            mainActivity.arrangeUi();
        });

        addView(stepButton);
        addView(resumeButton);
        addView(pauseButton);
        addView(stopButton);
        addView(closeButton);
        addView(startButton);

        hideControlButtons();
        startButton.setVisibility(VISIBLE);

        mainActivity.shell.mainInterpreter.addStartStopListener(new StartStopListener() {
            @Override
            public void programStarted() {
                mainActivity.runOnUiThread(() -> {
                    hideControlButtons();
                    if (!mainActivity.runningFromShortcut) {
                        pauseButton.setVisibility(VISIBLE);
                        stopButton.setVisibility(VISIBLE);
                    }
                    mainActivity.fullScreenMode = true;
                    mainActivity.arrangeUi();
                });
            }
            @Override
            public void programAborted(Exception cause) {
                mainActivity.runOnUiThread(() -> {
                    hideControlButtons();
                    mainActivity.clearCanvas();
                    startButton.setVisibility(VISIBLE);
                    mainActivity.fullScreenMode = false;
                    mainActivity.arrangeUi();

                    if (cause != null) {
                        mainActivity.showError(null, cause);
                    }

                });
            }
            @Override
            public void programEnded() {
                mainActivity.runOnUiThread(() -> {
                    hideControlButtons();
                    closeButton.setVisibility(VISIBLE);
                });
            }
            @Override
            public void programPaused() {
                mainActivity.runOnUiThread(() -> {
                    hideControlButtons();
                    resumeButton.setVisibility(VISIBLE);
                    stopButton.setVisibility(VISIBLE);
                    stepButton.setVisibility(VISIBLE);
                    mainActivity.fullScreenMode = false;
                    mainActivity.arrangeUi();
                });
            }
        });
    }


    private void hideControlButtons() {
        pauseButton.setVisibility(GONE);
        resumeButton.setVisibility(GONE);
        startButton.setVisibility(GONE);
        stepButton.setVisibility(GONE);
        stopButton.setVisibility(GONE);
        closeButton.setVisibility(GONE);
        mainActivity.programView.unHighlight();
    }
}
