package org.kobjects.asde.android.ide.symbollist;

import android.widget.LinearLayout;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.SymbolTitleView;

import java.util.ArrayList;
import java.util.List;

public abstract class SymbolView extends LinearLayout {
    final MainActivity mainActivity;
    final String name;

    SymbolTitleView titleView;
    List<ExpandListener> expandListeners = new ArrayList<>();
    boolean expanded;

    private ExpandableList contentView;

    SymbolView(MainActivity mainActivity, String name) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.name = name;
        setOrientation(VERTICAL);

        titleView = new SymbolTitleView(mainActivity, name);
        addView(titleView);
        titleView.setOnClickListener(clicked -> {
            setExpanded(!expanded, true);
        });
    }

    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
    }

    public abstract void syncContent();

    public void setExpanded(final boolean expand, boolean animated) {
        if (expanded == expand) {
            return;
        }
        if (animated) {
            getContentView().animateNextChanges();
        }
        expanded = expand;
        for (ExpandListener expandListener : expandListeners) {
            expandListener.notifyExpanding(this, animated);
        }
        syncContent();
    }

    public ExpandableList getContentView() {
        if (mainActivity.codeView != null) {
            return mainActivity.codeView;
        }

        if (contentView == null) {
            contentView = new ExpandableList(mainActivity);
            addView(contentView);
        }

        return contentView;
    }


}