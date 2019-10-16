package org.kobjects.asde.android.ide.symbollist;

import android.graphics.Color;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatTextView;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.LeadingMarginSpan;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.TextLinks;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Span;
import org.kobjects.asde.android.ide.AnnotatedStringConverter;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

import static android.util.TypedValue.COMPLEX_UNIT_PX;
import static androidx.annotation.Dimension.PX;

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

  void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
    lineNumberView.setText(lineNumber + " ");
  }

  void setCodeLine(CodeLine codeLine, Map<Node, Exception> errors) {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    codeLine.toString(asb, errors, false);
    SpannableString spannable = AnnotatedStringConverter.toSpanned(context, asb.build(), true);

    int factor = Math.round(statementView.getTextSize() / 2);

    spannable.setSpan(new LeadingMarginSpan.Standard(codeLine.indent * factor, (codeLine.indent + 2) * factor),0,spannable.length(),0);

    statementView.setText(spannable);
    if (spannable.getSpans(0, spannable.length(), ClickableSpan.class).length > 0) {
      statementView.setMovementMethod(LinkMovementMethod.getInstance());
    } else {
      statementView.setMovementMethod(null);
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
    } else if (isSelected()) {
      setBackgroundColor(Colors.ORANGE);
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
