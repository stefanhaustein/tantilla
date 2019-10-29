package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.symbol.SymbolView;

public interface ExpandListener {
    void notifyExpanding(SymbolView expandableView, boolean animated);
}
