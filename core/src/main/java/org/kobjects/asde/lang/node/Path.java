package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.InstanceType;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyChangeListener;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;


public class Path extends SymbolNode {
  public String pathName;
  private PropertyDescriptor resolvedPropertyDescriptor;
  private Object resolvedConstant;

  public Path(Node left, Node right) {
    super(left);
    if (!(right instanceof Identifier)) {
      throw new RuntimeException("Path name expected");
    }
    pathName = ((Identifier) right).name;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, int line) {
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
    return resolvedConstant != null ? resolvedConstant : resolvedPropertyDescriptor.get(evaluationContext, children[0].eval(evaluationContext));
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
  public void resolveForAssignment(FunctionValidationContext resolutionContext, Type type, int line) {
    resolve(resolutionContext, line);

    // TODO: Check support...
  }

  @Override
  public void addPropertyChangeListener(EvaluationContext evaluationContext, PropertyChangeListener listener) {
    resolvedPropertyDescriptor.addListener(children[0].eval(evaluationContext), listener);
  }

  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    Object target = children[0].eval(evaluationContext);
    resolvedPropertyDescriptor.set(evaluationContext, target, value);
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
