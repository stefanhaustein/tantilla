package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.typesystem.*;

import java.util.Map;


public class Path extends SymbolNode {
  private String pathName;
  private PropertyDescriptor resolvedPropertyDescriptor;
  private Object resolvedConstant;

  public Path(Node left, Node right) {
    super(left);
    if (!(right instanceof Identifier)) {
      throw new RuntimeException("Path name expected");
    }
    pathName = ((Identifier) right).name;
  }

  public Property evalProperty(EvaluationContext evaluationContext) {
    Object base = children[0].eval(evaluationContext);
    if (!(base instanceof Instance)) {
      throw new RuntimeException("instance expected; was: " + (base == null ? null : base.getClass()) + " expr: " + children[0]);
    }
    Instance instance = (Instance) base;
    PropertyDescriptor propertyDescriptor = resolvedPropertyDescriptor != null ? resolvedPropertyDescriptor : instance.getType().getPropertyDescriptor(pathName);
    if (propertyDescriptor == null) {
      throw new RuntimeException("Property '" + pathName + "' does not exist.");
    }
    return instance.getProperty(propertyDescriptor);
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    if (children[0].returnType() instanceof InstanceType) {
      resolvedPropertyDescriptor = ((InstanceType) children[0].returnType()).getPropertyDescriptor(pathName);
      if (resolvedPropertyDescriptor == null) {
        throw new RuntimeException("Property '" + pathName + "' not found in " + children[0].returnType());
      }
      if (resolvedPropertyDescriptor.type() == null) {
        throw new RuntimeException("Type of property '" + resolvedPropertyDescriptor + "' is null.");
      }
      return;
    }

    if (children[0].returnType() instanceof MetaType) {
      Type type = ((MetaType) children[0].returnType()).getWrapped();
      if (type instanceof EnumType) {
        EnumType enumType = (EnumType) type;
        resolvedConstant = enumType.getLiteral(pathName);
        return;
      }
    }

    throw new RuntimeException("InstanceType or Enum expected as path base; got: " + children[0].returnType());
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    return resolvedConstant != null ? resolvedConstant : evalProperty(evaluationContext).get();
  }

  @Override
  public Type returnType() {
    return resolvedPropertyDescriptor != null ? resolvedPropertyDescriptor.type() : Types.of(resolvedConstant);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, "." + pathName, errors);
  }

  @Override
  public void resolveForAssignment(FunctionValidationContext resolutionContext, Node parent, Type type, int line) {
    resolve(resolutionContext, parent, line);

    // TODO: Check support...
  }

  @Override
  public void addPropertyChangeListener(EvaluationContext evaluationContext, PropertyChangeListener listener) {
    evalProperty(evaluationContext).addListener(listener);
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    evalProperty(evaluationContext).set(value);
  }

  @Override
  public boolean isConstant() {
    return resolvedConstant != null;
  }

  @Override
  public boolean isAssignable() {
    return resolvedPropertyDescriptor != null;
  }


  public PropertyDescriptor getResolvedPropertyDescriptor() {
    return resolvedPropertyDescriptor;
  }

  @Override
  public boolean matches(StaticSymbol symbol, String oldName) {
    return symbol == resolvedPropertyDescriptor;
  }
}
