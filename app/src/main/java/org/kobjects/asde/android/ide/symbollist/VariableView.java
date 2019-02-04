package org.kobjects.asde.android.ide.symbollist;

import android.graphics.Typeface;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.ArrayType;
import org.kobjects.asde.lang.node.ArrayLiteral;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.typesystem.Type;

import java.util.Collections;


public class VariableView extends SymbolView {
    Object value = this;
    MainActivity mainActivity;

    public VariableView(MainActivity mainActivity, String name, GlobalSymbol symbol) {
        super(mainActivity, name, symbol);
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

    void addLine(ExpandableList target, int indent, Object value) {
        TextView initializerView = new TextView(mainActivity);
        initializerView.setTypeface(Typeface.MONOSPACE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        sb.append(value);
        initializerView.setText(sb);
        target.addView(initializerView);
    }

    void addChildList(ExpandableList target, int indent, Node node) {
        for (int i = 0; i < node.children.length; i++) {
            Node child = node.children[i];
            boolean last = i == node.children.length - 1;
            if (child instanceof ArrayLiteral && isMultiDim(child.returnType())) {
                if (i == 0) {
                    addLine(target, indent, "{");
                }
                addChildList(target, indent + 1, child);
                addLine(target, indent, last ? "}" : "}, {");
            } else {
                addLine(target, indent, last ? String.valueOf(child) : (child + ","));
            }
        }
    }


    public boolean isMultiDim(Type type) {
        return (type instanceof ArrayType && ((ArrayType) type).getReturnType() instanceof ArrayType);
    }

    @Override
    public void syncContent() {
        titleView.setBackgroundColor(symbol.errors.size() > 0 ? mainActivity.colors.accentLight : expanded ? mainActivity.colors.primaryLight : 0);

        StringBuilder sb = new StringBuilder(" ");
        value = symbol.value;
        if (value != null) {
            sb.append(value);
        } else if (symbol.initializer instanceof LetStatement && symbol.initializer.children[0] instanceof Literal) {
            sb.append(((Literal) symbol.initializer.children[0]).value);
        } else if (symbol.getType() != null) {
            sb.append('(').append(symbol.getType()).append(')');
        } else {
            sb.append("?");
        }
        titleView.setSubtitles(Collections.singletonList(sb.toString()));

        ExpandableList codeView = getContentView();

        codeView.removeAllViews();
        if (expanded) {
            if (symbol.initializer instanceof LetStatement && isMultiDim(symbol.type)) {
                addLine(codeView, 1, "LET " + name + " = {");
                addChildList(codeView, 2, symbol.initializer.children[0]);
                addLine(codeView, 1, "}");
            } else {
                addLine(codeView, 1, symbol.initializer);
            }

        }
    }
}


