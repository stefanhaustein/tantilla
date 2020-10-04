package org.kobjects.asde.lang.io;

import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.expression.Node;

import java.util.HashMap;

public class MultiValidationException extends RuntimeException {
  private final UserFunction code;
  private final HashMap<Node, Exception> errors;

  public MultiValidationException(UserFunction code, HashMap<Node, Exception> errors) {
    super("Validateion Erros in:\n" + code + "\n" + errors.values());
    this.code = code;
    this.errors = errors;
  }

  public UserFunction getCode() {
    return code;
  }

  public HashMap<Node, Exception> getErrors() {
    return errors;
  }
}
