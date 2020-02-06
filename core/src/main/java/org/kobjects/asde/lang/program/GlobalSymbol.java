package org.kobjects.asde.lang.program;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.ResolvedSymbol;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.classifier.ClassImplementation;
import org.kobjects.asde.lang.classifier.ClassValidationContext;
import org.kobjects.asde.lang.function.FunctionImplementation;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.statement.AbstractDeclarationStatement;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlobalSymbol implements ResolvedSymbol, StaticSymbol {

  public enum Scope {
    BUILTIN,
    PERSISTENT,
    TRANSIENT
  }

  private final Program program;
  private String name;
  public AbstractDeclarationStatement initializer;
  Object value;
  public Scope scope;
  Type type;
  private boolean constant;
  private Map<Node, Exception> errors = Collections.emptyMap();
  Set<GlobalSymbol> dependencies = Collections.emptySet();
  public int stamp;

  GlobalSymbol(Program program, String name, Scope scope, Object value) {
    this.program = program;
    this.name = name;
    this.scope = scope;
    this.value = value;
    this.type = value == null ? null : Types.of(value);
    this.stamp = program.currentStamp;
  }

  @Override
  public Object get(EvaluationContext evaluationContext) {
    return value;
  }

  @Override
  public SymbolOwner getOwner() {
    return program;
  }

  @Override
  public Map<Node, Exception> getErrors() {
    return errors;
  }

  public Node getInitializer() {
    return initializer;
  }

  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type;
  }

  public Scope getScope() {
    return scope;
  }

  public Object getValue() {
    return value;
  }

  void init(EvaluationContext evaluationContext, HashSet<GlobalSymbol> initialized) {
    if (initialized.contains(this)) {
      return;
    }
    if (dependencies != null) {
      for (GlobalSymbol dep : dependencies) {
        dep.init(evaluationContext, initialized);
      }
    }
    if (initializer != null) {
      initializer.eval(evaluationContext);
    }
    initialized.add(this);
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    this.value = value;
  }

  void setConstant(boolean constant) {
    this.constant = constant;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public String toString(boolean showValue) {
    if (initializer == null) {
      return name+ " = " + value;
    }
    return initializer.toString() + (showValue && value != null ? (" ' " + value) : "");
  }

  public void validate() {
    ProgramValidationContext context = new ProgramValidationContext(program);
    context.startChain(name);
    validate(context);
  }

  public void validate(ProgramValidationContext programValidationContext) {
    if (programValidationContext.validated.contains(this)) {
      return;
    }
    if (initializer != null) {
      FunctionValidationContext context = new FunctionValidationContext(programValidationContext, FunctionValidationContext.ResolutionMode.INTERACTIVE, null);
      try {
        initializer.resolve(context, 0);
      } catch (Exception e) {
        e.printStackTrace();
        context.addError(initializer, e);
      }
      this.errors = context.errors;
      this.dependencies = context.dependencies;
      type = initializer.getValueType();
    } else if (value instanceof FunctionImplementation) {
      // Avoid an infinite validation loop in recursion
      programValidationContext.validated.add(this);

      FunctionImplementation function = (FunctionImplementation) value;

      if (this == program.mainSymbol) {
        program.currentStamp++;
      }

      FunctionValidationContext context = new FunctionValidationContext(programValidationContext, FunctionValidationContext.ResolutionMode.PROGRAM, function);
      function.validate(context);
      this.errors = context.errors;
      this.dependencies = context.dependencies;
    } else if (value instanceof ClassImplementation) {
      //  programValidationContext.validated.add(this);

      ClassImplementation classImplementation = (ClassImplementation) value;
      ClassValidationContext classValidationContext = new ClassValidationContext(programValidationContext, classImplementation);
      classImplementation.validate(classValidationContext);
      this.errors = classValidationContext.errors;
      this.dependencies = classValidationContext.dependencies;
    }
    programValidationContext.validated.add(this);
  }



  public boolean isConstant() {
    return constant;
  }

  @Override
  public String toString() {
    return name + " = " + value;
  }
}
