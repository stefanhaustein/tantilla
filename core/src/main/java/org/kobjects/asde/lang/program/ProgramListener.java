package org.kobjects.asde.lang.program;

public interface ProgramListener {
    enum Event {
        RENAMED, CHANGED, LOADED, MODE_CHANGED
    }

    void programEvent(Event event);
}
