package org.kobjects.asde.android.ide.field;

import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.property.DeleteFlow;
import org.kobjects.asde.android.ide.property.RenameFlow;
import org.kobjects.asde.android.ide.property.PropertyView;
import org.kobjects.asde.lang.classifier.Module;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.node.ArrayLiteral;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;


public class FieldView extends PropertyView {
    Object cache = this;
    MainActivity mainActivity;

    public FieldView(MainActivity mainActivity, Property symbol) {
        super(mainActivity, symbol);
        this.mainActivity = mainActivity;
        this.field = symbol;
        boolean isTopLevel = symbol.getOwner() instanceof Module;
       titleView.setTypeIndicator((!symbol.isInstanceField() && !isTopLevel ? "static\n" : "") + (symbol.isMutable() ? "mut" : "const"), Colors.DARK_ORANGE, !isTopLevel);
 //       titleView.setTypeIndicator(symbol.isConstant() ? R.drawable.alpha_c : R.drawable.variable, Colors.DARK_ORANGE, symbol instanceof Property);
        titleView.setMoreClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                FieldFlow.editProperties(mainActivity, symbol);
                return true;
            });
            popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
                RenameFlow.start(mainActivity, symbol);
                return true;
            });
            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                DeleteFlow.start(mainActivity, symbol);
                return true;
            });

            popupMenu.show();
        });
        syncContent();
    }

    void addLine(ViewGroup target, int indent, Object value) {
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

    void addChildList(ViewGroup target, int indent, Node node) {
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
        return (type instanceof ListType && ((ListType) type).elementType instanceof ListType);
    }

    public void refresh() {
        super.refresh();
        if (cache != null && (field.getStaticValue() == cache || field.getType() == cache)) {
            return;
        }
        StringBuilder sb = new StringBuilder(" ");
        if (field.getStaticValue() != null) {
            cache = field.getStaticValue();
            sb.append(cache);
        } else if (field.getType() != null) {
            cache = field.getType();
            sb.append('(').append(field.getType()).append(')');
        } else {
            cache = null;
            sb.append("?");
        }
        titleView.setSubtitles(Collections.singletonList(sb.toString()));
    }

    @Override
    public void syncContent() {
        refresh();

        LinearLayout codeView = getContentView();

        if (!expanded) {
            if (codeView == contentView) {
                codeView.removeAllViews();
            }
            return;
        }

        codeView.removeAllViews();

        if (field.getInitializer() != null) {
                addLine(codeView, 1, field.getInitializer());
       }
    }
}


