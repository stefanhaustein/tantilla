package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.node.Node;

import java.util.Collections;

public class CodeLineView extends LinearLayout {
    TextView lineNumberView;
    TextView statementView;

    public CodeLineView(Context context) {
        super(context);

        lineNumberView = new AppCompatTextView(context);
        lineNumberView.setGravity(Gravity.TOP | Gravity.RIGHT);
        lineNumberView.setTypeface(Typeface.MONOSPACE);

        statementView = new EmojiTextView(context);
        statementView.setTypeface(Typeface.MONOSPACE);

        addView(lineNumberView, new LayoutParams(Math.round(lineNumberView.getTextSize() * 3f), ViewGroup.LayoutParams.MATCH_PARENT));
        addView(statementView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

    }

    void setLineNumber(int lineNumber) {
        lineNumberView.setText(lineNumber + " ");
    }

    void setCodeLine(CodeLine codeLine) {
        AnnotatedStringBuilder sb = new AnnotatedStringBuilder();
        codeLine.toString(sb, Collections.<Node, Exception>emptyMap());
        statementView.setText(codeLine.toString());
    }

    @Override
    public String toString() {
        return lineNumberView.getText().toString().trim() + " " + statementView.getText().toString().trim();
    }
}
