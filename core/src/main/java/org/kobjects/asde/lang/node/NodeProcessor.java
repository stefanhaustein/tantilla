package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.AbstractUserClassProperty;
import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.UserClassProperty;
import org.kobjects.asde.lang.function.FunctionImplementation;
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

  public void processCallableUnit(FunctionImplementation functionImplementation) {
    for (Statement statement : functionImplementation.allLines()) {
      processNode(statement);
    }
  }

  public void processClass(UserClass classImplementation) {
    for (AbstractUserClassProperty symbol : classImplementation.propertyMap.values()) {
      processSymbol(symbol);
    }
  }

  public void processSymbol(StaticSymbol symbol) {
    if (symbol.getValue() instanceof UserClass) {
      processClass((UserClass) symbol.getValue());
    }
    if (symbol.getValue() instanceof FunctionImplementation) {
      processCallableUnit((FunctionImplementation) symbol.getValue());
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
