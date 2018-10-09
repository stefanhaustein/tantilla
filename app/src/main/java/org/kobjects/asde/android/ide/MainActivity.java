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
import android.text.InputType;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.one.EmojiOneProvider;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.Colors;
import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.android.ide.widget.ExpandableView;
import org.kobjects.asde.android.ide.widget.FunctionView;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.LineEditor;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.android.ide.widget.VariableView;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
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

public class MainActivity extends AppCompatActivity implements Console, ExpandableView.ExpandListener, LineEditor {
  private final String PROGRAM_NAME_STORAGE_KEY = "ProgramName";

  LinearLayout scrollContentView;
  View rootView;
  ScrollView scrollView;
  LinearLayout bottomAppBar;
  EmojiEditText codeEditText;
  TextView errorView;
  IconButton codeEnterButton;
  FunctionView mainFunctionView;
  Program program = new Program(this);
  Drawable systemListDivider;
  LinearLayout shellView;
  String readLine;
  ScreenAdapter screen;
  VariableView variableView;
  LinearLayout codeInputView;
  Interpreter mainInterpreter = new Interpreter(program, program.main, null);
  Interpreter shellInterpreter = new Interpreter(program, null, null);
  TreeMap<String,FunctionView> functionViews = new TreeMap<>();
  FunctionView currentFunctionView;
  SharedPreferences sharedPreferences;
  boolean autoScroll = true;
  IconButton menuButton;
  boolean fullScreenMode;

    private TitleView shellTitleView;
    private Viewport viewport;
    private EmojiPopup emojiPopup;
    private EmojiEditText consoleEditText;
    private IconButton consoleEnterButton;
    private LinearLayout consoleInputView;


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
    mainFunctionView = new FunctionView(this, "Program \"" +program.getName() + "\"", program.main, mainInterpreter, this);
    mainFunctionView.addExpandListener(this);
    mainFunctionView.setVisibility(View.GONE);
    currentFunctionView = mainFunctionView;

    shellTitleView = new TitleView(this);
    shellTitleView.setTitle("Shell");
    shellTitleView.addView(clearButton);
    shellView = new LinearLayout(this);
    shellView.setOrientation(LinearLayout.VERTICAL);
    shellView.addView(shellTitleView);

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
    scrollContentView.addView(mainFunctionView);
    scrollContentView.addView(shellView);

    scrollView = new ScrollView(this);
    scrollView.addView(scrollContentView);

    // Input Layout

    menuButton = new IconButton(this, R.drawable.baseline_menu_black_24);
    menuButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMenu();
        }
    });


    errorView = new TextView(this);
    errorView.setVisibility(View.GONE);
    errorView.setTypeface(Typeface.MONOSPACE);

    codeEditText = new EmojiEditText(this);
    codeEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    codeEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);



/*
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());

        ((AutoCompleteTextView) codeEditText).setAdapter(adapter);
*/

    codeEnterButton = new IconButton(this, R.drawable.baseline_send_black_24);
    codeEnterButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        enter();
      }
    });

    codeInputView = new LinearLayout(this);
    codeInputView.addView(codeEditText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    codeInputView.addView(codeEnterButton);

    consoleEditText = new EmojiEditText(this);
    consoleEnterButton = new IconButton(this, R.drawable.baseline_send_black_24);
    consoleEnterButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            readLine = consoleEditText.getText().toString();
            consoleEditText.setText("");
        }
    });
    consoleInputView = new LinearLayout(this);
    consoleInputView.addView(consoleEditText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    consoleInputView.addView(consoleEnterButton);
    consoleInputView.setVisibility(View.GONE);

    LinearLayout inputLayout = new LinearLayout(this);
    inputLayout.addView(menuButton);
    inputLayout.addView(codeInputView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    inputLayout.addView(consoleInputView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

    bottomAppBar = new LinearLayout(this);
    bottomAppBar.setOrientation(LinearLayout.VERTICAL);
    bottomAppBar.addView(errorView);
    bottomAppBar.addView(inputLayout);

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


  void enter() {
    errorView.setVisibility(View.GONE);
    errorView.setText("");

    String line = codeEditText.getText().toString();
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

          postScrollIfAtEnd();
          shellView.addView(inputView);
          inputPrinted = true;

          shellInterpreter.runStatementsAsync(statements, mainInterpreter);


          break;
      }
        codeEditText.setText("");
      } catch (Exception e) {
        e.printStackTrace();
        errorView.setText(e.getMessage());
        errorView.setVisibility(View.VISIBLE);
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
        int cut = s.indexOf('\n');

        if (cut == -1) {
          errorView.setVisibility(View.VISIBLE);
          errorView.setText(errorView.getText() + s);
        } else {
          TextView textView = new EmojiTextView(MainActivity.this);
          textView.setText(errorView.getText() + s.substring(0, cut));
          textView.setTypeface(Typeface.MONOSPACE);

          postScrollIfAtEnd();
          shellView.addView(textView);

          errorView.setText("");
          errorView.setVisibility(View.GONE);
          lineCount = shellView.getChildCount();
          if (cut < s.length() - 1) {
            print(s.substring(cut + 1));
          }
        }
      }
    });
    try {
      Thread.sleep(Math.round(Math.log(lineCount)));
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
                  scrollContentView.addView(functionView, 1);
                  functionViews.put(name, functionView);
                  if (expandNew) {
                      functionView.setExpanded(true, false);
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
          mainFunctionView.setExpanded(true, false);
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


  void removeFromParent(View view) {
      if (view.getParent() instanceof ViewGroup) {
          ((ViewGroup) view.getParent()).removeView(view);
      }
  }

  public void onConfigurationChanged (Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
    arrangeUi();
  }

  private void arrangeUi() {

      if (emojiPopup != null && emojiPopup.isShowing()) {
          emojiPopup.dismiss();
          emojiPopup = null;
      }

      removeFromParent(scrollView);
      removeFromParent(viewport);
      removeFromParent(bottomAppBar);

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
        if (displayHeight >= displayWidth) {
            rootLayout.setOrientation(LinearLayout.VERTICAL);

            FrameLayout overlay = new FrameLayout(this);
            overlay.addView(scrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            overlay.addView(viewport, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            rootLayout.addView(overlay, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            rootLayout.addView(bottomAppBar);
        } else {
            LinearLayout codingLayout = new LinearLayout(this);
            codingLayout.setOrientation(LinearLayout.VERTICAL);
            codingLayout.setDividerDrawable(systemListDivider);
            codingLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            codingLayout.addView(scrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
            codingLayout.addView(bottomAppBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));

            rootLayout.addView(codingLayout, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
            rootLayout.addView(viewport, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }
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
            codeInputView.setVisibility(View.GONE);
            consoleInputView.setVisibility(View.VISIBLE);
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
              codeInputView.setVisibility(View.VISIBLE);
              consoleInputView.setVisibility(View.GONE);
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
                mainFunctionView.setName("Program \"" + name + "\"");
                sharedPreferences.edit().putString(PROGRAM_NAME_STORAGE_KEY, name).commit();
            }
        });
    }

    @Override
    public void clearScreen() {
        runOnUiThread(new Runnable() {
            public void run() {
                screen.clear();
                for (int i = shellView.getChildCount() - 1; i > 0; i--) {
                    shellView.removeViewAt(i);
                }
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
    public void notifyExpanding(ExpandableView functionView, boolean animated) {
      if (functionView != currentFunctionView && functionView instanceof FunctionView) {
          if (currentFunctionView != null) {
              currentFunctionView.setExpanded(false, animated);
          }
          currentFunctionView = (FunctionView) functionView;
      }
    }


    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuButton);
        Menu mainMenu = popupMenu.getMenu();

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


        SubMenu clearMenu = mainMenu.addSubMenu("Clear");
        clearMenu.add("Clear Screen").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                clearScreen();
                return true;
            }
        });
        clearMenu.add("Erase program").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                mainInterpreter.stop();
                program.clearAll();
                mainFunctionView.setVisibility(View.GONE);
                sync(false);
                return true;
            }
        });


        Menu loadMenu = mainMenu.addSubMenu("Load");
        Menu examplesMenu = loadMenu.addSubMenu("Examples");
        for (final String name : getProgramStoragePath().list()) {
            loadMenu.add(name).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mainInterpreter.stop();
                    program.load(name);
                    sync(false);
                    return true;
                }
            });
        }
        try {
            for (final String example : MainActivity.this.getAssets().list("examples")) {
                examplesMenu.add(example).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        openExample(example);
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
                fullScreenMode = true;
                arrangeUi();
                return true;
            }
        });

        mainMenu.add(emojiPopup != null && emojiPopup.isShowing() ? "Text Keyboard" : "Emoji Keyboard").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (emojiPopup == null) {
                    emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(codeEditText);
                }
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                } else {
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
                return true;
            }
        });

        popupMenu.show();

    }

    @Override
    public void edit(String line) {
        codeEditText.setText(line);
    }
}
