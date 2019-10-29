package org.kobjects.asde.lang.io;

import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.CodeLine;

import java.util.HashMap;

public class MultiValidationException extends RuntimeException {
  private final CodeLine codeLine;
  private final HashMap<Node, Exception> errors;

  public MultiValidationException(CodeLine codeLine, HashMap<Node, Exception> errors) {
    super("Validateion Erros in:\n" + codeLine + "\n" + errors.values());
    this.codeLine = codeLine;
    this.errors = errors;
  }

  public CodeLine getCodeLine() {
    return codeLine;
  }

  public HashMap<Node, Exception> getErrors() {
    return errors;
  }
}
