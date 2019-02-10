package org.kobjects.asde.lang.event;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.GlobalSymbol;

public interface ProgramChangeListener {
    void programChanged(Program program);
    void symbolChangedByUser(Program program, GlobalSymbol symbol);
}
