package org.kobjects.asde.lang;

public interface StartStopListener {
    void programStarted();

    /**
     * Program was forced to stop. The cause is null if stopped by the user.
     */
    void programAborted(Exception cause);
    void programPaused();

    /**
     * Program ended "naturally", i.e. main control reached end of the program code.
     */
    void programEnded();
}
