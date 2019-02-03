package org.kobjects.asde.android.ide.symbollist;

import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.SymbolTitleView;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.Collections;

public class VariableView extends SymbolView {
    Object value = this;
    final GlobalSymbol symbol;
    MainActivity mainActivity;

    public VariableView(MainActivity mainActivity, String name, GlobalSymbol symbol) {
        super(mainActivity, name);
        this.mainActivity = mainActivity;
        this.symbol = symbol;
        titleView.setTypeIndicator('V', mainActivity.colors.yellow);
        titleView.setMoreClickListener(view -> {
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
        syncContent();
    }

    @Override
    public void syncContent() {
        if (value == symbol.value) {
            return;
        }
        StringBuilder sb = new StringBuilder(" ");
        value = symbol.value;
        if (value != null) {
            sb.append(value);
        } else if (symbol.initializer instanceof LetStatement && symbol.initializer.children[0] instanceof Literal) {
            sb.append(((Literal) symbol.initializer.children[0]).value);
        } else if (symbol.getType() != null) {
            sb.append(symbol.getType());
        } else {
            sb.append("?");
        }
        titleView.setSubtitles(Collections.singletonList(sb.toString()));
    }
}


