package org.kobjects.asde.android.ide.symbollist;

import android.graphics.Typeface;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.node.ArrayLiteral;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.typesystem.Type;

import java.util.Collections;


public class VariableView extends SymbolView {
    Object cache = this;
    MainActivity mainActivity;

    public VariableView(MainActivity mainActivity, GlobalSymbol symbol) {
        super(mainActivity, symbol);
        this.mainActivity = mainActivity;
        this.symbol = symbol;
        titleView.setTypeIndicator(symbol.isConstant() ? 'C' : 'V', mainActivity.colors.yellow);
        titleView.setMoreClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                mainActivity.controlView.codeEditText.setText(String.valueOf(symbol.getInitializer()));
                return true;
            });
            popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
                new RenameFlow(mainActivity, symbol.getName()).start();
                return true;
            });
            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                new DeleteFlow(mainActivity, symbol.getName()).start();
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

    public void refresh() {
        super.refresh();
        if (symbol.getValue() == cache || symbol.getInitializer() == cache) {
            return;
        }
        StringBuilder sb = new StringBuilder(" ");
        if (symbol.getValue() != null) {
            sb.append(symbol.getValue());
        } else if (symbol.getInitializer() instanceof LetStatement && symbol.getInitializer().children[0] instanceof Literal) {
            sb.append(((Literal) symbol.getInitializer().children[0]).value);
        } else if (symbol.getType() != null) {
            sb.append('(').append(symbol.getType()).append(')');
        } else {
            sb.append("?");
        }
        cache = symbol.getValue() != null ? symbol.getValue() : symbol.getInitializer();
        titleView.setSubtitles(Collections.singletonList(sb.toString()));
    }

    @Override
    public void syncContent() {
        refresh();

        ExpandableList codeView = getContentView();

        codeView.removeAllViews();
        if (expanded) {
            if (symbol.getInitializer() instanceof LetStatement && isMultiDim(symbol.getType())) {
                addLine(codeView, 1, "LET " + symbol.getName() + " = {");
                addChildList(codeView, 2, symbol.getInitializer().children[0]);
                addLine(codeView, 1, "}");
            } else {
                addLine(codeView, 1, symbol.getInitializer());
            }

        }
    }
}


