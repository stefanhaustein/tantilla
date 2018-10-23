package org.kobjects.asde.android.ide;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.StartStopListener;

import java.io.IOException;

public class ControlView extends LinearLayout  {


    public EmojiEditText codeEditText;
    public EmojiTextView resultView;
    IconButton enterButton;
    public IconButton menuButton;
    public EmojiEditText consoleEditText;
    private EmojiPopup emojiPopup;
    private IconButton startButton;
    private IconButton pauseButton;
    private IconButton resumeButton;
    private IconButton emojiButton;
    private IconButton stopButton;
    private IconButton stepButton;
    private LinearLayout inputLayout;

    MainActivity mainActivity;
    boolean clearScreenOnTermination;


    public ControlView(MainActivity mainActivity) {
        super(mainActivity);
        setOrientation(VERTICAL);
        this.mainActivity = mainActivity;

        menuButton = new IconButton(mainActivity, R.drawable.baseline_menu_black_24);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        startButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_black_24);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                mainActivity.mainInterpreter.start();
            }
        });
        stopButton = new IconButton(mainActivity, R.drawable.baseline_stop_black_24);
        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                clearScreenOnTermination = true;
                mainActivity.mainInterpreter.terminate();
            }
        });
        pauseButton = new IconButton(mainActivity, R.drawable.baseline_pause_black_24);
        pauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                mainActivity.mainInterpreter.pause();
            }
        });
        resumeButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_black_24);
        resumeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                mainActivity.mainInterpreter.resume();
            }
        });

        stepButton = new IconButton(mainActivity, R.drawable.baseline_skip_next_black_24);
        stepButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.mainInterpreter.step();
            }
        });
        hideControlButtons();
        startButton.setVisibility(VISIBLE);

        emojiButton = new IconButton(mainActivity, R.drawable.baseline_tag_faces_black_24);
        emojiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup == null) {
                    emojiPopup = EmojiPopup.Builder.fromRootView(mainActivity.rootView).build(codeEditText);
                }
                if (emojiPopup.isShowing()) {
                    dismissEmojiPopup();
                } else {
                    emojiButton.setImageResource(R.drawable.baseline_keyboard_black_24);
                    emojiPopup.toggle();
                    // Needed sometimes when the keyboard is not showing in the first place.
                    codeEditText.post(new Runnable() {
                        @Override
                        public void run() {
                            if (emojiPopup != null && !emojiPopup.isShowing()) {
                                emojiPopup.toggle();
                            }
                        }
                    });
                }
            }
        });


        enterButton = new IconButton(mainActivity, R.drawable.baseline_keyboard_return_black_24);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeEditText.getVisibility() == View.VISIBLE) {
                    mainActivity.enter();
                } else {
                    mainActivity.readLine = consoleEditText.getText().toString();
                    consoleEditText.setText("");
                }
            }
        });

        // Main area

        resultView = new EmojiTextView(mainActivity);
        resultView.setTypeface(Typeface.MONOSPACE);
        resultView.setGravity(Gravity.BOTTOM);

        codeEditText = new EmojiEditText(mainActivity);
        codeEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        codeEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        consoleEditText = new EmojiEditText(mainActivity);
        consoleEditText.setVisibility(View.GONE);

        inputLayout = new LinearLayout(mainActivity);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        inputLayout.addView(consoleEditText);
        inputLayout.addView(codeEditText);

        // Right button bar

        mainActivity.mainInterpreter.addStartStopListener(new StartStopListener() {
            @Override
            public void programStarted() {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideControlButtons();
                        clearScreenOnTermination = false;
                        pauseButton.setVisibility(VISIBLE);
                    }
                });
            }

            @Override
            public void programTerminated() {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        consoleEditText.setVisibility(GONE);
                        codeEditText.setVisibility(VISIBLE);

                        hideControlButtons();
                        startButton.setVisibility(VISIBLE);

                        if (clearScreenOnTermination) {
                            mainActivity.clearScreen();
                        }
                    }
                });
            }

            @Override
            public void programPaused() {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        consoleEditText.setVisibility(GONE);
                        codeEditText.setVisibility(VISIBLE);

                        hideControlButtons();
                        resumeButton.setVisibility(VISIBLE);
                        stopButton.setVisibility(VISIBLE);
                        stepButton.setVisibility(VISIBLE);
                    }
                });
            }
        });

        arrangeButtons(false);
    }


    private void hideControlButtons() {
        pauseButton.setVisibility(GONE);
        resumeButton.setVisibility(GONE);
        startButton.setVisibility(GONE);
        stepButton.setVisibility(GONE);
        stopButton.setVisibility(GONE);
        mainActivity.programView.unHighlight();
    }

    public void arrangeButtons(boolean landscape) {
        MainActivity.removeFromParent(resultView);
        MainActivity.removeFromParent(inputLayout);

        MainActivity.removeFromParent(startButton);
        MainActivity.removeFromParent(emojiButton);
        MainActivity.removeFromParent(menuButton);
        MainActivity.removeFromParent(enterButton);
        MainActivity.removeFromParent(stopButton);
        MainActivity.removeFromParent(stepButton);
        MainActivity.removeFromParent(pauseButton);
        MainActivity.removeFromParent(resumeButton);

        removeAllViews();

        /*
        if (landscape) {
            LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
            resultLayoutParams.gravity = Gravity.BOTTOM;
            addView(new View(mainActivity), resultLayoutParams);
        }
        */


        LinearLayout topBar = new LinearLayout(mainActivity);
        if(!landscape) {
            topBar.addView(menuButton);
        }
        LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        resultLayoutParams.gravity = Gravity.BOTTOM;
        topBar.addView(resultView, resultLayoutParams);
        if (landscape) {
            topBar.addView(emojiButton);
        }
        topBar.addView(stepButton);
        topBar.addView(resumeButton);
        topBar.addView(stopButton);
        topBar.addView(pauseButton);
        topBar.addView(startButton);
        if (landscape) {
            topBar.addView(menuButton);
        }
        LinearLayout.LayoutParams topLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        topLayoutParams.gravity = Gravity.BOTTOM;
        addView(topBar, topLayoutParams);


        LinearLayout bottomBar = new LinearLayout(mainActivity);
        if (!landscape) {
            bottomBar.addView(emojiButton);
        }
        LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        inputLayoutParams.gravity = Gravity.BOTTOM;
        bottomBar.addView(inputLayout, inputLayoutParams);
        bottomBar.addView(enterButton);

        addView(bottomBar);
    }


    public void showMenu() {
        PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
        Menu mainMenu = popupMenu.getMenu();

        SubMenu clearMenu = mainMenu.addSubMenu("Clear");
        clearMenu.add("Clear Screen").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mainActivity.clearScreen();
                return true;
            }
        });
        clearMenu.add("Erase program").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
               mainActivity.eraseProgram();
                return true;
            }
        });


        Menu loadMenu = mainMenu.addSubMenu("Load");
        Menu examplesMenu = loadMenu.addSubMenu("Examples");
        for (final String name : mainActivity.getProgramStoragePath().list()) {
            loadMenu.add(name).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mainActivity.load(name);
                    return true;
                }
            });
        }
        try {
            for (final String example : mainActivity.getAssets().list("examples")) {
                examplesMenu.add(example).setOnMenuItemClickListener( item -> {
                        mainActivity.openExample(example);
                        return true;
                    }
                );
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        mainMenu.add("Fullscreen mode").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mainActivity.fullScreenMode = true;
                mainActivity.arrangeUi();
                return true;
            }
        });

        popupMenu.show();

    }

    public void dismissEmojiPopup() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            emojiPopup = null;
        }
        emojiButton.setImageResource(R.drawable.baseline_tag_faces_black_24);
    }
}
