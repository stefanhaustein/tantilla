package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.type.Type;

import java.util.Map;


public class Path extends SymbolNode {
  public String pathName;
  private Property resolvedProperty;
  private Object resolvedConstant;

  public Path(Node left, Node right) {
    super(left);
    if (!(right instanceof Identifier)) {
      throw new RuntimeException("Path name expected");
    }
    pathName = ((Identifier) right).name;
  }

  @Override
  protected void onResolve(ValidationContext resolutionContext, int line) {
    if (children[0].returnType() instanceof Classifier) {
      resolvedProperty = ((Classifier) children[0].returnType()).getPropertyDescriptor(pathName);
      if (resolvedProperty == null) {
        throw new RuntimeException("Property '" + pathName + "' not found in " + children[0].returnType());
      }
      resolutionContext.validateAndAddDependency(resolvedProperty);
      if (resolvedProperty.getType() == null) {
        throw new RuntimeException("Type of property '" + resolvedProperty + "' is null.");
      }
      if (!resolvedProperty.isInstanceField()) {
        throw new RuntimeException("No-static Instance property expected. Please use " + children[0].returnType() + "." + resolvedProperty + " for a static reference instead.");
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
    return resolvedConstant != null ? resolvedConstant : resolvedProperty.get(evaluationContext, children[0].eval(evaluationContext));
  }

  @Override
  public Type returnType() {
    return resolvedProperty != null ? resolvedProperty.getType() : Types.of(resolvedConstant);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    children[0].toString(asb, errors, preferAscii);
    appendLinked(asb, "." + pathName, errors);
  }

  @Override
  public void resolveForAssignment(ValidationContext resolutionContext, Type type, int line) {
    resolve(resolutionContext, line);

    // TODO: Check support...
  }


  @Override
  public void set(EvaluationContext evaluationContext, Object value) {
    Object target = children[0].eval(evaluationContext);
    resolvedProperty.set(evaluationContext, target, value);
  }

  @Override
  public boolean isConstant() {
    return resolvedConstant != null;
  }


  public Property getResolvedProperty() {
    return resolvedProperty;
  }

  @Override
  public boolean matches(UserProperty symbol, String oldName) {
    return symbol == resolvedProperty;
  }
}
