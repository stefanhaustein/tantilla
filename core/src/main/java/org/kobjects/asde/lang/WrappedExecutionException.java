package org.kobjects.asde.lang;

public class WrappedExecutionException extends RuntimeException {
    public final FunctionImplementation functionImplementation;
    public final int lineNumber;

    public WrappedExecutionException(FunctionImplementation functionImplementation, int lineNumber, Exception cause) {
        super(cause);
        this.functionImplementation = functionImplementation;
        this.lineNumber = lineNumber;
    }
}
