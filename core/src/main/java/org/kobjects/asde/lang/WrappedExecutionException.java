package org.kobjects.asde.lang;

public class WrappedExecutionException extends RuntimeException {
    public final CallableUnit callableUnit;
    public final int lineNumber;

    WrappedExecutionException(CallableUnit callableUnit, int lineNumber, Exception cause) {
        super(cause);
        this.callableUnit = callableUnit;
        this.lineNumber = lineNumber;
    }
}
