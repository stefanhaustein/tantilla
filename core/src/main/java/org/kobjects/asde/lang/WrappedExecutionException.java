package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.CallableUnit;

public class WrappedExecutionException extends RuntimeException {
    public final CallableUnit callableUnit;
    public final int lineNumber;

    public WrappedExecutionException(CallableUnit callableUnit, int lineNumber, Exception cause) {
        super(cause);
        this.callableUnit = callableUnit;
        this.lineNumber = lineNumber;
    }
}
