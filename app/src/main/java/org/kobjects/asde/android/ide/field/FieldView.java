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
import org.kobjects.asde.lang.classifier.Property;

import java.util.Collections;


public class FieldView extends PropertyView {
    Object cache = this;
    MainActivity mainActivity;

    public FieldView(MainActivity mainActivity, Property symbol) {
        super(mainActivity, symbol);
        this.mainActivity = mainActivity;
        this.property = symbol;
        boolean isTopLevel = false;//symbol.getOwner() instanceof Module;
       titleView.setTypeIndicator(symbol.isInstanceField() ? (symbol.isMutable() ? "mut" : "[prop]") : (symbol.isMutable() ? "mut" : "const"),
           symbol.isInstanceField() ? symbol.isMutable() ? Colors.LIGHT_PURPLE : Colors.LIGHT_BLUE_PURPLE : symbol.isMutable() ? Colors.LIGHT_ORANGE : Colors.LIGHT_ORANGE_RED, !isTopLevel);
     //   titleView.setTypeIndicator(symbol.isMutable() ? R.drawable.baseline_lock_open_24 : symbol.isInstanceField() ? R.drawable.outline_lock_24 : R.drawable.baseline_lock_24
      //      , Colors.DARK_PURPLE, !isTopLevel);
        titleView.setMoreClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), view);
            popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(item -> {
                FieldFlow.editProperties(mainActivity, this);
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


    public void refresh() {
        super.refresh();
        if (cache != null && (property.getStaticValue() == cache || property.getType() == cache)) {
            return;
        }
        StringBuilder sb = new StringBuilder(" ");
        if (property.getStaticValue() != null) {
            cache = property.getStaticValue();
            sb.append(cache);
        } else if (property.getType() != null) {
            cache = property.getType();
            sb.append('(').append(property.getType()).append(')');
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

        if (property.hasInitializer()) {
                addLine(codeView, 1, property.getInitializer());
       }
    }
}


