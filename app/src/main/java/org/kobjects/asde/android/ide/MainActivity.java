package org.kobjects.asde.android.ide;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.CodeLineView;
import org.kobjects.asde.android.ide.widget.Colors;
import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.android.ide.widget.FunctionView;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.android.ide.widget.VariableView;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.library.ui.Screen;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Console;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.node.Statement;

import java.util.List;

public class MainActivity extends AppCompatActivity implements Console {
  LinearLayout contentLayout;
  LinearLayout mainLayout;
  ScrollView scrollView;
  LinearLayout bottomAppBar;
  EditText mainInput;
  TextView errorView;
  IconButton enterButton;
  FunctionView currentFunction;
  Program program = new Program(this);
  Drawable systemListDivider;
  LinearLayout shellLayout;
  String readLine;
  FrameLayout screenView;
  Screen screen;
  VariableView variableView;
  Interpreter interpreter = new Interpreter(program);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EmojiManager.install(new TwitterEmojiProvider());

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

    TitleView titleView = new TitleView(this);
    titleView.setTitle("Shell");
    titleView.addView(clearButton);
    shellLayout = new LinearLayout(this);
    shellLayout.setOrientation(LinearLayout.VERTICAL);
    shellLayout.addView(titleView);

    contentLayout = new LinearLayout(this);
    contentLayout.setOrientation(LinearLayout.VERTICAL);

    ColorDrawable divider = new ColorDrawable(0x0) {
      @Override
      public int getIntrinsicHeight() {
        return iconPadding;
      }
   };

//    contentLayout.setDividerPadding(Dimensions.dpToPx(this, 12));
    contentLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    contentLayout.setDividerDrawable(divider);
    contentLayout.addView(variableView);
    contentLayout.addView(shellLayout);

    scrollView = new ScrollView(this);
    scrollView.addView(contentLayout);

    // Input Layout

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
    inputLayout.addView(mainInput, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    inputLayout.addView(enterButton);

    bottomAppBar = new LinearLayout(this);
    bottomAppBar.setOrientation(LinearLayout.VERTICAL);
    bottomAppBar.addView(errorView);
    bottomAppBar.addView(inputLayout);

    screenView = new FrameLayout(this);
    screenView.addView(scrollView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    screen = new Screen(screenView);
    program.classifiers.put("screen", Screen.CLASSIFIER);
    program.classifiers.put("sprite", screen.spriteClassifier);

    mainLayout = new LinearLayout(this);
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setDividerDrawable(systemListDivider);
    mainLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

    mainLayout.addView(screenView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

    mainLayout.addView(bottomAppBar);

    setContentView(mainLayout);

    interpreter.addStartStopListener(new StartStopListener() {
        @Override
        public void programStarted() {
            screen.clear();
        }

        @Override
        public void programStopped() {

        }
    });

    print("  " + (Runtime.getRuntime().totalMemory() / 1024) + "K SYSTEM  "
            + Runtime.getRuntime().freeMemory() + " ASDE BYTES FREE\n\n");

  }

  FunctionView getCurrentFunction() {
    if (currentFunction == null) {
      currentFunction = new FunctionView(this, interpreter, program.main);
      contentLayout.addView(currentFunction, 0);
    }
    return currentFunction;
  }

  void enter() {
    if (interpreter.isRunning()) {
      readLine = mainInput.getText().toString();
      mainInput.setText("");
      return;
    }

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
            getCurrentFunction().put(lineNumber, program.parser.parseStatementList(tokenizer));
            break;
          }
          // Not
          tokenizer = program.parser.createTokenizer(line);
          tokenizer.nextToken();
          // Fall-through intended
        default:
          List<Statement> statements = program.parser.parseStatementList(tokenizer);

          TextView inputView = new TextView(this);
          inputView.setText(CodeLineView.statementListToString(statements));
          inputView.setTextColor(Colors.SECONDARY);
          inputView.setTypeface(Typeface.MONOSPACE);

          shellLayout.addView(inputView);
          inputPrinted = true;

          interpreter.runStatements(statements);

          break;
      }
        mainInput.setText("");
      } catch (Exception e) {
        e.printStackTrace();
        errorView.setText(e.getMessage());
        errorView.setVisibility(View.VISIBLE);
    }
    variableView.sync();
  }

  int lineCount;

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
          shellLayout.addView(textView);
          errorView.setText("");
   //       errorView.setVisibility(View.GONE);
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

}
