package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Array;
import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Type;

import java.util.Map;

// Not static for access to the variables.
public class Apply extends AssignableNode {
  final Program program;

  public Apply(Program program, Node... children) {
    super(children);
    this.program = program;
  }

  public void set(Interpreter interpreter, Object value) {
    Object base = children[0].evalRaw(interpreter);
    if (!(base instanceof Array)) {
      throw new RuntimeException("Can't set inexed value to non-array" + value);
    }
    Array array = (Array) base;
    int[] indices = new int[array.getLocalVariableCount()];
    for (int i = 1; i < children.length; i++) {
      indices[i - 1] = evalInt(interpreter, i);
    }
    array.setAt(value, indices);
  }

  public Object eval(Interpreter interpreter) {
    Object base = children[0].evalRaw(interpreter);
    if (!(base instanceof Function)) {
      throw new RuntimeException("Can't apply parameters to " + base);
    }
    Function function = (Function) base;
    Object[] locals = new Object[function.getLocalVariableCount()];
    for (int i = 1; i < children.length; i++) {
      locals[i - 1] = children[i].eval(interpreter);
    }
    return  function.eval(interpreter, locals);
  }

  public Type returnType() {
    return children[0].toString().endsWith("$") ? Types.STRING : Types.NUMBER;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
     children[0].toString(asb, errors);
     asb.append('(');
     for (int i = 1; i < children.length; i++) {
          if (i > 1) {
              asb.append(", ");
          }
          children[i].toString(asb, errors);
      }
      asb.append(')');
  }
}
