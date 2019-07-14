package org.kobjects.asde.lang;

import org.kobjects.asde.lang.node.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class ClassValidationContext {
  final Map<Node, Exception> errors = new HashMap<>();
  final ProgramValidationContext programValidationContext;
  final ClassImplementation classImplementation;
  final LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
  final HashSet<ClassImplementation.ClassPropertyDescriptor> validated = new HashSet<>();

  ClassValidationContext(ProgramValidationContext programValidationContext, ClassImplementation classImplementation) {
    this.programValidationContext = programValidationContext;
    this.classImplementation = classImplementation;
  }

  public ResolvedSymbol resolve(String name) {
    ClassImplementation.ClassPropertyDescriptor descriptor = classImplementation.getPropertyDescriptor(name);
    if (descriptor == null) {
      return null;
    }
    if (dependencyChain.contains(name) && descriptor.initializer != null) {
      throw new RuntimeException("Circular member dependency: " + dependencyChain + " -> " + name);
    }
    if (!validated.contains(descriptor)) {
      dependencyChain.add(name);
      descriptor.validate(this);
      dependencyChain.remove(name);
    }
    return descriptor;
  }
}
