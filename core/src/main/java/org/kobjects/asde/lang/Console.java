package org.kobjects.asde.lang;

import java.io.File;

public interface Console {
    void print(String s);
    String read();

    File getProgramStoragePath();

    void programNameChangedTo(String name);
}
