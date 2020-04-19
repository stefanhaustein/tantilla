package org.kobjects.asde.lang.program;

public interface ProgramListener {
    enum Event {
        RENAMED, CHANGED, LOADED
    }

    void programEvent(Event event);
}
