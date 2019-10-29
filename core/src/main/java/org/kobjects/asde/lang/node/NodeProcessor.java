package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.CodeLine;

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
    for (CodeLine codeLine : functionImplementation.allLines()) {
      processCodeLine(codeLine);
    }
  }

  public void processCodeLine(CodeLine codeLine) {
    for (Node node : codeLine) {
      processNode(node);
    }
  }

  public void processClass(ClassImplementation classImplementation) {
    for (ClassImplementation.ClassPropertyDescriptor symbol : classImplementation.propertyMap.values()) {
      processSymbol(symbol);
    }
  }

  public void processSymbol(StaticSymbol symbol) {
    if (symbol.getValue() instanceof ClassImplementation) {
      processClass((ClassImplementation) symbol.getValue());
    }
    if (symbol.getValue() instanceof FunctionImplementation) {
      processCallableUnit((FunctionImplementation) symbol.getValue());
    }
    if (symbol.getInitializer() != null) {
      processNode(symbol.getInitializer());
    }
  }

  public void processProgram(Program program) {
    for (GlobalSymbol symbol : program.getSymbols()) {
      processSymbol(symbol);
    }

    processCallableUnit(program.main);
  }

}
