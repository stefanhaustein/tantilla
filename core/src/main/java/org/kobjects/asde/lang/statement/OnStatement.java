package org.kobjects.asde.lang.statement;

import org.kobjects.asde.lang.node.ExpressionNode;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.asde.lang.wasm.runtime.WasmExpression;
import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.program.ProgramControl;
import org.kobjects.asde.lang.node.Node;


import java.util.ArrayList;
import java.util.Map;

public class OnStatement extends BlockStatement  {

  int resolvedEndLine;
  WasmExpression resolvedTrigger;
  ArrayList<WasmExpression> resolvedListenableSubexpressions = new ArrayList<>();
  ArrayList<Type> resolvedTypes = new ArrayList();

  public OnStatement(ExpressionNode condition) {
    super(condition);
  }


  @Override
  protected void resolveImpl(ValidationContext resolutionContext, int line) {
    resolutionContext.startBlock(this);
    resolvedListenableSubexpressions.clear();
    resolvedTypes.clear();
    findListenableSubexpressions(children, resolutionContext, line);
    WasmExpressionBuilder builder = new WasmExpressionBuilder();
    children[0].resolveWasm(builder, resolutionContext, line, Types.BOOL);
    resolvedTrigger = builder.build();
  }

  void findListenableSubexpressions(ExpressionNode[] nodes, ValidationContext resolutionContext, int line) {
    for (ExpressionNode node: nodes) {
      WasmExpressionBuilder builder = new WasmExpressionBuilder();
      Type type = node.resolveWasm(builder, resolutionContext, line);
      if (type.supportsChangeListeners()) {
        resolvedTypes.add(type);
        resolvedListenableSubexpressions.add(builder.build());
      } else {
        findListenableSubexpressions(node.children, resolutionContext, line);
      }
    }
  }

  @Override
  public Object eval(EvaluationContext evaluationContext) {
    EvaluationContext newContectBase = new EvaluationContext(evaluationContext);
    newContectBase.currentLine++;

    Trigger trigger = new Trigger(newContectBase);
    for (int i = 0; i < resolvedTypes.size(); i++) {
      resolvedTypes.get(i).addChangeListener(resolvedListenableSubexpressions.get(i).run(evaluationContext).popObject(), trigger);
    }

    evaluationContext.currentLine = resolvedEndLine + 1;
    return null;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    appendLinked(asb, "on ", errors);
    children[0].toString(asb, errors, preferAscii);
    asb.append(": ");
  }

  @Override
  public void onResolveEnd(ValidationContext resolutionContext, EndStatement endStatement, int endLine) {
    resolvedEndLine = endLine;
  }

  @Override
  void evalEnd(EvaluationContext context) {
    context.currentLine = Integer.MAX_VALUE;
  }

  class Trigger implements  Runnable {

    final EvaluationContext evaluationContext;
    boolean armed = true;

    public Trigger(EvaluationContext evaluationContext) {
      this.evaluationContext = evaluationContext;
    }


    @Override
    public void run() {
      if (evaluationContext.control.getState() == ProgramControl.State.ABORTED ||
          evaluationContext.control.getState() == ProgramControl.State.ENDED) {
        return;
      }
      if (resolvedTrigger.run(evaluationContext).popBoolean()) {
//        System.out.println("Condition did trigger: " + OnStatement.this);
        if (armed) {
          armed = false;
          new Thread(() -> {
            try {
              evaluationContext.function.callImpl(new EvaluationContext(evaluationContext));
            } catch (Exception e) {
              e.printStackTrace();
            }
          }).start();
        }
      } else {
        armed = true;
      }
    }
  }

}
