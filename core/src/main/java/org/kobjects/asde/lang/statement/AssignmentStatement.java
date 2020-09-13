package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.node.Assignable;
import org.kobjects.asde.lang.node.AssignableNode;
import org.kobjects.asde.lang.node.AssignableWasmNode;
import org.kobjects.asde.lang.node.TraitCast;
import org.kobjects.asde.lang.type.AwaitableType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.async.Promise;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.function.ValidationContext;

import java.util.Map;


public class AssignmentStatement extends Statement {

  public enum Kind {
    ASSIGN, MUT, LET
  }

  public static AssignmentStatement createDeclaration(Kind kind, String varName, boolean await, Node init) {
    return new AssignmentStatement(kind, varName, null, await, init);
  }

  public static AssignmentStatement createAssignment(Node target, boolean await, Node init) {
    if (!(target instanceof AssignableNode) && !(target instanceof AssignableWasmNode)) {
      throw new RuntimeException("Assignment target is not assignable.");
    }
    return new AssignmentStatement(Kind.ASSIGN, null, target, await, init);
  }

  public final boolean await;
  public final Kind kind;
  String varName;
  Assignable resolvedTarget;
  Node resolvedSource;

  private AssignmentStatement(Kind kind, String varName, Node target, boolean await, Node init) {
    super(init, target);
    this.kind = kind;
    this.varName = varName;
    this.await = await;
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    Object value = resolvedSource.eval(evaluationContext);
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
  public boolean resolve(ValidationContext resolutionContext, int line) {
    block = resolutionContext.getCurrentBlock();
    if (!children[0].resolve(resolutionContext, line)) {
      return false;
    }
    if (kind == Kind.ASSIGN) {
      try {
        // May fail if resolve above has failed.
        if (children[1] instanceof AssignableNode) {
          Type expectedType = ((AssignableNode) children[1]).resolveForAssignment(resolutionContext, line);
          resolvedSource = TraitCast.autoCast(children[0], expectedType, resolutionContext);
          resolvedTarget = (AssignableNode) children[1];
        } else if (children[1] instanceof AssignableWasmNode) {
          WasmExpressionBuilder builder = new WasmExpressionBuilder();
          Type expectedType = ((AssignableWasmNode) children[1]).resolveForAssignment(builder, resolutionContext, line);
          resolvedSource = TraitCast.autoCast(children[0], expectedType, resolutionContext);
          WasmExpression wasm = builder.build();
          resolvedTarget = new Assignable() {
            @Override
            public void set(EvaluationContext evaluationContext, Object value) {
              evaluationContext.dataStack.pushObject(value);
              wasm.run(evaluationContext, -1);
            }
          };
        }
      } catch (Exception e) {
        resolutionContext.addError(this, e);
      }

    } else {
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
      resolvedTarget = resolutionContext.declareLocalVariable(varName, type, kind != Kind.LET);
      resolvedSource = children[0];
    }
    return true;
  }


  public void onResolve(ValidationContext resolutionContext, int line) {
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
