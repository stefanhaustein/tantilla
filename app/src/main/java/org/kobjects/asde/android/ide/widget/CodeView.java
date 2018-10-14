package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.widget.LinearLayout;

import org.kobjects.asde.lang.Program;

public class CodeView extends LinearLayout {

    public CodeView(Context context, Program program) {
        super(context);
        setOrientation(VERTICAL);
        TitleView titleView = new TitleView(context);
        titleView.setTitle("Program Code");
        addView(titleView);
    }

}
