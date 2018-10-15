package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.Map;
import java.util.TreeMap;

public class VariableView extends LinearLayout {

    Program program;
    TitleView titleView;
    ExpandableList listView;
    boolean expanded = true;

    public VariableView(Context context, Program program) {
        super(context);
        setOrientation(VERTICAL);
        this.program = program;
        titleView = new TitleView(context);
        titleView.setTitle("Variables");
        addView(titleView);
        //setPadding(0, 0, 0, 10);

        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(!expanded);
            }
        });

        listView = new ExpandableList(context);

        addView(listView);
        sync();
    }

    void expand(boolean expand) {
        if (this.expanded != expand) {
            this.expanded = expand;
            listView.animateNextChanges();
            sync();
        }
    }


    public void sync() {
        if (!expanded) {
            listView.removeAllViews();
            return;
        }
        int index = 1;
        TreeMap<String, GlobalSymbol> symbolMap = program.getSymbolMap();
        for (Map.Entry<String,GlobalSymbol> entry : symbolMap.entrySet()) {
            GlobalSymbol symbol = entry.getValue();
            if (symbol == null || symbol.scope != GlobalSymbol.Scope.PERSISTENT ||
                    (symbol.value instanceof Function && !(symbol.value instanceof Array))) {
                 continue;
            }
            TextView textView;
            if (index < listView.getChildCount()) {
                  textView = (TextView) listView.getChildAt(index);
            } else {
              textView = new TextView(getContext());
                textView.setTypeface(Typeface.MONOSPACE);
              listView.addView(textView);
            }
            textView.setText(symbol.toString(entry.getKey(), true));
            index++;
        }
        while (listView.getChildCount() > index) {
            listView.removeViewAt(listView.getChildCount() -1);
        }
        setVisibility(index > 1 ? VISIBLE : GONE);
    }

}
