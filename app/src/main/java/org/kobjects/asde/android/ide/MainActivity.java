package org.kobjects.asde.android.ide;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.Colors;
import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.android.ide.widget.ExpandableView;
import org.kobjects.asde.android.ide.widget.FunctionView;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.android.ide.widget.VariableView;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.asde.library.ui.Screen;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Console;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.typesystem.FunctionType;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity implements Console, ExpandableView.ExpandListener {
  LinearLayout contentLayout;
  LinearLayout mainLayout;
  ScrollView scrollView;
  LinearLayout bottomAppBar;
  EditText mainInput;
  TextView errorView;
  IconButton enterButton;
  FunctionView mainView;
  Program program = new Program(this);
  Drawable systemListDivider;
  LinearLayout shellLayout;
  String readLine;
  FrameLayout screenView;
  Screen screen;
  VariableView variableView;
  Interpreter mainInterpreter = new Interpreter(program, program.main, null);
  Interpreter shellInterpreter = new Interpreter(program, null, null);
  TreeMap<String,FunctionView> functionViews = new TreeMap<>();
  FunctionView currentFunctionView;
  SharedPreferences sharedPreferences;
  boolean autoScroll = true;

    private TitleView shellTitleView;

    @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EmojiManager.install(new TwitterEmojiProvider());

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
        screen.clear();
        for (int i = shellLayout.getChildCount() -1; i > 0; i--) {
          shellLayout.removeViewAt(i);
        }
      }
    });

    variableView = new VariableView(this, program);
    mainView = new FunctionView(this, "Program \"" +program.getName() + "\"", program.main, mainInterpreter);
    mainView.addExpandListener(this);
    mainView.setVisibility(View.GONE);
    currentFunctionView = mainView;

    shellTitleView = new TitleView(this);
    shellTitleView.setTitle("Shell");
    shellTitleView.addView(clearButton);
    shellLayout = new LinearLayout(this);
    shellLayout.setOrientation(LinearLayout.VERTICAL);
    shellLayout.addView(shellTitleView);

    contentLayout = new LinearLayout(this);
    contentLayout.setOrientation(LinearLayout.VERTICAL);

    ColorDrawable divider = new ColorDrawable(0x0) {
      @Override
      public int getIntrinsicHeight() {
        return Dimensions.dpToPx(MainActivity.this, 6);
      }
   };

//    contentLayout.setDividerPadding(Dimensions.dpToPx(this, 12));
    contentLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    contentLayout.setDividerDrawable(divider);
    contentLayout.addView(variableView);
    contentLayout.addView(mainView);
    contentLayout.addView(shellLayout);

    scrollView = new ScrollView(this);
    scrollView.addView(contentLayout);

    // Input Layout

    IconButton hamburgerButton = new IconButton(this, R.drawable.baseline_expand_less_black_24);
    hamburgerButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            Menu mainMenu = popupMenu.getMenu();

            mainMenu.add("New Program").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    mainInterpreter.stop();
                    program.clearAll();
                    mainView.setVisibility(View.GONE);
                    sync(false);
                    return true;
                }
            });

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

            Menu loadMenu = mainMenu.addSubMenu("Load");
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

            Menu examplesMenu = mainMenu.addSubMenu("Examples");
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

            popupMenu.show();
        }
    });

    errorView = new TextView(this);
    errorView.setVisibility(View.GONE);
    errorView.setTypeface(Typeface.MONOSPACE);

    mainInput = new EditText(this);

    enterButton = new IconButton(this, R.drawable.baseline_send_black_24);
    enterButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        enter();
      }
    });

    LinearLayout inputLayout = new LinearLayout(this);
    inputLayout.addView(hamburgerButton);
    inputLayout.addView(mainInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    inputLayout.addView(enterButton);

    bottomAppBar = new LinearLayout(this);
    bottomAppBar.setOrientation(LinearLayout.VERTICAL);
    bottomAppBar.addView(errorView);
    bottomAppBar.addView(inputLayout);

    screenView = new FrameLayout(this);
    screenView.addView(scrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    screen = new Screen(screenView);

    mainLayout = new LinearLayout(this);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setDividerDrawable(systemListDivider);
    mainLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

    mainLayout.addView(screenView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

    mainLayout.addView(bottomAppBar);

    setContentView(mainLayout);

    mainInterpreter.addStartStopListener(new StartStopListener() {
        @Override
        public void programStarted() {
            screen.clear();
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

    program.setSymbol("sprite", new GlobalSymbol(GlobalSymbol.Scope.BUILTIN, screen.spriteClassifier));
    program.setSymbol("screen", new GlobalSymbol(GlobalSymbol.Scope.BUILTIN, screen));

    String programName = sharedPreferences.getString("ProgramName", "Scratch");
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
        /*
    if (mainInterpreter.isRunning()) {
      readLine = mainInput.getText().toString();
      mainInput.setText("");
      return;
    }
    */

    errorView.setVisibility(View.GONE);
    errorView.setText("");

    String line = mainInput.getText().toString();
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
            break;
          }
          // Not
          tokenizer = program.parser.createTokenizer(line);
          tokenizer.nextToken();
          // Fall-through intended
        default:
          List<? extends Node> statements = program.parser.parseStatementList(tokenizer);
          ResolutionContext resolutionContext = new ResolutionContext(program, ResolutionContext.ResolutionMode.SHELL, new FunctionType(Types.VOID));
          for (Node statement : statements) {
              statement.resolve(resolutionContext);
          }
          TextView inputView = new TextView(this);
          inputView.setText(new CodeLine(statements).toString());
          inputView.setTextColor(Colors.SECONDARY);
          inputView.setTypeface(Typeface.MONOSPACE);

          postScrollIfAtEnd();
          shellLayout.addView(inputView);
          inputPrinted = true;

          shellInterpreter.runStatementsAsync(statements, mainInterpreter);

          break;
      }
        mainInput.setText("");
      } catch (Exception e) {
        e.printStackTrace();
        errorView.setText(e.getMessage());
        errorView.setVisibility(View.VISIBLE);
    }
  }

  int lineCount;

  void postScrollIfAtEnd() {


      if (contentLayout.getHeight() <= scrollView.getHeight() + scrollView.getScrollY()) {
          autoScroll = true;
      }

      if (autoScroll) {
          scrollView.post(new Runnable() {

          @Override
              public void run() {
                  if (contentLayout.getHeight() != scrollView.getHeight() + scrollView.getScrollY()) {
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
          TextView textView = new TextView(MainActivity.this);
          textView.setText(errorView.getText() + s.substring(0, cut));
          textView.setTypeface(Typeface.MONOSPACE);

          postScrollIfAtEnd();
          shellLayout.addView(textView);

          errorView.setText("");
          errorView.setVisibility(View.GONE);
          lineCount = shellLayout.getChildCount();
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

      Set<String> removeViews = functionViews.keySet();
      for (Map.Entry<String, GlobalSymbol> entry : program.getSymbolMap().entrySet()) {
          GlobalSymbol symbol = entry.getValue();
          if (symbol == null) {
              continue;
          }
          String name = entry.getKey();
          if (symbol.scope == GlobalSymbol.Scope.PERSISTENT && symbol.value instanceof CallableUnit) {
              removeViews.remove(name);
              if (!functionViews.containsKey(name)) {
                  FunctionView functionView = new FunctionView(this, name, (CallableUnit) symbol.value, mainInterpreter);
                  functionView.addExpandListener(this);
                  contentLayout.addView(functionView, 1);
                  functionViews.put(name, functionView);
                  if (expandNew) {
                      functionView.setExpanded(true, false);
                  }
              }
          }
      }
      for (String remove : removeViews) {
          contentLayout.removeView(functionViews.get(remove));
          functionViews.remove(remove);
      }

      if (program.main.getLineCount() > 0 && mainView.getVisibility() == View.GONE) {
          mainView.setVisibility(View.VISIBLE);
          mainView.setExpanded(true, false);
      }
      mainView.syncContent();
  }

  @Override
  public String read() {
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
                mainView.setName("Program \"" + name + "\"");
                sharedPreferences.edit().putString("ProgramName", name).commit();
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
}
