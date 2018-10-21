package org.kobjects.asde.android.ide;

import android.content.Context;
import android.widget.LinearLayout;

import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.List;

public class VariableView extends LinearLayout {

    public VariableView(Context context, String name, GlobalSymbol symbol) {
        super(context);
        setOrientation(VERTICAL);

        List<String> subtitles = new ArrayList<>();

        if (symbol.type != null) {
            subtitles.add("T: " + symbol.type);
        }
        if (symbol.initializer != null) {
            subtitles.add("I: " + symbol.initializer);
        }
        if (symbol.value != null) {
            subtitles.add("V: " + symbol.value);
        }

        SymbolTitleView titleView = new SymbolTitleView(context, 0xffffe0b2, 'V', name, subtitles);
        addView(titleView);
    }
}
