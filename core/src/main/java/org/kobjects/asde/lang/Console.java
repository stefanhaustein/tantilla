package org.kobjects.asde.lang;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface Console {
    void print(CharSequence s);
    String input();

    void clearOutput();
    void clearCanvas();

    void highlight(CallableUnit function, int lineNumber);

    InputStream openInputStream(String url);

    OutputStream openOutputStream(String url);

    void programReferenceChanged(ProgramReference fileReference);

    ProgramReference nameToReference(String name);

    void startProgress(String title);
    void updateProgress(String update);
    void endProgress();

    void delete(int line);
    void edit(int i);

    void showError(String message, Exception e);

    // TODO: Replace with program change notification mechanism
    void sync(boolean incremental);
}
