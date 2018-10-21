package org.kobjects.asde.lang;

import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Parameter;
import org.kobjects.typesystem.Type;

public enum Builtin implements Function {

    ABS(1, Types.NUMBER), ASC(1, Types.STRING),
    CHR$(1, Types.NUMBER), COS(1, Types.NUMBER),
    EXP(1, Types.NUMBER),
    INT(1, Types.NUMBER),
    LEFT$(2, Types.STRING, Types.NUMBER), LEN(1, Types.STRING), LOG(1, Types.NUMBER),
    MID$(2, Types.STRING, Types.NUMBER, Types.NUMBER),
    RIGHT$(2, Types.STRING, Types.NUMBER), RND(0, Types.NUMBER),
    SGN(1, Types.NUMBER), STR$(1, Types.NUMBER), SQR(1, Types.NUMBER), SIN(1, Types.NUMBER),
    TAB(1, Types.NUMBER), TAN(1, Types.NUMBER),
    VAL(1, Types.STRING);

  public static int asInt(Object o) {
    return Math.round(((Double) o).floatValue());
  }


  public int minParams;
    public FunctionType signature;

    Builtin(int minParams, Type... parameterTypes) {
      this.minParams = minParams;
      Parameter[] parameters = new Parameter[parameterTypes.length];
      for (int i = 0; i < parameters.length; i++) {
        parameters[i] = new Parameter(String.valueOf((char) ('a' + i)), parameterTypes[i]);
      }
      this.signature = new FunctionType(name().endsWith("$") ? Types.STRING : Types.NUMBER, parameterTypes);
    }

  @Override
  public FunctionType getType() {
    return signature;
  }

  public int getLocalVariableCount() {
      return signature.getParameterCount();
  }

  public Object call(Interpreter interpreter, int paramCount) {
    LocalStack localStack = interpreter.localStack;
    switch (this) {
      case ABS: return Math.abs((Double) localStack.getParameter(0, paramCount));
      case ASC: {
        String s = (String) localStack.getParameter(0, paramCount);
        return s.length() == 0 ? 0.0 : (double) s.charAt(0);
      }
      case CHR$: return String.valueOf((char) ((Double)(localStack.getParameter(0, paramCount))).intValue());
      case COS: return Math.cos((Double) localStack.getParameter(0, paramCount));
      case EXP: return Math.exp((Double) localStack.getParameter(0, paramCount));
      case INT: return Math.floor((Double) localStack.getParameter(0, paramCount));
      case LEFT$: {
        String s = (String) localStack.getParameter(0, paramCount);
        return s.substring(0, Math.min(s.length(), asInt(localStack.getParameter(1, paramCount))));
      }
      case LEN: return (double) ((String) localStack.getParameter(0, paramCount)).length();
      case LOG: return Math.log((Double) localStack.getParameter(0, paramCount));
      case MID$: {
        String s = (String) localStack.getParameter(0, paramCount);
        int start = Math.max(0, Math.min(asInt(localStack.getParameter(1, paramCount)) - 1, s.length()));
        if (paramCount == 2) {
          return s.substring(start);
        }
        int count = asInt(localStack.getParameter(2, paramCount));
        int end = Math.min(s.length(), start + count);
        return s.substring(start, end);
      }
      case SGN: return Math.signum((Double) localStack.getParameter(0, paramCount));
      case SIN: return Math.sin((Double) localStack.getParameter(0, paramCount));
      case SQR: return Math.sqrt((Double) localStack.getParameter(0, paramCount));
      case STR$: return Program.toString(localStack.getParameter(0, paramCount));
      case RIGHT$: {
        String s = (String) localStack.getParameter(0, paramCount);
        return s.substring(Math.min(s.length(), s.length() - asInt(localStack.getParameter(1, paramCount))));
      }
      case RND: return Math.random();
      case TAB: return interpreter.control.program.tab(asInt(localStack.getParameter(1, paramCount)));
      case TAN: return Math.tan((Double) localStack.getParameter(0, paramCount));
      case VAL: return Double.parseDouble((String) localStack.getParameter(0, paramCount));
      default:
        throw new IllegalArgumentException("NYI: " + name());
    }
  }

}
