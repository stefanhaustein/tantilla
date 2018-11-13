package org.kobjects.asde.android.ide;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.one.EmojiOneProvider;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.ResizableFrameLayout;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.ProgramControl;
import org.kobjects.asde.lang.ProgramReference;
import org.kobjects.asde.lang.Shell;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.asde.library.ui.DpadAdapter;
import org.kobjects.asde.library.ui.ScreenAdapter;
import org.kobjects.graphics.Viewport;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Console;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements Console {
  static final int SAVE_EXTERNALLY_REQUEST_CODE = 420;
  static final int LOAD_EXTERNALLY_REQUEST_CODE = 421;
  static final int OPEN_EXTERNALLY_REQUEST_CODE = 422;

  Colors colors;
    LinearLayout scrollContentView;
  public View rootView;
  ScrollView scrollView;
  ScrollView leftScrollView;
  ControlView controlView;
  Program program = new Program(this);
  Drawable systemListDivider;
  LinearLayout outputView;
  public String readLine;
  ScreenAdapter screen;
//  public ProgramControl mainInterpreter = new ProgramControl(program);
 // ProgramControl shellInterpreter = new ProgramControl(program);
  AsdePreferences preferences;
  boolean autoScroll = true;
  public boolean fullScreenMode;
  ProgramView programView;
  IconButton exitFullscreenButton;
  Shell shell = new Shell(program);

  /** The view that displays the code in landscape mode */
  ExpandableList codeView;

  private TitleView outputTitleView;
  private Viewport viewport;
  private boolean lineFeedPending;
  boolean windowMode;

    @Override
  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    if (!viewport.dispatchKeyEvent(keyEvent)) {
      return super.dispatchKeyEvent(keyEvent);
    }
    return true;
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

    IconButton clearButton = new IconButton(this, R.drawable.baseline_delete_24);
    clearButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clearOutput();
      }
    });


    programView = new ProgramView(this, program);

    outputTitleView = new TitleView(this, colors.primary);
    outputTitleView.setTitle("Output");
    outputTitleView.addView(clearButton);
    outputView = new LinearLayout(this);
    outputView.setOrientation(LinearLayout.VERTICAL);
    outputView.addView(outputTitleView);

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

    scrollView = new ScrollView(this);
    scrollView.addView(scrollContentView);

    leftScrollView = new ScrollView(this);
    controlView = new ControlView(this);
    viewport = new Viewport(this);
    screen = new ScreenAdapter(viewport);
    exitFullscreenButton = new IconButton(this, R.drawable.baseline_fullscreen_exit_24, icon -> {
        fullScreenMode = false;
        arrangeUi();
    });



    shell.shellInterpreter.addStartStopListener(new StartStopListener() {
        @Override
        public void programStarted() {
            // screen.cls();
        }

        @Override
        public void programTerminated() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sync(true);
                }
            });
        }

        @Override
        public void programPaused() {
            // TBD
        }
    });

    program.setValue(GlobalSymbol.Scope.BUILTIN,"screen", screen);
    program.setValue(GlobalSymbol.Scope.BUILTIN,"sprite", screen.spriteClassifier);
    program.setValue(GlobalSymbol.Scope.BUILTIN, "text", screen.textClassifier);
    program.setValue(GlobalSymbol.Scope.BUILTIN, "dpad", new DpadAdapter(viewport.dpad));
//    program.setValue(GlobalSymbol.Scope.BUILTIN,"pen", screen.penClassifier);

    arrangeUi();


    print("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
               + Runtime.getRuntime().freeMemory() + " ASDE BYTES FREE\n\n");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY < oldScrollY) {
                        autoScroll = false;
                    }
                }
            });
        }

      ProgramReference programReference = preferences.getProgramReference();

        load(programReference, false);

  }

  public void restart() {
      PackageManager packageManager = getPackageManager();
      Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
      ComponentName componentName = intent.getComponent();
      Intent mainIntent = Intent.makeRestartActivityTask(componentName);
      startActivity(mainIntent);
      Runtime.getRuntime().exit(0);
  }


  public void enter(String line) {
    if (line.equalsIgnoreCase("go 64") || line.equalsIgnoreCase("go 64!")) {
      preferences.setTheme(Colors.Theme.C64);
      restart();
    }
    try {
      shell.enter(line);
      controlView.codeEditText.setText("");
    } catch (Exception e) {
       e.printStackTrace();
       print(e.getMessage() + "\n");
    }
  }

  int lineCount;

  void postScrollIfAtEnd() {
      if (scrollContentView.getHeight() <= scrollView.getHeight() + scrollView.getScrollY()) {
          autoScroll = true;
      }
      if (autoScroll) {
          scrollView.post(new Runnable() {

          @Override
              public void run() {
                  if (scrollContentView.getHeight() != scrollView.getHeight() + scrollView.getScrollY()) {
                      scrollView.scrollTo(0, Integer.MAX_VALUE / 2);
                      scrollView.post(this);
                  }
              }
          });
      }
  }

  @Override
  public void print(final String s) {
    runOnUiThread(new Runnable() {
      public void run() {

        if (lineFeedPending) {
          TextView textView = new EmojiTextView(MainActivity.this);
          textView.setText(controlView.resultView.getText());
          textView.setTypeface(Typeface.MONOSPACE);
          outputView.addView(textView);
            postScrollIfAtEnd();
          lineFeedPending = false;
          lineCount++;
          controlView.resultView.setText("");
        }

        int cut = s.indexOf('\n');
        if (cut == -1) {
          controlView.resultView.setText(controlView.resultView.getText() + s);
        }  else {
          controlView.resultView.setText(controlView.resultView.getText() + s.substring(0, cut));
          lineFeedPending = true;
          if (cut < s.length() - 1) {
              print(s.substring(cut + 1));
          }
        }
      }
    });
    try {
      Thread.sleep(Math.round(Math.log(lineCount + 1)));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

    /**
     * Syncs the displayed program code to the program code. If the sync is incremental,
     * any new function will be expanded automatically. Otherwise, the sync process includes
     * scrolling to the to and autorun support.
     */
  public void sync(boolean incremental) {
      runOnUiThread(() -> {
                  programView.sync(incremental);
                  if (!incremental) {
                      scrollView.scrollTo(0, 0);
                      Map.Entry<Integer,CodeLine> line0 = program.main.ceilingEntry(0);
                      if (line0 != null) {
                          if (line0.getValue().toString().equalsIgnoreCase("REM autorun")) {
                              shell.mainInterpreter.start();
                          }

                      }

                  }
              });
/*
      if (!expandNew) {
          autoScroll = false;
          scrollView.scrollTo(0, 0);
          scrollView.post(new Runnable() {

              @Override
              public void run() {
                  scrollView.scrollTo(0, 0);
                  autoScroll = false;
              }
          });
      }
      */
  }


  public static void removeFromParent(View view) {
      if (view != null && view.getParent() instanceof ViewGroup) {
          ((ViewGroup) view.getParent()).removeView(view);
      }
  }

  public void onConfigurationChanged (Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    arrangeUi();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode,
                               Intent resultData) {

      if (resultCode == RESULT_OK && resultData != null) {
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

  public void arrangeUi() {
      controlView.dismissEmojiPopup();

      removeFromParent(leftScrollView);
      removeFromParent(scrollView);
      removeFromParent(viewport);
      removeFromParent(controlView);
      removeFromParent(programView);
      removeFromParent(codeView);
      removeFromParent(exitFullscreenButton);

      Display display = getWindowManager().getDefaultDisplay();
      int displayWidth = display.getWidth();
      int displayHeight = display.getHeight();

      if (programView.currentFunctionView != null) {
          programView.currentFunctionView.setExpanded(false, false);
      }

      if (fullScreenMode) {
         rootView = viewport;
//         setContentView(viewport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  //       rootView = null;
          FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
          layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
          viewport.addView(exitFullscreenButton, layoutParams);
      } else {
        LinearLayout rootLayout = new LinearLayout(this);
       // rootLayout.setDividerDrawable(systemListDivider);
        rootLayout.setDividerDrawable(new ColorDrawable(colors.primary));
        rootLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        rootLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout mainView = new FrameLayout(this);
        mainView.addView(scrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (windowMode) {
            ResizableFrameLayout resizableFrameLayout = new ResizableFrameLayout(this);
            resizableFrameLayout.addView(viewport, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            FrameLayout.LayoutParams resizableFrameLayoutParmas =
                    new FrameLayout.LayoutParams(Dimensions.dpToPx(this, 120), Dimensions.dpToPx(this, 120));

            resizableFrameLayoutParmas.rightMargin = Dimensions.dpToPx(this, 12);
            resizableFrameLayoutParmas.topMargin = Dimensions.dpToPx(this, 36);

            resizableFrameLayoutParmas.gravity = Gravity.TOP | Gravity.RIGHT;
            viewport.setBackgroundColor(colors.background);

            mainView.addView(resizableFrameLayout, resizableFrameLayoutParmas);
        } else {
            viewport.setBackgroundColor(0);
            mainView.addView(viewport, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        if (displayHeight >= displayWidth) {
            rootLayout.addView(mainView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            scrollContentView.addView(programView, 0);
            scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            rootLayout.addView(controlView,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            controlView.arrangeButtons(false);
            codeView = null;
        } else {
            leftScrollView.addView(programView);

            codeView = new ExpandableList(this);
            scrollContentView.addView(codeView, 0);
            scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);

            controlView.arrangeButtons(true);

            LinearLayout contentView = new LinearLayout(this);
            contentView.setDividerDrawable(new ColorDrawable(colors.primary));
            contentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            contentView.addView(leftScrollView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            contentView.addView(mainView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));

            rootLayout.addView(contentView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            rootLayout.addView(controlView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
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
      if (fullScreenMode) {
          fullScreenMode = false;
          arrangeUi();
      } else {
          super.onBackPressed();
      }
    }

  @Override
  public String input() {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            controlView.codeEditText.setVisibility(View.GONE);
            controlView.consoleEditText.setVisibility(View.VISIBLE);
        }
    });

    // Should use wait/notify or similar instead of active wait...
    while (readLine == null && !Thread.currentThread().isInterrupted()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    String result = "" + readLine;
    print(result + "\n");
    readLine = null;

      runOnUiThread(new Runnable() {
          @Override
          public void run() {
              controlView.codeEditText.setVisibility(View.VISIBLE);
              controlView.consoleEditText.setVisibility(View.GONE);
          }
      });

    return result;
  }


    @Override
    public void programReferenceChanged(ProgramReference fileReference) {
        runOnUiThread(() -> {
                programView.sync(false);
        });
        preferences.setProgramReference(fileReference);
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
        if (functionView != null) {
            CallableUnit callableUnit = functionView.callableUnit;
            if (callableUnit != null) {
                Map.Entry<Integer, CodeLine> entry = callableUnit.ceilingEntry(line);
                if (entry != null && entry.getKey() == line) {
                    callableUnit.setLine(line, null);
                    sync(true);
                    return;
                }
            }
        }
        throw new RuntimeException("Line " + line + " not found.");
    }

    @Override
    public void edit(int line) {
        runOnUiThread(() -> {
            FunctionView functionView = programView.currentFunctionView;
            // Append moves the cursor to the end.
            controlView.codeEditText.setText("");
            if (functionView != null) {
                CallableUnit callableUnit = functionView.callableUnit;
                Map.Entry<Integer, CodeLine> entry = callableUnit.ceilingEntry(line);
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
                lineFeedPending = false;
            }
        });
    }

    @Override
    public void clearCanvas() {
        runOnUiThread(new Runnable() {
            public void run() {
                screen.clear();

            };
        });
    }


    @Override
    public void trace(CallableUnit function, int lineNumber) {
      runOnUiThread(() -> programView.trace(function, lineNumber));
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
        shell.mainInterpreter.terminate();
        program.clearAll();
        sync(false);
    }

    public void load(ProgramReference programReference, boolean showErrors) {
        new Thread(() -> {
            shell.mainInterpreter.terminate();
            try {
                program.load(programReference);
            } catch (Exception e) {
                if (showErrors) {
                    showError("Error loading " + programReference.url, e);
                } else {
                    e.printStackTrace();
                }
            }
            sync(false);
        }).start();
    }

    public void showError(String s, Exception e) {
        runOnUiThread(() -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
            alertBuilder.setTitle(s);
            alertBuilder.setMessage("" + e);
            alertBuilder.show();
        });
    }
}
