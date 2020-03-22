package org.kobjects.asde.android.ide;

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

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.abcnotation.AbcScore;
import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.program.ProgramTitleView;
import org.kobjects.asde.android.ide.program.ProgramView;
import org.kobjects.asde.android.ide.property.PropertyView;
import org.kobjects.asde.android.ide.widget.ResizableFrameLayout;
import org.kobjects.asde.android.library.ui.PenType;
import org.kobjects.asde.android.library.ui.ScreenType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.BuiltinFunction;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.program.ProgramListener;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.io.Shell;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.android.library.ui.DpadAdapter;
import org.kobjects.asde.android.library.ui.SpriteAdapter;
import org.kobjects.asde.android.library.ui.TextBoxType;
import org.kobjects.graphics.Screen;
import org.kobjects.abcnotation.SampleManager;
import org.kobjects.asde.lang.function.FunctionType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.SortedMap;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {
  static final int SAVE_EXTERNALLY_REQUEST_CODE = 420;
  static final int LOAD_EXTERNALLY_REQUEST_CODE = 421;
  static final int OPEN_EXTERNALLY_REQUEST_CODE = 422;
  static final int PICK_SHORTCUT_ICON_REQUEST_CODE = 423;

  static final FunctionType FUNCTION_VOID_0 = new FunctionType(Types.VOID);

  static final String[] HINTS = {
      "Try the classic:\n\n  10 PRINT \"Hello\"\n  20 GOTO 10",
      "ASDE is a simple programming environment for mobile devices.",
  };

  public static void removeFromParent(View view) {
    if (view != null && view.getParent() instanceof ViewGroup) {
      ((ViewGroup) view.getParent()).removeView(view);
    }
  }

  LinearLayout scrollContentView;
  FrameLayout trueRootView;
  public View rootView;
  ScrollView mainScrollView;
  ScrollView leftScrollView;
  FloatingActionButton runButton;
  public ControlView controlView;
  public AndroidConsole console;
  public Program program;
  public TextOutputView textOutputView;
  ResizableFrameLayout resizableFrameLayout;
  //  public ProgramControl mainControl = new ProgramControl(program);
  // ProgramControl shellControl = new ProgramControl(program);
  AsdePreferences preferences;
  public boolean fullScreenMode;
  public ProgramView programView;
  public ProgramTitleView programTitleView;
  public Shell shell;
  DpadAdapter dpadAdapter;


  /**
   * The view that displays the code in landscape mode
   */
  private LinearLayout codeView;
  private View currentCodeViewOwner;


  RunControlView runControlView;
  Screen screen;
  boolean windowMode;
  boolean runningFromShortcut;

  ShortcutHandler shortcutHandler;

  public SortedMap<Integer, String> copyBuffer = new TreeMap<>();

  @Override
  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    if (!screen.dispatchKeyEvent(keyEvent)) {
      return super.dispatchKeyEvent(keyEvent);
    }
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    trueRootView = new FrameLayout(this);
    setContentView(trueRootView);

    console = new AndroidConsole(this);
    program = new Program(console);
    shell = new Shell(program);

    preferences = new AsdePreferences(this);

    if (!preferences.getHelloCopied()) {
      new Thread(() -> {
        try {
          preferences.setHelloCopied(true);
          InputStream is = getAssets().open("Hello");
          OutputStream os = new FileOutputStream(new File(getProgramStoragePath(), "Hello"));
          while (true) {
            int i = is.read();
            if (i < 0) {
              break;
            }
            os.write(i);
          }
          is.close();
          os.close();
        } catch (IOException e) {
          e.printStackTrace();
        }

      }).start();
    }

    EmojiManager.install(new TwitterEmojiProvider());

    programView = new ProgramView(this);
    programTitleView = new ProgramTitleView(this);
    textOutputView = new TextOutputView(this);

    SampleManager sampleManager = new SampleManager(this);

    resizableFrameLayout = new ResizableFrameLayout(this);

    scrollContentView = new LinearLayout(this);
    scrollContentView.setOrientation(LinearLayout.VERTICAL);

    runButton = new FloatingActionButton(this);
    runButton.setImageResource(R.drawable.ic_asde);
//    runButton.setPadding(0, 0, 0, 0);
    runButton.setScaleType(ImageView.ScaleType.CENTER);
  //  runButton.setIm
    //runButton.setMaxWidth(10);
    //runButton.setMaxHeight(10);

//    runButton.setScaleType(ImageView.ScaleType.CENTER);
  //  runButton.setSize(FloatingActionButton.SIZE_MINI);

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

    mainScrollView = new ScrollView(this);
    mainScrollView.addView(scrollContentView);

    leftScrollView = new ScrollView(this);
    runControlView = new RunControlView(this, runButton);
    controlView = new ControlView(this);
    screen = new Screen(this);

    new Thread(() -> {
      long callTime = System.currentTimeMillis();
      while (true) {
        long lastCall = callTime;
        callTime = System.currentTimeMillis();
        if (shell.mainControl.getState() != ProgramControl.State.PAUSED && callTime - lastCall > 5) {
          // TODO(haustein): Figure out what to do about this
          // screen.animate(callTime - lastCall);
        }
        try {
          Thread.sleep(15);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();

    dpadAdapter = new DpadAdapter(screen.dpad);
    program.addBuiltin("Screen", ScreenType.TYPE);
    program.addBuiltin("screen", screen);
    program.addBuiltin("EdgeMode", SpriteAdapter.EDGE_MODE);
    program.addBuiltin("XAlign", ScreenType.X_ALIGN);
    program.addBuiltin("YAlign", ScreenType.Y_ALIGN);
    program.addBuiltin("Sprite", SpriteAdapter.TYPE);
    program.addBuiltin("TextBox", TextBoxType.TYPE);
    program.addBuiltin("Pen", PenType.TYPE);
    program.addBuiltin("dpad", dpadAdapter);
    program.addBuiltin("sleep", new BuiltinFunction((evaluationContext, paramCount) -> {
            try {
              Thread.sleep(((Number) evaluationContext.getParameter(0)).intValue());
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
            return null;
          }, "Pause the execution for the given number of seconds.", Types.VOID, Types.FLOAT));

    program.addBuiltin("play", new Callable() {
      @Override
      public Property getDeclaringSymbol() {
        return null;
      }

      @Override
      public FunctionType getType() {
        return new FunctionType(Types.VOID, Types.STR);
      }

      @Override
      public Object call(EvaluationContext evaluationContext, int paramCount) {
        new AbcScore(sampleManager, String.valueOf(evaluationContext.getParameter(0))).play();
        return null;
      }
    });


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      mainScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
          // Manually scrolling up turns off autoScroll.
          if (scrollY < oldScrollY) {
            console.autoScroll = false;
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

      console.print("\n");
      console.print(HINTS[(int)(Math.random()*HINTS.length)]);
      console.print("\n\n\n\n");

      /*
      console.print("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
          + Runtime.getRuntime().freeMemory() + " ASDE BYTES FREE\n\n");
*/
      programReference = preferences.getProgramReference();
    }

    program.addProgramNameChangeListener((event) -> {
      programView.requestSynchronization();
      preferences.setProgramReference(program.reference);
      runOnUiThread(() -> {
        if (rootView != null) {
          rootView.setBackgroundColor(getBackgroundColor());
        }
      });
      if (event == ProgramListener.Event.CHANGED) {
        triggerAutosave();
      }
    });
    program.addSymbolChangeListener(symbol -> triggerAutosave());

    load(programReference, false, runningFromShortcut);
  }


  boolean autosaveTriggered;

  void triggerAutosave() {
    if (program.reference.urlWritable && !autosaveTriggered) {
      new Thread(() -> {
        try {
          Thread.sleep(100);
          autosaveTriggered = false;
          program.save(program.reference);
          programTitleView.refresh();
          programView.refresh();
        } catch (Exception e) {
          autosaveTriggered = false;
        }
      }).start();
    }
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


  public void onConfigurationChanged(Configuration newConfig) {
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
          console.showError("Icon loading error", e);
        }
      }


      Uri uri = resultData.getData();
      String displayName = "Unknown";
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
            console.showError("Error loading external file", e);
          }
          break;

        case SAVE_EXTERNALLY_REQUEST_CODE:
          try {
            program.save(programReference);
          } catch (Exception e) {
            console.showError("Error saving external file", e);
          }
          break;
      }
    } else {
      super.onActivityResult(requestCode, resultCode, resultData);
    }
  }

  int getBackgroundColor() {
    return Colors.BACKGROUND;
  }

  void arrangeUi() {
    runOnUiThread(() -> arrangeUiImpl());
  }

  private void arrangeUiImpl() {
    if (rootView != null) {
      rootView.setBackgroundColor(0);
    }

    removeFromParent(leftScrollView);
    removeFromParent(mainScrollView);
    removeFromParent(screen.view);
    removeFromParent(controlView);
    removeFromParent(programView);
    removeFromParent(programTitleView);
    removeFromParent(codeView);
    removeFromParent(runControlView);
    removeFromParent(resizableFrameLayout);
    removeFromParent(textOutputView);
    removeFromParent(runButton);

    if (leftScrollView != null) {
      leftScrollView.removeAllViews();
    }

    codeView = null;

    Display display = getWindowManager().getDefaultDisplay();
    int displayWidth = display.getWidth();
    int displayHeight = display.getHeight();

    if (programView.currentPropertyView != null) {
      programView.currentPropertyView.setExpanded(false, false);
    }

    if (fullScreenMode) {
      FrameLayout rootLayout = new FrameLayout(this);

      rootLayout.addView(mainScrollView);
      rootLayout.addView(screen.view);
      textOutputView.titleView.setVisibility(View.GONE);

      FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
      rootLayout.addView(runControlView, layoutParams);
      screen.view.setBackgroundColor(0);

      scrollContentView.addView(textOutputView);
      rootView = rootLayout;

    } else {
      LinearLayout rootLayout = new LinearLayout(this);
      FrameLayout mainView = new FrameLayout(this);

      rootLayout.setDividerDrawable(new ColorDrawable(Colors.PRIMARY_LIGHT_FILTER));
      rootLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

      boolean running = runButton.getVisibility() != View.VISIBLE;

      if (displayHeight >= displayWidth) {

        textOutputView.titleView.setVisibility(View.VISIBLE);

        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(programTitleView);
        rootLayout.addView(mainView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
    //    View dividerView = new View(this);
      //  dividerView.setBackgroundColor(Colors.PRIMARY_FILTER);
        //rootLayout.addView(dividerView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, 0));
        rootLayout.addView(controlView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        controlView.arrangeButtons(false);
        mainView.addView(mainScrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollContentView.addView(programView, 0);
        scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        scrollContentView.addView(textOutputView);

        rootLayout.setClipChildren(false);

      } else {
        textOutputView.titleView.setVisibility(View.GONE);

        rootLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout leftView = new LinearLayout(this);
        leftView.setOrientation(LinearLayout.VERTICAL);
        leftView.addView(programTitleView);
        leftScrollView.addView(programView);
        leftView.addView(leftScrollView);

        LinearLayout rightView = new LinearLayout(this);
        rightView.setOrientation(LinearLayout.VERTICAL);
        rightView.setDividerDrawable(new ColorDrawable(Colors.PRIMARY_FILTER));
        rightView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        controlView.arrangeButtons(true);

        boolean showCodeView = windowMode || !running;

        if (showCodeView) {
          codeView = new LinearLayout(this);
          codeView.setOrientation(LinearLayout.VERTICAL);
          scrollContentView.addView(codeView, 0);
        } else {
          scrollContentView.addView(textOutputView);
        }
        scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);

        rightView.addView(mainView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        rightView.addView(controlView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mainView.addView(mainScrollView);

        rootLayout.addView(leftView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        rootLayout.addView(rightView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, showCodeView ? 2 : 1));

        rightView.setClipChildren(false);
      }

      FrameLayout.LayoutParams runButtonParams = new FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.BOTTOM);
      runButtonParams.bottomMargin = Dimensions.dpToPx(this, -12);
      runButtonParams.rightMargin = Dimensions.dpToPx(this, 24);
      mainView.addView(runButton, runButtonParams);


      if (windowMode && running) {
        resizableFrameLayout.addView(screen.view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        FrameLayout.LayoutParams resizableFrameLayoutParmas =
            new FrameLayout.LayoutParams(Dimensions.dpToPx(this, 120), Dimensions.dpToPx(this, 120));

        resizableFrameLayoutParmas.rightMargin = Dimensions.dpToPx(this, 12);
        resizableFrameLayoutParmas.topMargin = Dimensions.dpToPx(this, 36);

        resizableFrameLayoutParmas.gravity = Gravity.TOP | Gravity.RIGHT;
        screen.view.setBackgroundColor(getBackgroundColor());

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
      textOutputView.syncContent();
      rootView = rootLayout;
    }
    rootView.setBackgroundColor(getBackgroundColor());

    setContentView(rootView);
  }

  public void onBackPressed() {
    if (fullScreenMode && !runningFromShortcut) {
      shell.mainControl.abort();
    } else {
      super.onBackPressed();
    }
  }


  public File getProgramStoragePath() {
    File result = new File(getFilesDir(), "programs");
    // Mkdirs needed to make sure there is a programs storage path.
    result.mkdirs();
    return result;
  }

  public void eraseProgram() {
    shell.mainControl.abort();
    program.deleteAll();
  }

  public void load(ProgramReference programReference, boolean showErrors, boolean run) {
    new Thread(() -> {
      shell.mainControl.abort();
      try {
        program.load(programReference);
        if (run) {
          shell.mainControl.start();
        }
      } catch (Exception e) {
        if (showErrors) {
          console.showError("Error loading " + programReference.url, e);
        } else {
          e.printStackTrace();
        }
      }
    }).start();
  }


  public boolean sharedCodeViewAvailable() {
    return codeView != null;
  }


  public LinearLayout obtainSharedCodeView(View owner) {
    if (owner != currentCodeViewOwner) {
      codeView.removeAllViews();
      codeView.setOnTouchListener(null);
      if (currentCodeViewOwner instanceof PropertyView) {
        ((PropertyView) currentCodeViewOwner).setExpanded(false, false);
      }
    }
    currentCodeViewOwner = owner;
    return codeView;
  }


  public boolean isUnsaved() {
    return program.hasUnsavedChanges
        || (program.reference.name.isEmpty() && !program.isEmpty());
  }

}
