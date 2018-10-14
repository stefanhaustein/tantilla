package org.kobjects.asde.android.ide;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
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
import org.kobjects.asde.android.ide.widget.CodeView;
import org.kobjects.asde.android.ide.widget.Colors;
import org.kobjects.asde.android.ide.widget.ControlView;
import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.android.ide.widget.FunctionView;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.LineEditor;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.android.ide.widget.VariableView;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.LocalStack;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.asde.library.ui.DpadAdapter;
import org.kobjects.asde.library.ui.ScreenAdapter;
import org.kobjects.graphics.Viewport;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Console;
import org.kobjects.asde.lang.Interpreter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements Console, FunctionView.ExpandListener, LineEditor {
  private final String PROGRAM_NAME_STORAGE_KEY = "ProgramName";

  LinearLayout scrollContentView;
  public View rootView;
  ScrollView scrollView;
  ControlView controlView;
  FunctionView mainFunctionView;
  Program program = new Program(this);
  Drawable systemListDivider;
  LinearLayout outputView;
  public String readLine;
  ScreenAdapter screen;
  VariableView variableView;
  public Interpreter mainInterpreter = new Interpreter(program, program.main, new LocalStack());
  Interpreter shellInterpreter = new Interpreter(program, null, new LocalStack());
  TreeMap<String,FunctionView> functionViews = new TreeMap<>();
  FunctionView currentFunctionView;
  SharedPreferences sharedPreferences;
  boolean autoScroll = true;
  public boolean fullScreenMode;
  CodeView codeView;

  private TitleView outputTitleView;
  private Viewport viewport;
  private boolean lineFeedPending;

  @Override
  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    if (!viewport.dispatchKeyEvent(keyEvent)) {
      return super.dispatchKeyEvent(keyEvent);
    }
    return true;
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EmojiManager.install(new EmojiOneProvider());

    sharedPreferences = getPreferences(MODE_PRIVATE);

    int[] attrs = { android.R.attr.listDivider };
    TypedArray ta = getApplicationContext().obtainStyledAttributes(attrs);
    systemListDivider = ta.getDrawable(0);
    ta.recycle();
    final int iconPadding = Dimensions.dpToPx(this, 12);

        IconButton clearButton = new IconButton(this, R.drawable.baseline_delete_black_24);
    clearButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clearScreen();
      }
    });


    variableView = new VariableView(this, program);
    codeView = new CodeView(this, program);
    mainFunctionView = new FunctionView(this, "Main Block", program.main, mainInterpreter, this);
    mainFunctionView.addExpandListener(this);
    mainFunctionView.setVisibility(View.GONE);
    currentFunctionView = mainFunctionView;

    outputTitleView = new TitleView(this);
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

//    scrollContentView.setDividerPadding(Dimensions.dpToPx(this, 12));
    scrollContentView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    scrollContentView.setDividerDrawable(divider);
    scrollContentView.addView(variableView);
    scrollContentView.addView(codeView);
    scrollContentView.addView(mainFunctionView);
    scrollContentView.addView(outputView);

    scrollView = new ScrollView(this);
    scrollView.addView(scrollContentView);

    controlView = new ControlView(this);

    viewport = new Viewport(this);

    screen = new ScreenAdapter(viewport);


    shellInterpreter.addStartStopListener(new StartStopListener() {
        @Override
        public void programStarted() {
            // screen.cls();
        }

        @Override
        public void programStopped() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sync(true);
                }
            });
        }
    });

    program.setValue(GlobalSymbol.Scope.BUILTIN,"screen", screen);
    program.setValue(GlobalSymbol.Scope.BUILTIN,"sprite", screen.spriteClassifier);
    program.setValue(GlobalSymbol.Scope.BUILTIN, "text", screen.textClassifier);
    program.setValue(GlobalSymbol.Scope.BUILTIN, "dpad", new DpadAdapter(viewport.dpad));
//    program.setValue(GlobalSymbol.Scope.BUILTIN,"pen", screen.penClassifier);

    arrangeUi();

    String programName = sharedPreferences.getString(PROGRAM_NAME_STORAGE_KEY, "Scratch");
    if (new File(getProgramStoragePath(), programName).exists()) {
        try {
            program.load(programName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    sync(false);

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
    }


  private LinearLayout createBottomAppBar() {
      // Left button bar


      return controlView;
  }


  public void enter() {
    String line = controlView.codeEditText.getText().toString();
    if (line.isEmpty()) {
        print("\n");
        return;
    }
    ExpressionParser.Tokenizer tokenizer = program.parser.createTokenizer(line);
    boolean inputPrinted = false;
    try {
      tokenizer.nextToken();
      switch (tokenizer.currentType) {
        case EOF:
          break;
        case NUMBER:
          int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
          tokenizer.nextToken();
          if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.IDENTIFIER || "?".equals(tokenizer.currentValue)) {
            currentFunctionView.put(lineNumber, program.parser.parseStatementList(tokenizer));
            sync(true);
            program.save(null);
            break;
          }
          // Not
          tokenizer = program.parser.createTokenizer(line);
          tokenizer.nextToken();
          // Fall-through intended
        default:
          List<? extends Node> statements = program.parser.parseStatementList(tokenizer);
          program.processDeclarations(statements);
          TextView inputView = new EmojiTextView(this);
          inputView.setText(new CodeLine(statements).toString());
          inputView.setTextColor(Colors.SECONDARY);
          inputView.setTypeface(Typeface.MONOSPACE);


          if (lineFeedPending) {
              print("");
          }

          outputView.addView(inputView);
          inputPrinted = true;
            postScrollIfAtEnd();


          shellInterpreter.runStatementsAsync(statements, mainInterpreter);


          break;
      }
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

  void sync(boolean expandNew) {
      variableView.sync();

      Set<String> removeViews = new HashSet<String>(functionViews.keySet());
      for (Map.Entry<String, GlobalSymbol> entry : program.getSymbolMap().entrySet()) {
          GlobalSymbol symbol = entry.getValue();
          if (symbol == null) {
              continue;
          }
          String name = entry.getKey();
          if (symbol.scope == GlobalSymbol.Scope.PERSISTENT && symbol.value instanceof CallableUnit) {
              removeViews.remove(name);
              if (!functionViews.containsKey(name)) {
                  FunctionView functionView = new FunctionView(this, name, (CallableUnit) symbol.value, mainInterpreter, this);
                  functionView.addExpandListener(this);
                  scrollContentView.addView(functionView, 2);
                  functionViews.put(name, functionView);
                  if (expandNew) {
                 //     functionView.setExpanded(true, false);
                  }
              }
          }
      }
      for (String remove : removeViews) {
          scrollContentView.removeView(functionViews.get(remove));
          functionViews.remove(remove);
      }

      if (program.main.getLineCount() > 0 && mainFunctionView.getVisibility() == View.GONE) {
          mainFunctionView.setVisibility(View.VISIBLE);
          //mainFunctionView.setExpanded(true, false);
      }
      mainFunctionView.syncContent();

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
      if (view.getParent() instanceof ViewGroup) {
          ((ViewGroup) view.getParent()).removeView(view);
      }
  }

  public void onConfigurationChanged (Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    arrangeUi();
  }

  public void arrangeUi() {

      controlView.dismissEmojiPopup();


      removeFromParent(scrollView);
      removeFromParent(viewport);
      removeFromParent(controlView);

      Display display = getWindowManager().getDefaultDisplay();
      int displayWidth = display.getWidth();
      int displayHeight = display.getHeight();

      if (fullScreenMode) {
         rootView = viewport;
//         setContentView(viewport, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  //       rootView = null;

      } else {
          LinearLayout rootLayout = new LinearLayout(this);
        rootLayout = new LinearLayout(this);
        rootLayout.setDividerDrawable(systemListDivider);
        rootLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        FrameLayout overlay = new FrameLayout(this);
        overlay.addView(scrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.addView(viewport, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (displayHeight >= displayWidth) {
            rootLayout.setOrientation(LinearLayout.VERTICAL);
            rootLayout.addView(overlay, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            rootLayout.addView(controlView,  new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            controlView.arrangeButtons(false);
        } else {
            rootLayout.addView(overlay, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            rootLayout.addView(controlView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            controlView.arrangeButtons(true);
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
  public String read() {
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
    public File getProgramStoragePath() {
        File programDir = new File(getFilesDir(), "programs");
        programDir.mkdir();
        return programDir;
    }

    @Override
    public void programNameChangedTo(final String name) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //  mainFunctionView.setName("Program \"" + name + "\"");
                sharedPreferences.edit().putString(PROGRAM_NAME_STORAGE_KEY, name).commit();
            }
        });
    }

    @Override
    public void clearScreen() {
        runOnUiThread(new Runnable() {
            public void run() {
                for (int i = outputView.getChildCount() - 1; i > 0; i--) {
                    outputView.removeViewAt(i);
                }
                controlView.resultView.setText("");
                lineFeedPending = false;
                screen.clear();
            }
        });
    }

    public void openExample(String name) {
      try {
          mainInterpreter.stop();
          program.load("Scratch", getAssets().open("examples/" + name));
          sync(false);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
    }

    @Override
    public void notifyExpanding(FunctionView functionView, boolean animated) {
      if (functionView != currentFunctionView && functionView instanceof FunctionView) {
          if (currentFunctionView != null) {
              currentFunctionView.setExpanded(false, animated);
          }
          currentFunctionView = (FunctionView) functionView;
      }
    }

    @Override
    public void edit(String line) {
        controlView.codeEditText.setText(line);
    }

    public void eraseProgram() {
        mainInterpreter.stop();
        program.clearAll();
        mainFunctionView.setVisibility(View.GONE);
        sync(false);
    }

    public void load(String name) {
        mainInterpreter.stop();
        program.load(name);
        sync(false);
    }
}
