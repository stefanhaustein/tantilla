package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.type.CallableUnit;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.GlobalSymbol;

import java.util.Map;


public abstract class Visitor {

    public void visitNode(Node node) {
        for (Node child : node.children) {
            child.accept(this);
        }
    }

    public void visitIdentifier(Identifier identifier) {
        visitNode(identifier);
    }

    public void visitCallableUnit(CallableUnit callableUnit) {
        for (Map.Entry<Integer,CodeLine> entry : callableUnit.entrySet()) {
            visitCodeLine(entry.getKey(), entry.getValue());
        }

    }

    public void visitCodeLine(int lineNumber, CodeLine codeLine) {
        for (Node node : codeLine.statements) {
            node.accept(this);
        }
    }

    public void visitSymbol(String name, GlobalSymbol symbol) {
        if (symbol.getValue() instanceof CallableUnit) {
            visitCallableUnit((CallableUnit) symbol.getValue());
        }
    }

    public void visitProgram(Program program) {
        for (Map.Entry<String, GlobalSymbol> entry : program.getSymbolMap().entrySet()) {
            visitSymbol(entry.getKey(), entry.getValue());
        }

        visitCallableUnit(program.main);
    }

    public void visitApply(Apply apply) {
        visitNode(apply);
    }
}
