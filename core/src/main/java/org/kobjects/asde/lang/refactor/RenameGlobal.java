package org.kobjects.asde.lang.refactor;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.asde.lang.symbol.LocalSymbol;

public class RenameGlobal extends Visitor {

    private final String oldName;
    private final String newName;


    public RenameGlobal(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public void visitIdentifier(Identifier identifier) {
        if (identifier.getName().equals(oldName) && !(identifier.getResolved() instanceof LocalSymbol)) {
            identifier.setName(newName);
        }
    }

    @Override
    public void visitProgram(Program program) {
        super.visitProgram(program);
        program.notifyProgramChanged();
    }
}
