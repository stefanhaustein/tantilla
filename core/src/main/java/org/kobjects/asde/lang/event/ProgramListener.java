package org.kobjects.asde.lang.event;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.io.ProgramReference;

public interface ProgramListener {
    enum Event {
        RENAMED, CHANGED, LOADED, MODE_CHANGED
    }

    void programEvent(Event event);
}
