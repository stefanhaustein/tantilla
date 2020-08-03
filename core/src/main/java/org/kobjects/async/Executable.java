package org.kobjects.async;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

public interface Executable<W> {
  void execute(Executor executor, Consumer<? super W> consumer);
}
