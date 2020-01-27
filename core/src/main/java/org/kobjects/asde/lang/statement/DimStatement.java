package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.array.Array;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.array.ArrayType;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.FunctionValidationContext;
import org.kobjects.typesystem.Type;

import java.util.Map;

public class DimStatement extends AbstractDeclarationStatement {
  public final Type elementType;

  public DimStatement(Type elementType, String varName, Node... children) {
    super(varName, children);
    this.elementType = elementType;
    this.varName = varName;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line) {
    for (Node node : children) {
      if (node.returnType() != Types.FLOAT) {
        resolutionContext.addError(node, new RuntimeException("Numerical type required for DIM"));
      }
    }
    resolved = resolutionContext.resolveVariableDeclaration(varName, new ArrayType(elementType, children.length), false);
    if (!elementType.hasDefaultValue()) {
      Node lastChild = children[children.length -1];
      if (!(lastChild instanceof Literal) || !((Literal) lastChild).value.equals(0.0)) {
        throw new RuntimeException("The last dimension must be 0 for element types without a default values such as " + elementType);
      }
    }
  }

  @Override
  public Object evalValue(EvaluationContext evaluationContext) {
    int[] dims = new int[children.length];
    for (int i = 0; i < children.length; i++) {
      dims[i] = children[i].evalInt(evaluationContext);
    }
    return new Array(elementType, dims);
  }

  @Override
  public Type getValueType() {
    return new ArrayType(elementType, children.length);
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "DIM " + varName + "(", errors);
    if (children.length > 0) {
      children[0].toString(asb, errors, preferAscii);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors, preferAscii);
      }
    }
    asb.append(')');
    if (elementType != (varName.endsWith("$") ? Types.STR : Types.FLOAT)) {
      asb.append(" AS ");
      asb.append(elementType.toString());
    }
  }

}
