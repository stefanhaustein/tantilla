package org.kobjects.asde.lang;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface Console {
    void print(String s);
    String read();

    void clearOutput();
    void clearCanvas();

    void trace(CallableUnit function, int lineNumber);

    InputStream openInputStream(String url);

    OutputStream openOutputStream(String url);

    void programReferenceChanged(ProgramReference fileReference);

    ProgramReference nameToReference(String name);

    void startProgress(String title);
    void updateProgress(String update);
    void endProgress();
}
