package org.kobjects.asde.android.ide.symbollist;

import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.android.ide.AnnotatedStringConverter;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class CodeLineView extends LinearLayout {
  TextView lineNumberView;
  TextView statementView;
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

    statementView = new EmojiTextView(context);
    statementView.setTypeface(Typeface.MONOSPACE);

    addView(lineNumberView, new LayoutParams(Math.round(lineNumberView.getTextSize() * 3f), ViewGroup.LayoutParams.MATCH_PARENT));
    addView(statementView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

    updateColor();
  }

  void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
    lineNumberView.setText(lineNumber + " ");
  }

  void setCodeLine(CodeLine codeLine, Map<Node, Exception> errors) {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    codeLine.toString(asb, errors);
    statementView.setText(AnnotatedStringConverter.toSpanned(context, asb.build(), true));
    if (asb.spans().iterator().hasNext()) {
      statementView.setMovementMethod(LinkMovementMethod.getInstance());
    }
  }

  void setHighlighted(boolean highlighted) {
    if (highlighted != this.highlighted) {
      this.highlighted = highlighted;
      updateColor();
    }
  }

  void updateColor() {
    if (highlighted) {
      setBackgroundColor(Colors.RED);
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
