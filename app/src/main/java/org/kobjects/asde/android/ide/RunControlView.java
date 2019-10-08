package org.kobjects.asde.android.ide;

import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.ForcedStopException;
import org.kobjects.asde.lang.event.StartStopListener;
import org.kobjects.asde.lang.io.Console;

public class RunControlView extends LinearLayout {
    //private final IconButton startButton;
    private final IconButton pauseButton;
    private final IconButton resumeButton;
    private final IconButton stopButton;
    private final IconButton stepButton;
    private final IconButton closeButton;
    private final MainActivity mainActivity;
    private final FloatingActionButton startButton;

    public RunControlView(MainActivity mainActivity, FloatingActionButton startButton) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.startButton = startButton;

        startButton.setOnClickListener( item -> {
            hideControlButtons();
            mainActivity.shell.mainControl.start();
        });

        /*
        startButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainControl.start();
        });
         */
        stopButton = new IconButton(mainActivity, R.drawable.baseline_stop_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainControl.abort();
        });
        pauseButton = new IconButton(mainActivity, R.drawable.baseline_pause_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainControl.pause();
        });
        resumeButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24, item -> {
           hideControlButtons();
           mainActivity.shell.mainControl.resume();
        });
        stepButton = new IconButton(mainActivity, R.drawable.baseline_skip_next_24, item -> {
            mainActivity.shell.mainControl.step();
        });
        closeButton = new IconButton(mainActivity, R.drawable.baseline_clear_24, item -> {
            hideControlButtons();
            startButton.show();
            mainActivity.clearScreen(Console.ClearScreenType.PROGRAM_CLOSED);
            mainActivity.fullScreenMode = false;
            mainActivity.arrangeUi();
        });

        addView(stepButton);
        addView(resumeButton);
        addView(pauseButton);
        addView(stopButton);
        addView(closeButton);
        startButton.hide();
        // addView(startButton);

        hideControlButtons();
        startButton.show();

        mainActivity.shell.mainControl.addStartStopListener(new StartStopListener() {
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
                    mainActivity.clearScreen(Console.ClearScreenType.PROGRAM_CLOSED);
                    startButton.show();
                    mainActivity.fullScreenMode = false;
                    mainActivity.arrangeUi();

                    if (cause != null && !(cause instanceof ForcedStopException)) {
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
        startButton.hide();
        stepButton.setVisibility(GONE);
        stopButton.setVisibility(GONE);
        closeButton.setVisibility(GONE);
        mainActivity.programView.unHighlight();
    }
}
