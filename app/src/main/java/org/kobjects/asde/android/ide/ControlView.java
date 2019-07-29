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

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.editor.CreateClassFlow;
import org.kobjects.asde.android.ide.editor.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.Format;
import org.kobjects.asde.lang.io.ValidationException;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.expressionparser.ExpressionParser;

import java.io.IOException;

public class ControlView extends LinearLayout  {
  private final MainActivity mainActivity;
  public EmojiEditText codeEditText;
  public EmojiTextView resultView;
  IconButton enterButton;
  public IconButton menuButton;
  private EmojiPopup emojiPopup;
  private IconButton emojiButton;
  private LinearLayout inputLayout;

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
        enter();
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
        mainActivity.controlView.enter();
        return true;
      }
      return false;
    });


    inputLayout = new LinearLayout(mainActivity);
    inputLayout.setOrientation(LinearLayout.VERTICAL);

    inputLayout.addView(codeEditText);
  }


  public void arrangeButtons(boolean landscape) {
    MainActivity.removeFromParent(resultView);
    MainActivity.removeFromParent(inputLayout);
    MainActivity.removeFromParent(emojiButton);
    MainActivity.removeFromParent(menuButton);
    MainActivity.removeFromParent(enterButton);

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

      addView(mainActivity.runControlView, buttonParams);

    } else {
      setOrientation(VERTICAL);
      LinearLayout topBar = new LinearLayout(mainActivity);
      topBar.addView(menuButton);

      LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
      resultLayoutParams.gravity = Gravity.BOTTOM;
      topBar.addView(resultView, resultLayoutParams);
      topBar.addView(mainActivity.runControlView);
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

    Menu examplesMenu = mainMenu.addSubMenu("Examples");
    try {
      for (final String example : mainActivity.getAssets().list("examples")) {
        examplesMenu.add(example).setOnMenuItemClickListener(item -> {
              mainActivity.load(new ProgramReference(example, "file:///android_asset/examples/" + example, false), true, false);
              return true;
            }
        );
      }
    }catch (IOException e) {
      throw new RuntimeException(e);
    }

    Menu addMenu = mainMenu.addSubMenu("Add");
    addMenu.add("Class").setOnMenuItemClickListener(item -> {
      CreateClassFlow.start(mainActivity);
      return true;
    });
    addMenu.add("Function").setOnMenuItemClickListener(item -> {
      FunctionSignatureFlow.createFunction(mainActivity);
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
        mainActivity.load(mainActivity.nameToReference(files[0]), true, false);
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

    saveMenu.add("Add shortcut").setOnMenuItemClickListener(item -> {
      mainActivity.addShortcut();
      return true;
    });


    SubMenu displayMenu = mainMenu.addSubMenu("Display");
    displayMenu.add("Clear").setOnMenuItemClickListener(item -> {
      mainActivity.clearScreen(Console.ClearScreenType.CLS_STATEMENT);
      return true;
    });
    displayMenu.add(1, 0, 0, "Overlay mode").setChecked(!mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = false;
      mainActivity.arrangeUi();
      return true;
    });
    displayMenu.add(1, 0, 0, "Window mode").setChecked(mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = true;
      mainActivity.arrangeUi();
      return true;
    });
    displayMenu.setGroupCheckable(1, true, true);
    displayMenu.add("Dark theme").setCheckable(true).setChecked(mainActivity.preferences.getTheme() == Colors.Theme.DARK).setOnMenuItemClickListener(item -> {
      mainActivity.preferences.setTheme(mainActivity.preferences.getTheme() == Colors.Theme.DARK ? Colors.Theme.LIGHT : Colors.Theme.DARK);
      mainActivity.restart();
      return true;
    });

    popupMenu.show();

  }

  public void dismissEmojiPopup() {
    if (emojiPopup != null && emojiPopup.isShowing()) {
      emojiPopup.dismiss();
      emojiPopup = null;
    }
    emojiButton.setImageResource(R.drawable.baseline_tag_faces_24);
  }

  public void enter() {
    mainActivity.print(resultView.getText() + "\n");
    resultView.setText("");

    String line = codeEditText.getText().toString();

    if (line.equalsIgnoreCase("go 64") || line.equalsIgnoreCase("go 64!")) {
      mainActivity.preferences.setTheme(Colors.Theme.C64);
      mainActivity.restart();
    }
    try {
      mainActivity.shell.enter(line, mainActivity.programView.currentFunctionView.symbol, result -> {
        mainActivity.runOnUiThread(() -> {
          if (result == null) {
            resultView.setText("Ok");
          } else {
            resultView.setText("" + result);
          }
        });
      });
      codeEditText.setText("");
    } catch (ExpressionParser.ParsingException e) {
      e.printStackTrace();
      codeEditText.setText("");
      resultView.setText(Format.exceptionToString(e));
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      int len = e.end - e.start;
      int sanitizedStart = Math.min(Math.max(0, e.start + len), line.length());
      int sanitizedEnd = Math.max(sanitizedStart, Math.min(e.end + len, line.length()));
      asb.append(line, 0, sanitizedStart);
      asb.append(line.subSequence(sanitizedStart, sanitizedEnd), e);
      asb.append(line, sanitizedEnd, line.length());
      codeEditText.append(AnnotatedStringConverter.toSpanned(mainActivity, asb.build(), false));
    } catch (ValidationException e) {
      e.printStackTrace();
      codeEditText.setText("");
      resultView.setText(Format.exceptionToString(e.getErrors().values().iterator().next()));
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      e.getCodeLine().toString(asb, e.getErrors());
      codeEditText.append(AnnotatedStringConverter.toSpanned(mainActivity, asb.build(), false));
    } catch (Throwable e) {
      e.printStackTrace();
      resultView.setText(Format.exceptionToString(e));
    }
  }
}
