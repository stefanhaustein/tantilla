package org.kobjects.asde.android.ide.function;

import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.SpannableString;
import android.text.style.LeadingMarginSpan;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.android.ide.text.AnnotatedStringConverter;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.TextViews;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.statement.Statement;

import java.util.Map;

public class CodeLineView extends LinearLayout {
  TextView lineNumberView;
  EmojiTextView statementView;
  boolean odd;
  boolean highlighted;
  int lineNumber;
  MainActivity context;

  public CodeLineView(MainActivity context, boolean odd) {
    super(context);
    this.context = context;
    this.odd = odd;

    lineNumberView = new AppCompatTextView(context);
    lineNumberView.setGravity(Gravity.TOP | Gravity.RIGHT);
    lineNumberView.setTypeface(Typeface.MONOSPACE);
    lineNumberView.setAlpha(0.5f);

    statementView = new EmojiTextView(context);
    statementView.setTypeface(Typeface.MONOSPACE);

    addView(lineNumberView, new LayoutParams(Math.round(lineNumberView.getTextSize() * 3f), ViewGroup.LayoutParams.MATCH_PARENT));
    addView(statementView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

    updateColor();

    /*
    setOnDragListener(new OnDragListener() {
      @Override
      public boolean onDrag(View view, DragEvent dragEvent) {
        setBackgroundColor(0xff000000 | (int) (Math.random() * 0xffffff));
        return true;
      }
    });
*/



  }

  public void setSelected(boolean selected) {
    super.setSelected(selected);
    updateColor();
  }

  void setCodeLine(int index, Statement statement, Map<Node, Exception> errors) {
    this.lineNumber = index;
    lineNumberView.setText(lineNumber + " ");
    statementView.setText("");

    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    int indent = statement.getIndent();
    statement.toString(asb, errors, false);
    SpannableString spannable = AnnotatedStringConverter.toSpanned(context, asb.build(), lineNumber);

    int factor = Math.round(statementView.getTextSize() / 2);

    spannable.setSpan(new LeadingMarginSpan.Standard(indent * factor, (indent + 2) * factor),0,spannable.length(),0);

    statementView.setTextScaleX(0.9f);
    // setText breaks adjustMovementMethod below...
    statementView.append(spannable);

    TextViews.adjustMovementMethod(statementView);
  }

  public void setHighlighted(boolean highlighted) {
    if (highlighted != this.highlighted) {
      this.highlighted = highlighted;
      updateColor();
    }
  }

  void updateColor() {
    if (highlighted) {
      setBackgroundColor(Colors.RED);
    } else if (isSelected()) {
      setBackgroundColor(Colors.DARK_ORANGE);
    } else if (odd) {
      setBackgroundColor(Colors.PRIMARY_LIGHT_FILTER);
    } else {
      setBackgroundColor(0);
    }

  }

  @Override
  public String toString() {
    return lineNumberView.getText().toString().trim() + " " + statementView.getText().toString().trim();
  }
}
