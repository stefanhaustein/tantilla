package org.kobjects.asde.lang;

import org.kobjects.asde.lang.symbol.GlobalSymbol;

public interface ProgramChangeListener {
    void programChanged(Program program);
    void symbolChangedByUser(Program program, GlobalSymbol symbol);
    void programRenamed(Program program, ProgramReference newName);
}
