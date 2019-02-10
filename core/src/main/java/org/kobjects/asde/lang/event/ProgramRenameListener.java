package org.kobjects.asde.lang.event;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.io.ProgramReference;

public interface ProgramRenameListener {
    void programRenamed(Program program, ProgramReference newName);
}
