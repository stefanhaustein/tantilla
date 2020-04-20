package org.kobjects.asde.android.ide;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import com.vanniktech.emoji.EmojiEditText;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.io.Format;
import org.kobjects.asde.lang.io.MultiValidationException;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.ParsingException;


public class ControlView extends LinearLayout  {
  private final MainActivity mainActivity;
  public EmojiEditText codeEditText;
  public EmojiTextView resultView;
  IconButton enterButton;
  private LinearLayout inputLayout;

  public ControlView(MainActivity mainActivity) {
    super(mainActivity);
    setOrientation(VERTICAL);
    //this.setBackgroundColor(Colors.PRIMARY_LIGHT_FILTER);
    this.mainActivity = mainActivity;

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
    codeEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS|InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
    codeEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_FLAG_NO_FULLSCREEN);
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
    MainActivity.removeFromParent(enterButton);

    removeAllViews();

    if (landscape) {
      setOrientation(HORIZONTAL);
      LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

      LinearLayout ioView = new LinearLayout(mainActivity);
      ioView.setOrientation(VERTICAL);

      ioView.addView(resultView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      ioView.addView(inputLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      addView(ioView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

      buttonParams.gravity = Gravity.CENTER_VERTICAL;
      addView(enterButton, buttonParams);

      // addView(mainActivity.runControlView, buttonParams);

    } else {
      setOrientation(VERTICAL);
      LinearLayout topBar = new LinearLayout(mainActivity);

      LinearLayout.LayoutParams resultLayoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
      resultLayoutParams.gravity = Gravity.BOTTOM;
      topBar.addView(resultView, resultLayoutParams);
  //    topBar.addView(mainActivity.runControlView);
      LinearLayout.LayoutParams topLayoutParams = new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      topLayoutParams.gravity = Gravity.BOTTOM;
      addView(topBar, topLayoutParams);


      LinearLayout bottomBar = new LinearLayout(mainActivity);
      LinearLayout.LayoutParams inputLayoutParams = new LinearLayout.LayoutParams(
          ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
      inputLayoutParams.gravity = Gravity.BOTTOM;
      bottomBar.addView(inputLayout, inputLayoutParams);
      bottomBar.addView(enterButton);

      addView(bottomBar);
    }
  }


  public void enter() {
//    mainActivity.console.print(resultView.getText() + "\n");
  //  resultView.setText("");

    mainActivity.console.print("\n");

    String line = codeEditText.getText().toString();

    try {
      mainActivity.shell.enter(line);
    } catch (ParsingException e) {
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
      codeEditText.append(AnnotatedStringConverter.toSpanned(mainActivity, asb.build(), -1));
    } catch (MultiValidationException e) {
      e.printStackTrace();
      codeEditText.setText("");
      resultView.setText(Format.exceptionToString(e.getErrors().values().iterator().next()));
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      e.getCode().toString(asb, "", false, e.getErrors());
      codeEditText.append(AnnotatedStringConverter.toSpanned(mainActivity, asb.build(), -1));
    } catch (Throwable e) {
      e.printStackTrace();
      resultView.setText(Format.exceptionToString(e));
    }
  }
}
