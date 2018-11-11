package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.ProgramReference;
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

        menuButton = new IconButton(mainActivity, R.drawable.baseline_menu_24);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        startButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24);
        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                mainActivity.mainInterpreter.start();
            }
        });
        stopButton = new IconButton(mainActivity, R.drawable.baseline_stop_24);
        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                clearScreenOnTermination = true;
                mainActivity.mainInterpreter.terminate();
            }
        });
        pauseButton = new IconButton(mainActivity, R.drawable.baseline_pause_24);
        pauseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                mainActivity.mainInterpreter.pause();
            }
        });
        resumeButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24);
        resumeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideControlButtons();
                mainActivity.mainInterpreter.resume();
            }
        });

        stepButton = new IconButton(mainActivity, R.drawable.baseline_skip_next_24);
        stepButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.mainInterpreter.step();
            }
        });
        hideControlButtons();
        startButton.setVisibility(VISIBLE);

        emojiButton = new IconButton(mainActivity, R.drawable.baseline_tag_faces_24);
        emojiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emojiPopup == null) {
                    emojiPopup = EmojiPopup.Builder.fromRootView(mainActivity.rootView).build(codeEditText);
                }
                if (emojiPopup.isShowing()) {
                    dismissEmojiPopup();
                } else {
                    emojiButton.setImageResource(R.drawable.baseline_keyboard_24);
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


        enterButton = new IconButton(mainActivity, R.drawable.baseline_keyboard_return_24);
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
        // codeEditText.setImeActionLabel("Done", EditorInfo.IME_ACTION_SEND);
        codeEditText.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                mainActivity.enter();
                return true;
            }
            return false;
        });


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
                            mainActivity.clearCanvas();
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


        if (landscape) {
            setOrientation(HORIZONTAL);
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addView(menuButton, buttonParams);
            addView(emojiButton, buttonParams);

            LinearLayout ioView = new LinearLayout(mainActivity);
            ioView.setOrientation(VERTICAL);

            ioView.addView(resultView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ioView.addView(inputLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(ioView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            buttonParams.gravity = Gravity.CENTER_VERTICAL;
            addView(enterButton, buttonParams);
            addView(stepButton, buttonParams);
            addView(resumeButton, buttonParams);
            addView(stopButton, buttonParams);
            addView(pauseButton, buttonParams);
            addView(startButton, buttonParams);

        } else {
            setOrientation(VERTICAL);
            LinearLayout topBar = new LinearLayout(mainActivity);
            topBar.addView(menuButton);

            LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            resultLayoutParams.gravity = Gravity.BOTTOM;
            topBar.addView(resultView, resultLayoutParams);
            topBar.addView(stepButton);
            topBar.addView(resumeButton);
            topBar.addView(stopButton);
            topBar.addView(pauseButton);
            topBar.addView(startButton);
            LinearLayout.LayoutParams topLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            topLayoutParams.gravity = Gravity.BOTTOM;
            addView(topBar, topLayoutParams);


            LinearLayout bottomBar = new LinearLayout(mainActivity);
            bottomBar.addView(emojiButton);
            LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            inputLayoutParams.gravity = Gravity.BOTTOM;
            bottomBar.addView(inputLayout, inputLayoutParams);
            bottomBar.addView(enterButton);

            addView(bottomBar);
        }
    }


    public void showMenu() {
        PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
        Menu mainMenu = popupMenu.getMenu();

        mainMenu.add("Erase all and restart").setOnMenuItemClickListener(item -> {
               mainActivity.eraseProgram();
               try {
                   mainActivity.program.save(mainActivity.program.reference);
                   mainActivity.restart();
               } catch (Exception e) {
                   e.printStackTrace();
               }
                return true;
        });



        Menu loadMenu = mainMenu.addSubMenu("Load");
        loadMenu.add("Load local file").setOnMenuItemClickListener(item -> {
            DialogProperties properties = new DialogProperties();
            properties.root = mainActivity.getProgramStoragePath();
            properties.error_dir = mainActivity.getProgramStoragePath();
            properties.offset = mainActivity.getProgramStoragePath();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;

            // TODO: new String[] {".bas", ".asde", ""};
            properties.extensions = null;

            FilePickerDialog dialog = new FilePickerDialog(mainActivity, properties);
            dialog.setTitle("Select Program File");
            dialog.show();
            dialog.setDialogSelectionListener(files -> {
                mainActivity.load(mainActivity.nameToReference(files[0]), true);
            });
            return true;
        });

        loadMenu.add("Load external file").setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            mainActivity.startActivityForResult(intent, MainActivity.OPEN_EXTERNALLY_REQUEST_CODE);
            return true;
        });

        loadMenu.add("Import external file").setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            mainActivity.startActivityForResult(intent, MainActivity.LOAD_EXTERNALLY_REQUEST_CODE);
            return true;
        });


        Menu examplesMenu = loadMenu.addSubMenu("Examples");
        try {
            for (final String example : mainActivity.getAssets().list("examples")) {
                examplesMenu.add(example).setOnMenuItemClickListener(item -> {
                            mainActivity.load(new ProgramReference(example, "file:///android_asset/examples/" + example, false), true);
                            return true;
                        }
                );
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }



        Menu saveMenu = mainMenu.addSubMenu("Save");

        saveMenu.add("Save locally as...").setOnMenuItemClickListener(item -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
            EditText fileNameInput = new EditText(mainActivity);
            fileNameInput.setText(mainActivity.preferences.getProgramReference().name);
            dialog.setTitle("Save as...");
            dialog.setMessage("File name");
            dialog.setView(fileNameInput);
            dialog.setPositiveButton("Save", (dlg, btn) -> {
                String name = fileNameInput.getText().toString();
                if (!name.isEmpty()) {
                    try {
                        mainActivity.program.save(mainActivity.nameToReference(fileNameInput.getText().toString()));
                    } catch (Exception e) {
                        mainActivity.showError("Error saving file " + fileNameInput.getText().toString(), e);
                    }
                }
            });
            dialog.show();
            return true;
        });

        saveMenu.add("Save externally as...").setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.setType("text/plain");

            mainActivity.startActivityForResult(intent, MainActivity.SAVE_EXTERNALLY_REQUEST_CODE);
            return true;
        });



        SubMenu displayMenu = mainMenu.addSubMenu("Display");
        displayMenu.add("Clear").setOnMenuItemClickListener(item -> {
                mainActivity.clearOutput();
            mainActivity.clearCanvas();
            return true;
        });

        displayMenu.add(1, 0, 0, "Overlay mode").setChecked(!mainActivity.windowMode).setOnMenuItemClickListener(item -> {
            mainActivity.windowMode = false;
            mainActivity.arrangeUi();
            return true;
        });
        displayMenu.add(1, 0, 0, "Fullscreen mode").setOnMenuItemClickListener(item -> {
                mainActivity.fullScreenMode = true;
                mainActivity.arrangeUi();
                return true;
        });
        displayMenu.add(1, 0, 0, "Window mode").setChecked(mainActivity.windowMode).setOnMenuItemClickListener(item -> {
            mainActivity.windowMode = true;
            mainActivity.arrangeUi();
            return true;
        });
        displayMenu.setGroupCheckable(1, true, true);

        AsdePreferences.Theme theme = mainActivity.preferences.getTheme();
        SubMenu themeMenu = mainMenu.addSubMenu("Theme");
        themeMenu.add(1, 0, 0, "Arcorn").setChecked(theme == AsdePreferences.Theme.ARCORN).setOnMenuItemClickListener(item -> {
            mainActivity.preferences.setTheme(AsdePreferences.Theme.ARCORN);
            mainActivity.restart();
            return true;
        });
        themeMenu.add(1, 0, 0, "C64").setChecked(theme == AsdePreferences.Theme.C64).setOnMenuItemClickListener(item -> {
            mainActivity.preferences.setTheme(AsdePreferences.Theme.C64);
            mainActivity.restart();
            return true;
        });
        themeMenu.add(1, 0, 0, "Spectrum").setChecked(theme == AsdePreferences.Theme.SPECTRUM).setOnMenuItemClickListener(item -> {
                    mainActivity.preferences.setTheme(AsdePreferences.Theme.SPECTRUM);
                    mainActivity.restart();
                    return true;
                });
        themeMenu.setGroupCheckable(1, true, true);

        popupMenu.show();

    }

    public void dismissEmojiPopup() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            emojiPopup = null;
        }
        emojiButton.setImageResource(R.drawable.baseline_tag_faces_24);
    }
}
