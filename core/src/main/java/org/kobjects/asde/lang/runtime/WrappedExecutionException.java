package org.kobjects.asde.lang.runtime;

import org.kobjects.asde.lang.function.UserFunction;

public class WrappedExecutionException extends RuntimeException {
    public final UserFunction userFunction;
    public final int lineNumber;

    public WrappedExecutionException(UserFunction userFunction, int lineNumber, Exception cause) {
        super(cause);
        this.userFunction = userFunction;
        this.lineNumber = lineNumber;
    }
}
