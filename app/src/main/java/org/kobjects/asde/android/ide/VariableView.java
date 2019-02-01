package org.kobjects.asde.android.ide;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.SymbolTitleView;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VariableView extends LinearLayout {

    String name;
    Object value;
    final GlobalSymbol symbol;
    SymbolTitleView titleView;
    MainActivity mainActivity;

    public VariableView(MainActivity mainActivity, String name, GlobalSymbol symbol) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.symbol = symbol;
        this.name = name;
        setOrientation(VERTICAL);
        sync();
    }

    void sync() {
        if (value == symbol.value && titleView != null) {
            return;
        }
        if (titleView != null) {
            removeView(titleView);
        }
        StringBuilder sb = new StringBuilder(" ");
        value = symbol.value;
        if (value != null) {
            sb.append(value);
        } else if (symbol.initializer instanceof LetStatement && symbol.initializer.children[0] instanceof Literal) {
            sb.append(((Literal) symbol.initializer.children[0]).value);
        } else if (symbol.getType() != null) {
            sb.append(symbol.getType() );
        } else {
            sb.append("?");
        }
        titleView = new SymbolTitleView(mainActivity, mainActivity.colors.yellow, 'V', name, Collections.singletonList(sb.toString()), view -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), view);
                popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                    mainActivity.controlView.codeEditText.setText(String.valueOf(symbol.initializer));
                    return true;
                });
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
            addView(titleView, 0);

        }
}


