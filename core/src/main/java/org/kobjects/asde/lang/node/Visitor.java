package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.FunctionImplementation;
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

    public void visitCallableUnit(FunctionImplementation functionImplementation) {
        for (Map.Entry<Integer,CodeLine> entry : functionImplementation.entrySet()) {
            visitCodeLine(entry.getValue());
        }

    }

    public void visitCodeLine(CodeLine codeLine) {
        for (Node node : codeLine) {
            node.accept(this);
        }
    }

    public void visitSymbol(GlobalSymbol symbol) {
        if (symbol.getValue() instanceof FunctionImplementation) {
            visitCallableUnit((FunctionImplementation) symbol.getValue());
        }
    }

    public void visitProgram(Program program) {
        for (GlobalSymbol symbol : program.getSymbols()) {
            visitSymbol(symbol);
        }

        visitCallableUnit(program.main);
    }

    public void visitApply(Apply apply) {
        visitNode(apply);
    }
}
