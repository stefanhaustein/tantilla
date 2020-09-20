package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.list.ListImpl;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.wasm.Wasm;
import org.kobjects.asde.lang.wasm.builder.WasmExpressionBuilder;
import org.kobjects.markdown.AnnotatedStringBuilder;

import java.util.Map;


public class Slice extends ExpressionNode {
  Type resolvedElementType;
  boolean forString;

  public Slice(Node... children) {
    super(children);
  }

  @Override
  protected Type resolveWasmImpl(WasmExpressionBuilder wasm, ValidationContext resolutionContext, int line) {
    Type type0 = children[0].resolveWasm(wasm, resolutionContext, line);
    forString = type0 == Types.STR;
    if (!forString && !(type0 instanceof ListType)) {
      throw new RuntimeException("Str or List base type expected.");
    }
    if (children.length != 3) {
      throw new RuntimeException("Expected two slicing argumnets; got: " + (children.length - 1));
    }
    boolean toEnd = false;
    resolvedElementType = forString ? Types.STR : ((ListType) type0).elementType;
    for (int i = 1; i < children.length; i++) {
      if (children[i] instanceof ImpliedSliceValue) {
        switch (i) {
          case 1:
            wasm.opCode(Wasm.F64_CONST);
            wasm.f64(0);
            break;
          case 2:
            if (forString) {
              wasm.callWithContext(context -> {
                String s = (String) context.dataStack.getObject(context.dataStack.size() - 2);
                context.dataStack.pushF64(s.codePointCount(0, s.length()));
              });
            } else {
              wasm.callWithContext(context -> {
                ListImpl l = (ListImpl) context.dataStack.getObject(context.dataStack.size() - 2);
                context.dataStack.pushF64(l.length());
              });
            }
            break;
        }
      } else {
        Type typeI = children[i].resolveWasm(wasm, resolutionContext, line);
        if (typeI != Types.FLOAT) {
          throw new RuntimeException("Slice arguments must be numbers.");
        }
      }
    }

    if (forString) {
      wasm.callWithContext(context -> {
        int end = (int) context.dataStack.popF64();
        int start = (int) context.dataStack.popF64();
        String s = (String) context.dataStack.popObject();
        if (start < 0) {
          start = s.codePointCount(0, s.length()) - start;
        }
        if (end < 0) {
          end = s.codePointCount(0, s.length()) - end;
        }
        int pos = 0;
        for (int i = 0; i < start; i++) {
          pos += Character.charCount(s.codePointAt(pos));
        }
        context.dataStack.pushObject(s.substring(start, end));
      });
    } else {
      wasm.callWithContext(context -> {
        int end = (int) context.dataStack.popF64();
        int start = (int) context.dataStack.popF64();
        ListImpl list = (ListImpl) context.dataStack.popObject();
        if (start < 0) {
          start = list.length() - start;
        }
        if (end < 0) {
          end = list.length() - end;
        }
        Object[] data = new Object[end - start];
        for (int i = start; i < end; i++) {
          data[i - start] = list.get(i);
        }
        context.dataStack.pushObject(new ListImpl(resolvedElementType, data));
      });
    }
    return resolvedType = type0;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors, boolean preferAscii) {
    int start = asb.length();
    children[0].toString(asb, errors, preferAscii);
    asb.append('[');
    for (int i = 1; i < children.length; i++) {
      if (i > 1) {
        asb.append(":");
      }
      children[i].toString(asb, errors, preferAscii);
    }
    asb.append(']');
    asb.annotate(start, asb.length(), errors.get(this));
  }
}
