package org.kobjects.asde.lang.refactor;

import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.node.Path;
import org.kobjects.asde.lang.node.Visitor;

public class RenameProperty extends Visitor {

  private final ClassImplementation.ClassPropertyDescriptor propertyDescriptor;

  public RenameProperty(ClassImplementation.ClassPropertyDescriptor propertyDescriptor) {
    this.propertyDescriptor = propertyDescriptor;
  }

  @Override
  public void visitIdentifier(Identifier identifier) {
    if (identifier.getResolved() == propertyDescriptor) {
      identifier.setName(propertyDescriptor.getName());
    }
  }

  @Override
  public void visitPath(Path path) {
    if (path.getResolvedPropertyDescriptor() == propertyDescriptor) {
      path.setName(propertyDescriptor.getName());
    }
  }


  @Override
  public void visitProgram(Program program) {
    super.visitProgram(program);
    program.notifyProgramChanged();
  }

}
