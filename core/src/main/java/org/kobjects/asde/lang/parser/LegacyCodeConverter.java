package org.kobjects.asde.lang.parser;

import org.kobjects.asde.lang.array.Array;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.statement.DefStatement;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.GotoStatement;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class LegacyCodeConverter {
  private final Program program;

  private HashMap<String, Type> symbols = new HashMap<>();
  private HashMap<String, String> renameMap = new HashMap<>();

  public LegacyCodeConverter(Program program) {
    this.program = program;
  }


  private Statement processStatement(Statement statement, boolean beforeMinGoto) {
    if (statement instanceof DimStatement) {
      return processDimStatement((DimStatement) statement, beforeMinGoto);
    }
    if (statement instanceof DefStatement) {
      return processDefStatement((DefStatement) statement);
    }

    return (Statement) processNode(statement);
  }


  private Node processNode(Node node) {
    if (node instanceof Apply) {
      return processApply((Apply) node);
    }
    if (node instanceof Identifier) {
      return processIdentifier((Identifier) node);
    }
    processChildren(node, 0);
    return node;
  }

  private DefStatement processDefStatement(DefStatement defStatement) {
    processChildren(defStatement, 0);
    program.setPersistentInitializer(defStatement.getVarName(), defStatement);
    return null;
  }

  private Identifier processIdentifier(Identifier identifier) {
    processIdentifier(identifier, identifier.getName().endsWith("$") ? Types.STRING : Types.NUMBER);
    return identifier;
  }

  private String resolveNameForType(String originalName, Type impliedType) {
    String normalized = originalName.toLowerCase();
    Type type = symbols.get(normalized);
    if (type == null) {
      symbols.put(normalized, impliedType);
    } else if (!type.equals(impliedType)) {
      String renameTo = renameMap.get(normalized);
      if (renameTo == null) {
        int i = 2;
        while (symbols.containsKey(normalized + "_" + i)) {
          i++;
        }
        renameTo = normalized + "_" + i;
        renameMap.put(normalized, renameTo);
        symbols.put(renameTo, impliedType);
      }
      return renameTo;
    }
    return originalName;
  }

  private void processIdentifier(Identifier identifier, Type impliedType) {
    identifier.setName(resolveNameForType(identifier.getName(), impliedType));
  }

  private DimStatement processDimStatement(DimStatement dimStatement, boolean beforeMinGoto) {
    ArrayType type = new ArrayType(dimStatement.elementType, dimStatement.children.length);
    dimStatement.setVarName(resolveNameForType(dimStatement.getVarName(), type));
    processChildren(dimStatement, 0);

    String name = dimStatement.getVarName();

    if (beforeMinGoto) {
      boolean allLiterals = true;
      for (Node child : dimStatement.children) {
        if (!(child instanceof Literal)) {
          allLiterals = false;
          break;
        }
      }
      if (allLiterals) {
        program.setPersistentInitializer(name, dimStatement);
        return null;
      }
    }

    Node[] init = new Node[type.getDimension()];
    for (int i = 0; i < init.length; i++) {
      init[i] = new Literal(11.0);
    }
    program.setPersistentInitializer(name, new DimStatement(dimStatement.elementType, name, init));

    return dimStatement;
  }


  private void processChildren(Node node, int startIndex) {
    for (int i = startIndex; i < node.children.length; i++) {
      node.children[i] = processNode(node.children[i]);
    }
  }

  private Node processApply(Apply apply) {
    if (!(apply.children[0] instanceof Identifier)) {
      for (int i = 0; i < apply.children.length; i++) {
        apply.children[i] = processNode(apply.children[i]);
      }
      return apply;
    }
    Identifier identifier = (Identifier) apply.children[0];
    if (program.getSymbol(identifier.getName()) == null && !identifier.getName().toLowerCase().startsWith("fn")) {
      processIdentifier(identifier, new ArrayType(identifier.getName().endsWith("$") ? Types.STRING : Types.NUMBER, apply.children.length - 1));
    }
    return apply;
  }

  int findMinGotoLine() {
    int minGoto = Integer.MAX_VALUE;
    for (Node statement : program.main.allStatements()) {
      if (statement instanceof GotoStatement) {
        minGoto = Math.min(((GotoStatement) statement).target, minGoto);
      }
    }
    return minGoto;
  }


  public void run() {
    int minGoto = findMinGotoLine();

    System.out.println("*************** MinGoto: " + minGoto);

    for (CodeLine codeLine : program.main.allLines()) {
      System.out.println("Processing line: " + codeLine);
      int lineNumber = codeLine.getNumber();
      ArrayList<Node> newStatements = new ArrayList<>();
      for (Node node : codeLine) {
        Node replacement = processStatement((Statement) node, lineNumber < minGoto);
        if (replacement != null) {
          newStatements.add(replacement);
        }
      }
      if (newStatements.size() > 0) {
        program.main.setLine(new CodeLine(lineNumber, newStatements.toArray(new Node[0])));
      } else {
        program.main.deleteLine(lineNumber);
      }
    }

    System.out.println("Symbols: " + symbols);

    for (Map.Entry<String, Type> entry : symbols.entrySet()) {
      String name = entry.getKey();
      Type type = entry.getValue();
      if (type == Types.NUMBER) {
        program.setPersistentInitializer(name, new DeclarationStatement(DeclarationStatement.Kind.LET, name, new Literal(0.0)));
      } else if (type == Types.STRING) {
        program.setPersistentInitializer(name, new DeclarationStatement(DeclarationStatement.Kind.LET, name, new Literal("")));
      }
    }
  }
}
