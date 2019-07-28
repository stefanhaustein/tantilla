package org.kobjects.asde.lang.refactor;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.SymbolNode;
import org.kobjects.asde.lang.node.Visitor;

public class ChangeSignature extends Visitor {
    private final StaticSymbol symbol;
    private int[] newOrder;

    public ChangeSignature(StaticSymbol symbol, int[] newOrder) {
        this.symbol = symbol;
        this.newOrder = newOrder;
    }

    @Override
    public void visitApply(Apply apply) {
        super.visitApply(apply);
        Node base = apply.children[0];
        if (!(base instanceof SymbolNode) || !((SymbolNode) base).matches(symbol, symbol.getName())) {
            return;
        }
        Node[] oldChildren = apply.children;
        apply.children = new Node[newOrder.length + 1];
        apply.children[0] = base;
        for (int i = 0; i < newOrder.length; i++) {
            if (newOrder[i] != -1) {
                apply.children[i + 1] = oldChildren[newOrder[i] + 1];
            }
        }
    }

    @Override
    public void visitProgram(Program program) {
        super.visitProgram(program);
        program.notifyProgramChanged();
    }
}
