package org.kobjects.asde.lang.symbol;

import org.kobjects.asde.lang.symbol.StaticSymbol;

public interface SymbolChangeListener {
    void symbolChangedByUser(StaticSymbol symbol);
}
