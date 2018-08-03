package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.lang.node.Statement;

import java.util.List;

public class CodeLineView extends LinearLayout {

    public static String statementListToString(List<Statement> statementList) {
        StringBuilder sb = new StringBuilder();
        for (Statement statement : statementList) {
            if (sb.length() > 0) {
                sb.append(" : ");
            }
            sb.append(statement.toString());
        }
        return sb.toString();
    }

    TextView lineNumberView;
    TextView statementView;
    List<Statement> statementList;

    public CodeLineView(Context context) {
        super(context);

        lineNumberView = new TextView(context);
        lineNumberView.setGravity(Gravity.TOP | Gravity.RIGHT);
        lineNumberView.setTypeface(Typeface.MONOSPACE);

        statementView = new TextView(context);
        statementView.setTypeface(Typeface.MONOSPACE);

        addView(lineNumberView, new LayoutParams(Math.round(lineNumberView.getTextSize() * 3f), ViewGroup.LayoutParams.MATCH_PARENT));
        addView(statementView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

    }

    void setLineNumber(int lineNumber) {
        lineNumberView.setText(lineNumber + " ");
    }
    void setStatement(List<Statement> statementList) {
        this.statementList = statementList;

        statementView.setText(statementListToString(statementList));
    }

}
