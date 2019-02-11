package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.type.Array;
import org.kobjects.asde.lang.type.Function;
import org.kobjects.asde.lang.EvaluationContext;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.FunctionValidationContext;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Apply extends AssignableNode {

    private final boolean parentesis;

    public Apply(boolean parentesis, Node... children) {
        super(children);
        this.parentesis = parentesis;
    }


    public void accept(Visitor visitor) {
        visitor.visitApply(this);
    }


    public void set(EvaluationContext evaluationContext, Object value) {
        Object base = children[0].evalRaw(evaluationContext);
        if (!(base instanceof Array)) {
            throw new EvaluationException(this, "Can't set indexed value to non-array: " + value);
        }
        Array array = (Array) base;
        int[] indices = new int[children.length - 1];
        for (int i = 1; i < children.length; i++) {
            indices[i - 1] = evalChildToInt(evaluationContext, i);
        }
        array.setValueAt(value, indices);
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    protected void onResolve(FunctionValidationContext resolutionContext, int line, int index) {
        if (children[0].returnType() instanceof FunctionType) {
            FunctionType resolved = (FunctionType) children[0].returnType();
            // TODO: b/c optional params, add minParameterCount
            if (children.length - 1 > resolved.getParameterCount() || children.length - 1 < resolved.getMinParameterCount()) {
                throw new RuntimeException("Expected parameter count is "
                        + resolved.getMinParameterCount() + ".."
                        + resolved.getParameterCount() + " but got " + (children.length - 1) + " for " + this);
            }
            for (int i = 0; i < children.length - 1; i++) {
                if (!Types.match(resolved.getParameterType(i), children[i+1].returnType())) {
                    throw new RuntimeException("Type mismatch for parameter " + i + ": expected: "
                            + resolved.getParameterType(i) + " actual: " + children[i+1].returnType());
                }
            }
        } else if (children[0].returnType() != null) {
            throw new RuntimeException("Can't apply parameters to " + children[0].returnType());
        }
    }

    public Object eval(EvaluationContext evaluationContext) {
        Object base = children[0].evalRaw(evaluationContext);
        if (!(base instanceof Function)) {
            throw new EvaluationException(this, "Can't apply parameters to " + base);
        }
        Function function = (Function) base;
        evaluationContext.ensureExtraStackSpace(function.getLocalVariableCount());
        if (children.length - 1 > function.getLocalVariableCount()) {
            throw new RuntimeException("Too many params for " + function);
        }
        // Push is important here, as parameter evaluation might also run apply().
        for (int i = 1; i < children.length; i++) {
            evaluationContext.push(children[i].eval(evaluationContext));
        }
        evaluationContext.popN(children.length - 1);
        try {
            return function.call(evaluationContext, children.length - 1);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() + " in " + children[0], e);
        }
    }

    public Type returnType() {
        if (children[0].returnType() instanceof FunctionType) {
            return ((FunctionType) children[0].returnType()).getReturnType(children.length - 1);
        }
        if (children[0].returnType() == null) {
            return null;
        }
        throw new RuntimeException("Can't apply parameters to " + children[0].returnType());
    }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
     children[0].toString(asb, errors);
     asb.append(parentesis ? '(' : ' ');
     for (int i = 1; i < children.length; i++) {
          if (i > 1) {
              asb.append(", ");
          }
          children[i].toString(asb, errors);
      }
      if (parentesis) {
          asb.append(')');
      }
  }
}
