package org.kobjects.async;

import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class Promise<T> implements Executable<T> {

  private final Executable<T> executable;

  public static <T> Promise<T> of(T value) {
    return new Promise<>((executor, consumer) -> consumer.accept(value));
  }

  public Promise(Executable<T> executable) {
    this.executable = executable;
  }

  public <R> Promise<R> then(Function<? super T, ? extends R> transformation) {
    return new Promise<R>(
        (executor, consumer) -> execute(executor, result -> executor.execute(() -> consumer.accept(transformation.apply(result)))));
  }

  public void execute(Executor executor, Consumer<? super T> consumer) {
    executable.execute(executor, result -> {
      if (result instanceof Promise) {
        ((Promise) result).execute(executor, consumer);
      } else {
        consumer.accept(result);
      }
    });
  }
}
