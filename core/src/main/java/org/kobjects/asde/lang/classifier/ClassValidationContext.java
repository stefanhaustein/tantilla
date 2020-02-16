package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.ProgramValidationContext;
import org.kobjects.asde.lang.node.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class ClassValidationContext {
  public final Map<Node, Exception> errors = new HashMap<>();
  public final ProgramValidationContext programValidationContext;
  public final UserClass classImplementation;
  public HashSet<GlobalSymbol> dependencies = new HashSet<>();
  final HashSet<UserClassProperty> validated = new HashSet<>();

  public ClassValidationContext(ProgramValidationContext programValidationContext, UserClass classImplementation) {
    this.programValidationContext = programValidationContext;
    this.classImplementation = classImplementation;
  }

}
