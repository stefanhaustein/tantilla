package org.kobjects.asde.android.ide;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.SymbolTitleView;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.List;

public class VariableView extends LinearLayout {

    public VariableView(MainActivity mainActivity, String name, GlobalSymbol symbol) {
        super(mainActivity);
        setOrientation(VERTICAL);

        List<String> subtitles = new ArrayList<>();

        if (symbol.getType() != null) {
            subtitles.add("T: " + symbol.getType());
        }
        if (symbol.initializer != null) {
            subtitles.add("I: " + symbol.initializer);
        }
        if (symbol.value != null) {
            subtitles.add("V: " + symbol.value);
        }

        SymbolTitleView titleView = new SymbolTitleView(mainActivity, mainActivity.colors.yellow, 'V', name, subtitles, view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
                new RenameFlow(mainActivity, name).start();
                return true;
            });
            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                new DeleteFlow(mainActivity, name).start();
                return true;
            });

            popupMenu.show();
        });
        addView(titleView);
    }
}
