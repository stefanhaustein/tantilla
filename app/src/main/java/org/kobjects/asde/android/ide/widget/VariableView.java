package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Symbol;

import java.util.Map;
import java.util.TreeMap;

public class VariableView extends LinearLayout {

    Program program;
    TitleView titleView;

    public VariableView(Context context, Program program) {
        super(context);
        setOrientation(VERTICAL);
        this.program = program;
        titleView = new TitleView(context);
        titleView.setTitle("Symbols");
        addView(titleView);
        //setPadding(0, 0, 0, 10);
        sync();
    }

    public void sync() {
        int index = 1;
        TreeMap<String, Symbol> symbolMap = program.getSymbolMap();
        for (Map.Entry<String,Symbol> entry : symbolMap.entrySet()) {
           if (entry.getKey().equals("pi") || entry.getKey().equals("tau")) {
                 continue;
           }
           TextView textView;
           if (index < getChildCount()) {
              textView = (TextView) getChildAt(index);
           } else {
              textView = new TextView(getContext());
              addView(textView);
            }
            textView.setText(" " + entry.getKey() + ": " + entry.getValue().value);
            index++;
        }
        while (getChildCount() > index) {
            removeViewAt(getChildCount() -1);
        }
        setVisibility(index > 1 ? VISIBLE : GONE);
    }

}
