package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.ResolvedSymbol;
import org.kobjects.asde.lang.node.Literal;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.ArrayType;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.FunctionValidationContext;
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
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    for (Node node : children) {
      if (node.returnType() != Types.NUMBER) {
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
  public Object eval(EvaluationContext evaluationContext) {
    int[] dims = new int[children.length];
    for (int i = 0; i < children.length; i++) {
      dims[i] = children[i].evalInt(evaluationContext) + (evaluationContext.control.program.legacyMode ? 1 : 0);
    }
    resolved.set(evaluationContext, new Array(elementType, dims));
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
    appendLinked(asb, "DIM " + varName + "(", errors);
    if (children.length > 0) {
      children[0].toString(asb, errors);
      for (int i = 1; i < children.length; i++) {
        asb.append(", ");
        children[i].toString(asb, errors);
      }
    }
    asb.append(')');
    if (elementType != (varName.endsWith("$") ? Types.STRING : Types.NUMBER)) {
      asb.append(" AS ");
      asb.append(elementType.toString());
    }
  }

}
