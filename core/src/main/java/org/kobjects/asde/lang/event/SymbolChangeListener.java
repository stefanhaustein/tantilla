package org.kobjects.asde.lang.event;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.lang.StaticSymbol;

public interface SymbolChangeListener {
    void symbolChangedByUser(StaticSymbol symbol);
}
