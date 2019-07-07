package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;

import java.util.HashMap;
import java.util.Map;

public class ClassValidationContext {
  final Map<Node, Exception> errors = new HashMap<>();
  final ProgramValidationContext programValidationContext;
  final ClassImplementation classImplementation;

  ClassValidationContext(ProgramValidationContext programValidationContext, ClassImplementation classImplementation) {
    this.programValidationContext = programValidationContext;
    this.classImplementation = classImplementation;
  }

}
