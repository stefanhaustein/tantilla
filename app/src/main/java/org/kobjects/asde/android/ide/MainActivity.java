package org.kobjects.asde.android.ide;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.one.EmojiOneProvider;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.Annotations;
import org.kobjects.annotatedtext.Span;
import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.symbollist.FunctionView;
import org.kobjects.asde.android.ide.symbollist.ProgramView;
import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.ResizableFrameLayout;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.type.FunctionImplementation;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Function;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.ProgramControl;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.io.Shell;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.WrappedExecutionException;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.library.ui.DpadAdapter;
import org.kobjects.asde.library.ui.ScreenAdapter;
import org.kobjects.asde.library.ui.SpriteAdapter;
import org.kobjects.asde.library.ui.TextBoxAdapter;
import org.kobjects.graphics.Screen;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.typesystem.FunctionType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class MainActivity extends AppCompatActivity implements Console {
    static final int SAVE_EXTERNALLY_REQUEST_CODE = 420;
    static final int LOAD_EXTERNALLY_REQUEST_CODE = 421;
    static final int OPEN_EXTERNALLY_REQUEST_CODE = 422;
    static final int PICK_SHORTCUT_ICON_REQUEST_CODE = 423;

    static final FunctionType FUNCTION_VOID_0 = new FunctionType(Types.VOID);

    public static void removeFromParent(View view) {
        if (view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    public Colors colors;
    LinearLayout scrollContentView;
  public View rootView;
  ScrollView mainScrollView;
  ScrollView leftScrollView;
  public ControlView controlView;
  public Program program = new Program(this);
  LinearLayout outputView;
  public String readLine;
  ResizableFrameLayout resizableFrameLayout;
  ScreenAdapter screenAdapter;
//  public ProgramControl mainInterpreter = new ProgramControl(program);
 // ProgramControl shellInterpreter = new ProgramControl(program);
  AsdePreferences preferences;
  boolean autoScroll = true;
  public boolean fullScreenMode;
  ProgramView programView;
  public Shell shell = new Shell(program);


  /** The view that displays the code in landscape mode */
  public ExpandableList codeView;

  RunControlView runControlView;
  private TitleView outputTitleView;
  private Screen screen;
  private TextView pendingOutput;
  boolean windowMode;
  boolean runningFromShortcut;

  ShortcutHandler shortcutHandler;

    @Override
  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    if (!screen.dispatchKeyEvent(keyEvent)) {
      return super.dispatchKeyEvent(keyEvent);
    }
    return true;
  }


  public Spanned annotatedStringToSpanned(AnnotatedString annotated, boolean linked) {
      SpannableString s = new SpannableString(annotated.toString());
      for (final Span span : annotated.spans()) {
          if (span.annotation == Annotations.ACCENT_COLOR) {
              s.setSpan(new ForegroundColorSpan(colors.accent), span.start, span.end, 0);
          } else if (span.annotation instanceof Exception) {
              s.setSpan(new BackgroundColorSpan(colors.accentLight), span.start, span.end, 0);
              if (linked) {
                  s.setSpan(new ClickableSpan() {
                      @Override
                      public void onClick(View widget) {
                          android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                          builder.setTitle("Error");
                          builder.setMessage(span.annotation.toString());
                          builder.show();
                      }
                  }, span.start, span.end, 0);
              }
          }
      }
      return s;
  }


    @Override
  protected void onCreate(Bundle savedInstanceState) {
      preferences = new AsdePreferences(this);
   //   setTheme(preferences.getDarkMode() ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

      switch (preferences.getTheme()) {
          case C64:
              setTheme(R.style.AppTheme_Blue);
              break;
          case LIGHT:
              setTheme(R.style.AppTheme_Light);
              break;
          default:
              setTheme(R.style.AppTheme_Dark);
      }

    super.onCreate(savedInstanceState);
    colors = new Colors(this, preferences.getTheme());
    EmojiManager.install(new EmojiOneProvider());

    programView = new ProgramView(this, program);

    outputTitleView = new TitleView(this, colors.primary);
    outputTitleView.setTitle("Output");
    outputView = new LinearLayout(this);
    outputView.setOrientation(LinearLayout.VERTICAL);
    outputView.addView(outputTitleView);

    resizableFrameLayout = new ResizableFrameLayout(this);

    scrollContentView = new LinearLayout(this);
    scrollContentView.setOrientation(LinearLayout.VERTICAL);


    ColorDrawable divider = new ColorDrawable(0x0) {
      @Override
      public int getIntrinsicHeight() {
        return Dimensions.dpToPx(MainActivity.this, 6);
      }
    };

    //scrollContentView.setDividerPadding(Dimensions.dpToPx(this, 12));
    scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    scrollContentView.setDividerDrawable(divider);

    scrollContentView.addView(programView);
    scrollContentView.addView(outputView);

    mainScrollView = new ScrollView(this);
    mainScrollView.addView(scrollContentView);

    leftScrollView = new ScrollView(this);
    runControlView = new RunControlView(this);
    controlView = new ControlView(this);
    screen = new Screen(this);

    new Thread(() -> {
        while (true) {
            if (shell.mainInterpreter.getState() != ProgramControl.State.PAUSED) {
                screen.animate(15);
            }
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }).start();

    screenAdapter = new ScreenAdapter(screen);

    program.setValue(GlobalSymbol.Scope.BUILTIN,"screen", screenAdapter);
    program.setValue(GlobalSymbol.Scope.BUILTIN,"Sprite", SpriteAdapter.CLASSIFIER);
    program.setValue(GlobalSymbol.Scope.BUILTIN, "TextBox", TextBoxAdapter.CLASSIFIER);
    program.setValue(GlobalSymbol.Scope.BUILTIN, "dpad", new DpadAdapter(screen.dpad));
    program.setValue(GlobalSymbol.Scope.BUILTIN, "cls", new Function() {
        @Override
        public FunctionType getType() {
            return FUNCTION_VOID_0;
        }
        @Override
        public Object call(Interpreter interpreter, int paramCount) {
            clearOutput();
            clearCanvas();
            return null;
        }
    });
    program.setValue(GlobalSymbol.Scope.BUILTIN, "sleep", new Function() {
        @Override
        public FunctionType getType() {
            return null;
        }

        @Override
        public Object call(Interpreter interpreter, int paramCount) {
            try {
                Thread.sleep(((Number) interpreter.localStack.getParameter(0, paramCount)).intValue());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mainScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY < oldScrollY) {
                        autoScroll = false;
                    }
                }
            });
        }



    ProgramReference programReference;
    String runIntent = getIntent().getStringExtra("run");
    runningFromShortcut = runIntent != null && !runIntent.isEmpty();
    if (runningFromShortcut) {
        programReference = ProgramReference.parse(runIntent);
    } else {
        arrangeUi();
        print("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
                + Runtime.getRuntime().freeMemory() + " ASDE BYTES FREE\n\n");

        programReference = preferences.getProgramReference();
    }

    program.addProgramRenameListener((program, newReference) -> {
            programView.requestSynchronization();
            preferences.setProgramReference(newReference);
        });

    load(programReference, false, runningFromShortcut);
  }

  public void addShortcut() {
        if (shortcutHandler == null) {
            shortcutHandler = new ShortcutHandler(this);
        }
        shortcutHandler.run();
  }

  public void restart() {
      PackageManager packageManager = getPackageManager();
      Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
      ComponentName componentName = intent.getComponent();
      Intent mainIntent = Intent.makeRestartActivityTask(componentName);
      startActivity(mainIntent);
      Runtime.getRuntime().exit(0);
  }


    int lineCount;

  void postScrollIfAtEnd() {
      if (scrollContentView.getHeight() <= mainScrollView.getHeight() + mainScrollView.getScrollY()
              || mainScrollView.getScrollY() == 0) {
          autoScroll = true;
      }
      if (autoScroll) {
          mainScrollView.post(new Runnable() {

          @Override
              public void run() {
                  if (scrollContentView.getHeight() != mainScrollView.getHeight() + mainScrollView.getScrollY()) {
                      mainScrollView.scrollTo(0, Integer.MAX_VALUE / 2);
                      mainScrollView.post(this);
                  }
              }
          });
      }
  }

  @Override
  public void print(final CharSequence chars) {
    final AnnotatedString s = AnnotatedString.of(chars);
      int cut = s.indexOf('\n');
    runOnUiThread(() -> {
        if (pendingOutput == null) {
            pendingOutput = new EmojiTextView(this);
            outputView.addView(pendingOutput);
            postScrollIfAtEnd();
        }
        if (cut == -1) {
          pendingOutput.append(annotatedStringToSpanned(s, true));
        }  else {
          pendingOutput.append(annotatedStringToSpanned(s.subSequence(0, cut), true));
          pendingOutput = null;
          if (cut < s.length() - 1) {
              print(s.subSequence(cut + 1, s.length()));
          }
      }
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

    /*
     * Syncs the displayed program code to the program code. If the sync is incremental,
     * any new function will be expanded automatically. Otherwise, the sync process includes
     * scrolling to the to and autorun support.
     *
  public void sync(boolean incremental) {
      runOnUiThread(() -> {
                  programView.sync(incremental);
                  if (!incremental) {
                      mainScrollView.scrollTo(0, 0);

                  }
              });

  }*/


  public void onConfigurationChanged (Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    arrangeUi();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode,
                               Intent resultData) {

      if (resultCode == RESULT_OK && resultData != null) {
          if (requestCode == PICK_SHORTCUT_ICON_REQUEST_CODE) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(resultData.getData());
                shortcutHandler.bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                addShortcut();

                return;
            } catch (Exception e) {
                showError("Icon loading error", e);
            }
          }


          Uri uri = resultData.getData();
          String displayName = "Unnamed";
          Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);
          try {
              if (cursor != null && cursor.moveToFirst()) {
                  displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
              }
          } finally {
              cursor.close();
          }
          int cut = displayName.lastIndexOf('.');
          if (cut != -1) {
              displayName = displayName.substring(0, cut);
          }

          int takeFlags = resultData.getFlags()
                  & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                  | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
          getContentResolver().takePersistableUriPermission(uri, takeFlags);

          boolean writable = (takeFlags & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0;
          ProgramReference programReference = new ProgramReference(displayName, uri.toString(), writable);

          switch (requestCode) {
              case OPEN_EXTERNALLY_REQUEST_CODE:
              case LOAD_EXTERNALLY_REQUEST_CODE:
                  try {
                      program.load(programReference);
                  } catch (Exception e) {
                      showError("Error loading external file", e);
                  }
                  break;

              case SAVE_EXTERNALLY_REQUEST_CODE:
                  try {
                      program.save(programReference);
                  } catch (Exception e) {
                      showError("Error saving external file", e);
                  }
                  break;
          }
      }
  }


  void arrangeUi() {
      runOnUiThread(() -> arrangeUiImpl());
  }

  private void arrangeUiImpl() {
      controlView.dismissEmojiPopup();

      removeFromParent(leftScrollView);
      removeFromParent(mainScrollView);
      removeFromParent(screen.view);
      removeFromParent(controlView);
      removeFromParent(programView);
      removeFromParent(codeView);
      removeFromParent(runControlView);
      removeFromParent(resizableFrameLayout);

      Display display = getWindowManager().getDefaultDisplay();
      int displayWidth = display.getWidth();
      int displayHeight = display.getHeight();

      if (programView.currentSymbolView != null) {
          programView.currentSymbolView.setExpanded(false, false);
      }

      if (fullScreenMode) {
          FrameLayout mainView = new FrameLayout(this);
          mainView.addView(mainScrollView);
          mainView.addView(screen.view);
          outputTitleView.setVisibility(View.GONE);

//         setContentView(viewport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  //       rootView = null;
          FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
          mainView.addView(runControlView, layoutParams);
          screen.view.setBackgroundColor(0);
          rootView = mainView;
      } else {
          outputTitleView.setVisibility(View.VISIBLE);
        LinearLayout rootLayout = new LinearLayout(this);
       // rootLayout.setDividerDrawable(systemListDivider);
        rootLayout.setDividerDrawable(new ColorDrawable(colors.primary));
        rootLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout mainView = new FrameLayout(this);
        rootLayout.addView(mainView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        rootLayout.addView(controlView,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (displayHeight >= displayWidth) {
            controlView.arrangeButtons(false);
            mainView.addView(mainScrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            scrollContentView.addView(programView, 0);
            scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            codeView = null;
        } else {
            controlView.arrangeButtons(true);
            leftScrollView.addView(programView);

            codeView = new ExpandableList(this);
            scrollContentView.addView(codeView, 0);
            scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);

            LinearLayout contentView = new LinearLayout(this);
            contentView.setDividerDrawable(new ColorDrawable(colors.primary));
            contentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            contentView.addView(leftScrollView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            contentView.addView(mainScrollView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            mainView.addView(contentView);
        }

          if (windowMode) {
              resizableFrameLayout.addView(screen.view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

              FrameLayout.LayoutParams resizableFrameLayoutParmas =
                      new FrameLayout.LayoutParams(Dimensions.dpToPx(this, 120), Dimensions.dpToPx(this, 120));

              resizableFrameLayoutParmas.rightMargin = Dimensions.dpToPx(this, 12);
              resizableFrameLayoutParmas.topMargin = Dimensions.dpToPx(this, 36);

              resizableFrameLayoutParmas.gravity = Gravity.TOP | Gravity.RIGHT;
              screen.view.setBackgroundColor(colors.background);

              mainView.addView(resizableFrameLayout, resizableFrameLayoutParmas);
          } else {
              screen.view.setBackgroundColor(0);
              mainView.addView(screen.view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
          }

        /*}  else {
            LinearLayout codingLayout = new LinearLayout(this);
            codingLayout.setOrientation(LinearLayout.VERTICAL);
            codingLayout.setDividerDrawable(systemListDivider);
            codingLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            codingLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            codingLayout.addView(bottomAppBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));

            rootLayout.addView(codingLayout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            rootLayout.addView(viewport, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        } */
        rootView = rootLayout;
     }
      setContentView(rootView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

    public void onBackPressed () {
      if (fullScreenMode && !runningFromShortcut) {
          shell.mainInterpreter.abort();
      } else {
          super.onBackPressed();
      }
    }

  @Override
  public String input() {
      SynchronousQueue<String> inputQueue = new SynchronousQueue<>();

    runOnUiThread(() -> {
        final LinearLayout inputView = new LinearLayout(this);
        final EditText inputEditText = new EditText(this);
        inputView.addView(inputEditText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        IconButton inputButton = new IconButton(this, R.drawable.baseline_keyboard_return_24);
        inputView.addView(inputButton);
        outputView.addView(inputView);
        inputButton.setOnClickListener(item-> {
            outputView.removeView(inputView);
            inputQueue.add(inputEditText.getText().toString());
        });
    });

    try {
        String result = inputQueue.take();
        print(result + "\n");
        readLine = null;
        return result;
    } catch (InterruptedException e) {
        throw new RuntimeException("interrupted");
    }
  }



    public File getProgramStoragePath() {
      File result = new File(getFilesDir(), "programs");
      // Mkdirs needed to make sure there is a programs storage path.
      result.mkdirs();
      return result;
    }

    @Override
    public ProgramReference nameToReference(String name) {
      String url;
      int cut = name.lastIndexOf("/");
      String displayName = cut == -1 ? name : name.substring(cut + 1);

      if (name.startsWith("/")) {
          url = "file://" + name;
      } else if (name.indexOf(':') != -1) {
          url = name;
      } else {
          url = "file://" + getProgramStoragePath().getAbsolutePath() + "/" + name;
      }
      return new ProgramReference(displayName, url, true);
    }

    ProgressDialog progressDialog;
    int openProgressCount;

    @Override
    public void startProgress(String title) {
        if (openProgressCount == 0) {
            runOnUiThread(() -> {
                if (progressDialog == null) {
                    progressDialog = ProgressDialog.show(this, title, "", true);
                }
            });
        }
        openProgressCount++;
    }

    @Override
    public void updateProgress(String update) {
        runOnUiThread(() -> {
            if (progressDialog != null) {
                progressDialog.setMessage(update);
            }
        });
    }

    @Override
    public void endProgress() {
        openProgressCount--;
        runOnUiThread(() -> {
            if (openProgressCount == 0) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        });
    }

    @Override
    public void delete(int line) {
        FunctionView functionView = programView.currentFunctionView;
        program.deleteLine(functionView.symbol, line);
    }

    @Override
    public void edit(int line) {
        runOnUiThread(() -> {
            FunctionView functionView = programView.currentFunctionView;
            // Append moves the cursor to the end.
            controlView.codeEditText.setText("");
            if (functionView != null) {
                FunctionImplementation functionImplementation = functionView.functionImplementation;
                Map.Entry<Integer, CodeLine> entry = functionImplementation.ceilingEntry(line);
                if (entry != null && entry.getKey() == line) {
                    controlView.codeEditText.append(entry.getKey() + " " + entry.getValue());
                    return;
                }
            }
            controlView.codeEditText.append("" + line + " ");
        });
    }

    @Override
    public void clearOutput() {
        runOnUiThread(new Runnable() {
            public void run() {
                for (int i = outputView.getChildCount() - 1; i > 0; i--) {
                    outputView.removeViewAt(i);
                }
                controlView.resultView.setText("");
                pendingOutput = null;
            }
        });
    }

    @Override
    public void clearCanvas() {
        //
        screen.cls();
    }


    @Override
    public void highlight(FunctionImplementation function, int lineNumber) {
      runOnUiThread(() -> programView.highlight(function, lineNumber));
    }

    @Override
    public InputStream openInputStream(String url) {
      try {
          if (url.startsWith("file:///android_asset/")) {
              return getAssets().open(url.substring(22));
          }
          Uri uri = Uri.parse(url);
          return getContentResolver().openInputStream(uri);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
    }

    @Override
    public OutputStream openOutputStream(String url) {
        try {
            Uri uri = Uri.parse(url);
            return getContentResolver().openOutputStream(uri);
        } catch (IOException e) {
            throw new RuntimeException("Can't write to URL " + url, e);
        }
    }

    public void eraseProgram() {
        shell.mainInterpreter.abort();
        program.deleteAll();
    }

    public void load(ProgramReference programReference, boolean showErrors, boolean run) {
        new Thread(() -> {
            shell.mainInterpreter.abort();
            try {
                program.load(programReference);
                if (run) {
                    shell.mainInterpreter.start();
                }
            } catch (Exception e) {
                if (showErrors) {
                    showError("Error loading " + programReference.url, e);
                } else {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void showError(String s, Exception e) {
        e.printStackTrace();
        runOnUiThread(() -> {
            if (e instanceof WrappedExecutionException) {
                WrappedExecutionException wrappedExecutionException = (WrappedExecutionException) e;

                while (wrappedExecutionException.getCause() instanceof WrappedExecutionException) {
                    wrappedExecutionException = (WrappedExecutionException) wrappedExecutionException.getCause();
                }

                highlight(wrappedExecutionException.functionImplementation, wrappedExecutionException.lineNumber);
            }

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle(s == null ? "Error" : s);
            alertBuilder.setMessage("" + e.getMessage());
            alertBuilder.show();
        });
    }
}
