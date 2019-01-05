package org.kobjects.asde.lang.refactor;

import org.kobjects.asde.lang.node.Apply;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.node.Visitor;

public class ChangeSignature extends Visitor {
    private final String name;
    private int[] newOrder;

    public ChangeSignature(String name, int[] newOrder) {
        this.name = name;
        this.newOrder = newOrder;
    }

    @Override
    public void visitApply(Apply apply) {
        super.visitApply(apply);
        Node base = apply.children[0];
        if (!(base instanceof Identifier) || !((Identifier) base).getName().equals(name)) {
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
}
