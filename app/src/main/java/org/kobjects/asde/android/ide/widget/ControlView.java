package org.kobjects.asde.android.ide.widget;

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
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.StartStopListener;

import java.io.IOException;
import java.util.MissingFormatArgumentException;

public class ControlView extends LinearLayout  {


    public EmojiEditText codeEditText;
    public EmojiTextView resultView;
    IconButton enterButton;
    public IconButton menuButton;
    public EmojiEditText consoleEditText;
    private EmojiPopup emojiPopup;
    private IconButton startStopButton;
    private IconButton emojiButton;
    private LinearLayout inputLayout;

    MainActivity mainActivity;


    public ControlView(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;

        menuButton = new IconButton(mainActivity, R.drawable.baseline_menu_black_24);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        startStopButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_black_24);
        startStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainActivity.mainInterpreter.isRunning()) {
                    mainActivity.mainInterpreter.stop();
                } else {
                    mainActivity.mainInterpreter.runAsync();
                }
            }
        });

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

        codeEditText = new EmojiEditText(mainActivity);
        codeEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        codeEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        consoleEditText = new EmojiEditText(mainActivity);
        consoleEditText.setVisibility(View.GONE);

        inputLayout = new LinearLayout(mainActivity);
        inputLayout.setOrientation(LinearLayout.VERTICAL);

        inputLayout.addView(resultView);
        inputLayout.addView(consoleEditText);
        inputLayout.addView(codeEditText);

/*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        ((AutoCompleteTextView) codeEditText).setAdapter(adapter);
*/


        // Right button bar



        mainActivity.mainInterpreter.addStartStopListener(new StartStopListener() {
            @Override
            public void programStarted() {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startStopButton.setImageResource(R.drawable.baseline_stop_black_24);

                    }
                });
            }

            @Override
            public void programStopped() {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startStopButton.setImageResource(R.drawable.baseline_play_arrow_black_24);
                        consoleEditText.setVisibility(GONE);
                        codeEditText.setVisibility(VISIBLE);
                    }
                });
            }
        });


        arrangeButtons(false);
    }


    public void arrangeButtons(boolean landscape) {

        MainActivity.removeFromParent(startStopButton);
        MainActivity.removeFromParent(emojiButton);
        MainActivity.removeFromParent(menuButton);
        MainActivity.removeFromParent(enterButton);
        MainActivity.removeFromParent(inputLayout);

        removeAllViews();

        if (landscape) {
            setOrientation(VERTICAL);
            LinearLayout topButtonBar = new LinearLayout(mainActivity);
            topButtonBar.addView(emojiButton);
            topButtonBar.addView(startStopButton);
            topButtonBar.addView(menuButton);
            LinearLayout.LayoutParams topLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            topLayoutParams.gravity = Gravity.RIGHT;
            addView(topButtonBar, topLayoutParams);

            LinearLayout mainLayout = new LinearLayout(mainActivity);
            LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            inputParams.gravity = Gravity.BOTTOM;
            mainLayout.addView(inputLayout, inputParams);

            LinearLayout.LayoutParams enterParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
            enterParams.gravity = Gravity.BOTTOM;
            mainLayout.addView(enterButton, enterParams);

            addView(mainLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));


        } else {
            setOrientation(HORIZONTAL);
            LinearLayout leftButtonBar = new LinearLayout(mainActivity);
            leftButtonBar.setOrientation(LinearLayout.VERTICAL);
            leftButtonBar.addView(menuButton);
            leftButtonBar.addView(emojiButton);
            LinearLayout.LayoutParams leftLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            leftLayoutParams.gravity = Gravity.BOTTOM;
            addView(leftButtonBar, leftLayoutParams);

            LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            inputLayoutParams.gravity = Gravity.BOTTOM;
            addView(inputLayout, inputLayoutParams);

            LinearLayout rightButtonBar = new LinearLayout(mainActivity);
            rightButtonBar.setOrientation(LinearLayout.VERTICAL);
            rightButtonBar.addView(startStopButton);
            rightButtonBar.addView(enterButton);
            LinearLayout.LayoutParams rightLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rightLayoutParams.gravity = Gravity.BOTTOM;
            addView(rightButtonBar, rightLayoutParams);
        }

    }


    public void showMenu() {
        PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
        Menu mainMenu = popupMenu.getMenu();

        /*
        if (mainInterpreter.isRunning()) {
            mainMenu.add("Stop").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mainInterpreter.stop();
                    return true;
                }
            });
        } else {
            mainMenu.add("Run").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mainInterpreter.runAsync();
                    return true;
                }
            });
        }

*/

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
                examplesMenu.add(example).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mainActivity.openExample(example);
                        return true;
                    }
                });
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
