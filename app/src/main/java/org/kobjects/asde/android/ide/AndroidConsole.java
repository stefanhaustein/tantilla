package org.kobjects.asde.android.ide;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.ForcedStopException;
import org.kobjects.asde.lang.Format;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.WrappedExecutionException;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.typesystem.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.SynchronousQueue;

public class AndroidConsole implements Console {
  private final MainActivity mainActivity;
  private TextView pendingOutput;
  private String readLine;
  boolean autoScroll = true;



  AndroidConsole(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }


  void clearOutput() {
    mainActivity.runOnUiThread(() -> {
      mainActivity.textOutputView.clear();
      mainActivity.controlView.resultView.setText("");
      pendingOutput = null;
    });
  }

  @Override
  public void clearScreen(ClearScreenType clearScreenType) {
    switch (clearScreenType) {
      case PROGRAM_CLOSED:
        mainActivity.screen.clearAll();  // sprites
        break;
      case CLEAR_STATEMENT:
        mainActivity.screen.clearAll();
        // Fall_through intended;
      case CLS_STATEMENT:
        clearOutput();
        mainActivity.screen.cls();
        break;
    }
  }



  @Override
  public String input(Type typeHint) {
    SynchronousQueue<String> inputQueue = new SynchronousQueue<>();
    final EditText[] inputEditText = new EditText[1];
    mainActivity.runOnUiThread(() -> {
      final LinearLayout inputView = new LinearLayout(mainActivity);
      inputEditText[0] = new EditText(mainActivity);

      inputEditText[0].setInputType(
          InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
              | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
              | (mainActivity.program.legacyMode ? InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS : 0)
              | (typeHint == Types.NUMBER ? InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED : 0));
      inputEditText[0].setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_FLAG_NO_FULLSCREEN);
      inputEditText[0].setOnEditorActionListener((view, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
            && event.getAction() == KeyEvent.ACTION_DOWN) {
          mainActivity.textOutputView.removeContent(inputView);
          inputQueue.add(inputEditText[0].getText().toString());
          return true;
        }
        return false;
      });

      inputView.addView(inputEditText[0], new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
      IconButton inputButton = new IconButton(mainActivity, R.drawable.baseline_keyboard_return_24);
      inputView.addView(inputButton);
      mainActivity.textOutputView.addContent(inputView);
      inputView.requestFocus();
      inputButton.setOnClickListener(item -> {
        mainActivity.textOutputView.removeContent(inputView);
        inputQueue.add(inputEditText[0].getText().toString());
      });
    });

    try {
      String result = inputQueue.take();
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      asb.append(result, Annotations.ACCENT_COLOR);
      asb.append("\n");
      print(asb.build());
      readLine = null;
      return result;
    } catch (InterruptedException e) {
      if (inputEditText[0] != null) {
        mainActivity.runOnUiThread(() -> inputEditText[0].setEnabled(false));
      }
      throw new ForcedStopException(e);
    }
  }


  @Override
  public ProgramReference nameToReference(String name) {
    String displayName;
    String url;
    if (name == null || name.isEmpty()) {
      displayName = "";
      url = "file://" + mainActivity.getFilesDir().getAbsolutePath() + "/Unnamed";
    } else {
      int cut0 = name.lastIndexOf("/");
      int cut1 = name.indexOf(".", cut0 + 1);
      displayName = name.substring(cut0 + 1, cut1 == -1 ? name.length() : cut1);

      if (name.startsWith("/")) {
        url = "file://" + name;
      } else if (name.indexOf(':') != -1) {
        url = name;
      } else {
        url = "file://" + mainActivity.getProgramStoragePath().getAbsolutePath() + "/" + name;
      }
    }
    return new ProgramReference(displayName, url, true);
  }

  ProgressDialog progressDialog;
  int openProgressCount;

  @Override
  public void startProgress(String title) {
    if (openProgressCount == 0) {
      mainActivity.runOnUiThread(() -> {
        if (progressDialog == null) {
          progressDialog = ProgressDialog.show(mainActivity, title, "", true);
        }
      });
    }
    openProgressCount++;
  }

  @Override
  public void showError(String s, Exception e) {
    e.printStackTrace();
    mainActivity.runOnUiThread(() -> {
      if (e instanceof WrappedExecutionException) {
        WrappedExecutionException wrappedExecutionException = (WrappedExecutionException) e;

        while (wrappedExecutionException.getCause() instanceof WrappedExecutionException) {
          wrappedExecutionException = (WrappedExecutionException) wrappedExecutionException.getCause();
        }

       highlight(wrappedExecutionException.functionImplementation, wrappedExecutionException.lineNumber);
      }

      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
      alertBuilder.setTitle(s == null ? "Error" : s);
      alertBuilder.setMessage(Format.exceptionToString(e));
      alertBuilder.show();
    });
  }

  @Override
  public void updateProgress(String update) {
    mainActivity.runOnUiThread(() -> {
      if (progressDialog != null) {
        progressDialog.setMessage(update);
      }
    });
  }

  @Override
  public void endProgress() {
    openProgressCount--;
    mainActivity.runOnUiThread(() -> {
      if (openProgressCount == 0) {
        progressDialog.dismiss();
        progressDialog = null;
      }
    });
  }

  @Override
  public void delete(int line) {
    FunctionView functionView = mainActivity.programView.currentFunctionView;
    mainActivity.program.deleteLine(functionView.symbol, line);
  }

  int lineCount;

  void postScrollIfAtEnd() {
    if (mainActivity.scrollContentView.getHeight() <= mainActivity.mainScrollView.getHeight() + mainActivity.mainScrollView.getScrollY()
        || mainActivity.mainScrollView.getScrollY() == 0) {
      autoScroll = true;
    }
    if (autoScroll) {
      mainActivity.mainScrollView.postDelayed(() -> {
            mainActivity.mainScrollView.scrollTo(0, Integer.MAX_VALUE / 2);
            }, 100);
    }
  }

  @Override
  public void edit(int line) {
    mainActivity.runOnUiThread(() -> {
      FunctionView functionView = mainActivity.programView.currentFunctionView;
      // Append moves the cursor to the end.
      mainActivity.controlView.codeEditText.setText("");
      if (functionView != null) {
        FunctionImplementation functionImplementation = functionView.functionImplementation;
        CodeLine codeLine = functionImplementation.getExactLine(line);
        if (codeLine != null) {
          mainActivity.controlView.codeEditText.append(line + " " + codeLine);
          return;
        }
      }
      mainActivity.controlView.codeEditText.append("" + line + " ");
    });
  }

  @Override
  public void edit(StaticSymbol symbol) {
    mainActivity.runOnUiThread(() -> mainActivity.programView.selectImpl(symbol));
  }

  @Override
  public FunctionImplementation getSelectedFunction() {
    return mainActivity.programView.currentFunctionView.functionImplementation;
  }


  private void printImpl(AnnotatedString s) {
    if (pendingOutput == null) {
      pendingOutput = new EmojiTextView(mainActivity);
      pendingOutput.setTypeface(Typeface.MONOSPACE);

      mainActivity.textOutputView.addContent(pendingOutput);
      postScrollIfAtEnd();
    }
    int cut = s.indexOf('\n');
    if (cut == -1) {
      pendingOutput.append(AnnotatedStringConverter.toSpanned(mainActivity, s, 0));
    } else {
      pendingOutput.append(AnnotatedStringConverter.toSpanned(mainActivity, s.subSequence(0, cut), 0));
      pendingOutput = null;
      if (cut < s.length() - 1) {
        printImpl(s.subSequence(cut + 1, s.length()));
      }
    }

  }



  @Override
  public void print(final CharSequence chars) {
    if (chars.length() == 0) {
      return;
    }
    final AnnotatedString s = AnnotatedString.of(chars);
    int cut = s.indexOf('\n');
    mainActivity.runOnUiThread(() -> {
      printImpl(s);
    });
    if (cut != -1) {
      lineCount++;
      try {
        Thread.sleep(Math.round(Math.log(lineCount + 1)));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }


  @Override
  public void highlight(FunctionImplementation function, int lineNumber) {
    mainActivity.runOnUiThread(() -> mainActivity.programView.highlightImpl(function, lineNumber));
  }

  @Override
  public InputStream openInputStream(String url) {
    try {
      if (url.startsWith("file:///android_asset/")) {
        return mainActivity.getAssets().open(url.substring(22));
      }
//      if (url.startsWith("http://vintage-basic.net/") || url.startsWith("https://raw.githubusercontent.com")) {
  //    }
      Uri uri = Uri.parse(url);
      try {
        return mainActivity.getContentResolver().openInputStream(uri);
      } catch (IOException e) {
        return new URL(url).openConnection().getInputStream();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public OutputStream openOutputStream(String url) {

    try {
      // Why is this needed?
      if (url.startsWith("file://")) {
        return new FileOutputStream(url.substring(6));
      }
      Uri uri = Uri.parse(url);
      return mainActivity.getContentResolver().openOutputStream(uri);
    } catch (IOException e) {
      throw new RuntimeException("Can't write to URL " + url, e);
    }
  }

}