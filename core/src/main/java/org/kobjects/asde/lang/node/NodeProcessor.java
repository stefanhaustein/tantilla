package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.symbol.StaticSymbol;


/**
 * Used for refactorings.
 */
public class NodeProcessor {

  Consumer<Node> action;

  public NodeProcessor(Consumer<Node> action) {
    this.action = action;
  }

  public void processNode(Node node) {
    for (Node child : node.children) {
      processNode(child);
    }
    action.accept(node);
  }

  public void processCallableUnit(UserFunction userFunction) {
    for (Statement statement : userFunction.allLines()) {
      processNode(statement);
    }
  }

  public void processClass(UserClass classImplementation) {
    for (UserProperty property : classImplementation.getUserProperties()) {
      processSymbol(property);
    }
  }


  public void processSymbol(StaticSymbol symbol) {
    if (symbol.getStaticValue() instanceof UserClass) {
      processClass((UserClass) symbol.getStaticValue());
    }
    if (symbol.getStaticValue() instanceof UserFunction) {
      processCallableUnit((UserFunction) symbol.getStaticValue());
    }
    if (symbol.getInitializer() != null) {
      processNode(symbol.getInitializer());
    }
  }

  public void processProgram(Program program) {
    synchronized (program) {
      for (GlobalSymbol symbol : program.getSymbols()) {
        processSymbol(symbol);
      }

      processCallableUnit(program.main);
    }
  }

}
