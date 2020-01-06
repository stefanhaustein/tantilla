package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.program.GlobalSymbol;
import org.kobjects.asde.lang.program.ProgramValidationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.node.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class ClassValidationContext {
  public final Map<Node, Exception> errors = new HashMap<>();
  public final ProgramValidationContext programValidationContext;
  public final ClassImplementation classImplementation;
  final LinkedHashSet<String> dependencyChain = new LinkedHashSet<>();
  public HashSet<GlobalSymbol> dependencies = new HashSet<>();
  final HashSet<ClassPropertyDescriptor> validated = new HashSet<>();

  public ClassValidationContext(ProgramValidationContext programValidationContext, ClassImplementation classImplementation) {
    this.programValidationContext = programValidationContext;
    this.classImplementation = classImplementation;
  }

  public ResolvedSymbol resolve(String name) {
    ClassPropertyDescriptor descriptor = classImplementation.getPropertyDescriptor(name);
    if (descriptor == null) {
      return null;
    }
    if (descriptor.initializer != null) {
      if (dependencyChain.contains(name)) {
       throw new RuntimeException("Circular member dependency: " + dependencyChain + " -> " + name);
      }
      if (!validated.contains(descriptor)) {
        dependencyChain.add(name);
        descriptor.validate(this);
        dependencyChain.remove(name);
      }
    }
    return descriptor;
  }
}
