package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.async.Promise;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.LocalSymbol;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;

import java.util.Map;


public class DeclarationStatement extends Statement {

  public enum Kind {
    MUT, LET
  }

  public final boolean await;
  public final Kind kind;
  String varName;
  LocalSymbol resolved;

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object value = children[0].eval(evaluationContext);
    if (await) {
      final EvaluationContext innerContext = new EvaluationContext(evaluationContext);
      innerContext.currentLine++;
      evaluationContext.returnValue = ((Promise<?>) value).then(resolved -> {
        evaluationContext.function.callImpl(innerContext);
       return innerContext.returnValue;
     });
     evaluationContext.currentLine = Integer.MAX_VALUE;
    } else {
      resolved.set(evaluationContext, value);
    }
    return null;
  }

  public String getVarName() {
    return varName;
  }

  public DeclarationStatement(Kind kind, String varName, boolean await, Node init) {
    super(init);
    this.varName = varName;
    this.kind = kind;
    this.await = await;
  }

  public void onResolve(ValidationContext resolutionContext, int line) {
    Type type = children[0].returnType();
    if (await) {
      if (type instanceof AwaitableType) {
        throw new RuntimeException("awaitable type expected.");
      }
      if (!(resolutionContext.userFunction.getType().getReturnType() instanceof AwaitableType)) {
        throw new RuntimeException("Function " + resolutionContext.userFunction + " must be async for await.");
      }
      type = ((AwaitableType) type).getWrapped();
    }
    resolved = resolutionContext.declareLocalVariable(varName, type, kind != Kind.LET);
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    asb.append(kind.name().toLowerCase(), SyntaxColor.KEYWORD);
    appendLinked(asb, " " + varName + " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }
}
