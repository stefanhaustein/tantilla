package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;

public class Builtin extends Node {

  public enum Type {
    ABS(1, "D"), ASC(1, "S"), CHR$(1, "D"), COS(1, "D"), EXP(1, "D"), INT(1, "D"),
    LEFT$(2, "SD"), LEN(1, "S"), MID$(2, "SDD"), LOG(1, "D"), NEG(1, "D"), NOT(1, "D"),
    RIGHT$(2, "SD"), RND(0, "D"), SGN(1, "D"), STR$(1, "D"), SQR(1, "D"), SIN(1, "D"),
    TAB(1, "D"), TAN(1, "D"), VAL(1, "S");

    public int minParams;
    public String signature;

    Type(int minParams, String parameters) {
      this.minParams = minParams;
      this.signature = parameters;
    }
  }

  final Program program;
  final Type type;

  public Builtin(Program program, Type id, Node... args) {
    super(args);
    this.program = program;
    this.type = id;
  }

  public Object eval(Interpreter interpreter) {
    if (type == null) {
      return children[0].eval(interpreter);  // Grouping ().
    }
    switch (type) {
      case ABS: return Math.abs(evalDouble(interpreter, 0));
      case ASC: {
        String s = evalString(interpreter,0);
        return s.length() == 0 ? 0.0 : (double) s.charAt(0);
      }
      case CHR$: return String.valueOf((char) evalDouble(interpreter,0));
      case COS: return Math.cos(evalDouble(interpreter,0));
      case EXP: return Math.exp(evalDouble(interpreter,0));
      case INT: return Math.floor(evalDouble(interpreter,0));
      case LEFT$: {
        String s = evalString(interpreter,0);
        return s.substring(0, Math.min(s.length(), evalInt(interpreter,1)));
      }
      case LEN: return (double) evalString(interpreter,0).length();
      case LOG: return Math.log(evalDouble(interpreter,0));
      case MID$: {
        String s = evalString(interpreter,0);
        int start = Math.max(0, Math.min(evalInt(interpreter,1) - 1, s.length()));
        if (children.length == 2) {
          return s.substring(start);
        }
        int count = evalInt(interpreter,2);
        int end = Math.min(s.length(), start + count);
        return s.substring(start, end);
      }
      case NEG: return -evalDouble(interpreter,0);
      case NOT: return Double.valueOf(~((int) evalDouble(interpreter,0)));
      case SGN: return Math.signum(evalDouble(interpreter,0));
      case SIN: return Math.sin(evalDouble(interpreter,0));
      case SQR: return Math.sqrt(evalDouble(interpreter,0));
      case STR$: return Program.toString(evalDouble(interpreter,0));
      case RIGHT$: {
        String s = evalString(interpreter,0);
        return s.substring(Math.min(s.length(), s.length() - evalInt(interpreter,1)));
      }
      case RND: return Math.random();
      case TAB: return program.tab(evalInt(interpreter,0));
      case TAN: return Math.tan(evalDouble(interpreter,0));
      case VAL: return Double.parseDouble(evalString(interpreter,0));
      default:
        throw new IllegalArgumentException("NYI: " + type);
    }
  }

  public Object returnType() {
    return type == null ? children[0].returnType() : type.name().endsWith("$")
        ? String.class : Double.class;
  }

  public String toString() {
    if (type == null) {
      return children[0].toString();
    } else if (type == Type.NEG) {
      return "-" + children[0];
    } else if (type == Type.NOT) {
      return "NOT " + children[0];
    } else if (children.length == 0) {
      return type.name();
    }
    return type.name() + "(" + super.toString() + ")";
  }
}
