package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.expression.Assignable;
import org.kobjects.asde.lang.expression.AssignableWasmNode;
import org.kobjects.asde.lang.expression.ExpressionNode;
import org.kobjects.asde.lang.expression.TraitCast;
import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.async.Promise;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.function.ValidationContext;

import java.util.Map;


public class AssignmentStatement extends Statement {

  public enum Kind {
    ASSIGN, MUT, LET
  }

  public static AssignmentStatement createDeclaration(Kind kind, String varName, boolean await, ExpressionNode init) {
    return new AssignmentStatement(kind, varName, null, await, init);
  }

  public static AssignmentStatement createAssignment(ExpressionNode target, boolean await, ExpressionNode init) {
    if (!(target instanceof AssignableWasmNode)) {
      throw new RuntimeException("Assignment target is not assignable.");
    }
    return new AssignmentStatement(Kind.ASSIGN, null, target, await, init);
  }

  public final boolean await;
  public final Kind kind;
  String varName;
  Assignable resolvedTarget;
  WasmExpression resolvedSource;

  private AssignmentStatement(Kind kind, String varName, ExpressionNode target, boolean await, ExpressionNode init) {
    super(init, target);
    this.kind = kind;
    this.varName = varName;
    this.await = await;
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    resolvedSource.run(evaluationContext);
    Object value = evaluationContext.dataStack.popObject();
    if (await) {
      final EvaluationContext innerContext = new EvaluationContext(evaluationContext);
      innerContext.currentLine++;
      evaluationContext.returnValue = ((Promise<?>) value).then(resolved -> {
        resolvedTarget.set(innerContext, resolved);
        evaluationContext.function.callImpl(innerContext);
       return innerContext.returnValue;
     });
     evaluationContext.currentLine = Integer.MAX_VALUE;
    } else {
      resolvedTarget.set(evaluationContext, value);
    }
    return null;
  }

  public String getVarName() {
    return varName;
  }

  @Override
  public void resolveImpl(ValidationContext resolutionContext, int line) {
    if (kind == Kind.ASSIGN) {
      try {
          WasmExpressionBuilder builder = new WasmExpressionBuilder();
          Type expectedType = ((AssignableWasmNode) children[1]).resolveForAssignment(builder, resolutionContext, line);
          WasmExpression wasm = builder.build();
          resolvedTarget = new Assignable() {
            @Override
            public void set(EvaluationContext evaluationContext, Object value) {
              evaluationContext.dataStack.pushObject(value);
              wasm.run(evaluationContext, -1);
            }
          };

          builder = new WasmExpressionBuilder();
          Type actualType = children[0].resolveWasm(builder, resolutionContext, line);
          TraitCast.autoCastWasm(builder, actualType, expectedType, resolutionContext);
          resolvedSource = builder.build();

      } catch (Exception e) {
        resolutionContext.addError(this, e);
      }

    } else {
      WasmExpressionBuilder builder = new WasmExpressionBuilder();
      Type type = children[0].resolveWasm(builder, resolutionContext, line);;
      resolvedSource = builder.build();
      if (await) {
        if (type instanceof AwaitableType) {
          throw new RuntimeException("awaitable type expected.");
        }
        if (!(resolutionContext.userFunction.getType().getReturnType() instanceof AwaitableType)) {
          throw new RuntimeException("Function " + resolutionContext.userFunction + " must be async for await.");
        }
        type = ((AwaitableType) type).getWrapped();
      }
      resolvedTarget = resolutionContext.declareLocalVariable(varName, type, kind != Kind.LET);
    }
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    if (kind != Kind.ASSIGN) {
      asb.append(kind.name().toLowerCase(), SyntaxColor.KEYWORD);
      asb.append(' ');
      asb.append(varName);
    } else {
      children[1].toString(asb, errors, preferAscii);
    }
    appendLinked(asb, " = ", errors);
    children[0].toString(asb, errors, preferAscii);
  }


}
