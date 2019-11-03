package org.kobjects.asde.lang.statement;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.asde.lang.node.Node;

import java.util.Map;

public class DefStatement extends AbstractDeclarationStatement {

  public final FunctionImplementation implementation;

  public DefStatement(String name, FunctionImplementation implementation) {
    super(name);
    this.implementation = implementation;
  }

  @Override
  protected void onResolve(FunctionValidationContext resolutionContext, Node parent, int line, int index) {
    FunctionValidationContext innerContext = new FunctionValidationContext(resolutionContext.programValidationContext, resolutionContext.mode, implementation);
    implementation.validate(innerContext);
    resolved = resolutionContext.resolveVariableDeclaration(varName, implementation.getType(), true);
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolved.set(evaluationContext, implementation);
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "DEF " + varName + "(", errors);
    if (implementation.parameterNames.length > 0) {
      asb.append(implementation.parameterNames[0]);
      for (int i = 1; i < implementation.parameterNames.length; i++) {
        asb.append(", ");
        asb.append(implementation.parameterNames[i]);
      }
    }
    asb.append(") = ");
    ((FunctionReturnStatement) implementation.findNextLine(10).get(0)).children[0].toString(asb, errors, preferAscii);
  }
}
