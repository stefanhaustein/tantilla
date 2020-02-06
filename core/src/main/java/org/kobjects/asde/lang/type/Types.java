package org.kobjects.asde.lang.type;

import org.kobjects.asde.lang.type.EnumType;
import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.TypeImpl;
import org.kobjects.asde.lang.type.Typed;

import java.util.HashMap;

public class Types {


  private static HashMap<Object, Type> typeMap = new HashMap<>();

  public static final Type BOOL = new TypeImpl("bool", Boolean.FALSE);
  public static final Type FLOAT = new TypeImpl("float", 0.0);
  public static final Type STR = new TypeImpl("str", "");
  public static final Type VOID = new TypeImpl("void", null);

  public static Type of(Object o) {
    if (o == null) {
      return VOID;
    }
    if (o instanceof Typed) {
      return ((Typed) o).getType();
    }
    if (o instanceof Boolean) {
      return BOOL;
    }
    if (o instanceof Double) {
      return FLOAT;
    }
    if (o instanceof String) {
      return STR;
    }
    Type result = typeMap.get(o);
    if (result == null) {
      throw new IllegalArgumentException("Unrecognized type: " + o.getClass());
    }
    return result;
  }


  public static EnumType wrapEnum(String name, Object[] values) {
    EnumType type = new EnumType(name, values);
    for (Object value : values) {
      typeMap.put(value, type);
    }
    return type;
  }
}
