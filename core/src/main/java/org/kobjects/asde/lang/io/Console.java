package org.kobjects.asde.lang.io;

import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.typesystem.Type;

import java.io.InputStream;
import java.io.OutputStream;

public interface Console {
    enum ClearScreenType {
        CLS_STATEMENT, CLEAR_STATEMENT, PROGRAM_CLOSED
    }

    void print(CharSequence s);
    String input(Type typeHint);

    void clearScreen(ClearScreenType clearScreenType);

    void highlight(FunctionImplementation function, int lineNumber);

    InputStream openInputStream(String url);

    OutputStream openOutputStream(String url);

    ProgramReference nameToReference(String name);

    void startProgress(String title);
    void updateProgress(String update);
    void endProgress();

    void delete(int line);
    void edit(int i);

    void showError(String message, Exception e);
}
