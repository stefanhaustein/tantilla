package org.kobjects.asde.lang.io;

import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.UserFunction;

import java.io.InputStream;
import java.io.OutputStream;

public interface Console {
  void prompt();

  enum ClearScreenType {
        CLS_STATEMENT, CLEAR_STATEMENT, PROGRAM_CLOSED
    }

    void print(CharSequence s);
    String input();

    void clearScreen(ClearScreenType clearScreenType);

    void highlight(UserFunction function, int lineNumber);

    InputStream openInputStream(String url);

    OutputStream openOutputStream(String url);

    /**
     * Use null for the "scratch" / default reference.
     */
    ProgramReference nameToReference(String name);

    void startProgress(String title);
    void updateProgress(String update);
    void endProgress();

    void delete(int line);
    void edit(int line);
    void selectProperty(Property property);

    Property getSelectedProperty();

    void showError(String message, Exception e);
}
