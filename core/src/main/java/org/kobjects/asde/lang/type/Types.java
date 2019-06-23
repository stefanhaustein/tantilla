package org.kobjects.asde.lang.type;

import org.kobjects.typesystem.EnumType;
import org.kobjects.typesystem.Type;
import org.kobjects.typesystem.TypeImpl;
import org.kobjects.typesystem.Typed;

import java.util.HashMap;

public class Types {
  private static HashMap<Object, Type> typeMap = new HashMap<>();

  public static final Type BOOLEAN = new TypeImpl("Boolean");
  public static final Type NUMBER = new TypeImpl("Number");
  public static final Type STRING = new TypeImpl("String");
  public static final Type VOID = new TypeImpl("Void");

  public static Type of(Object o) {
    if (o == null) {
      return VOID;
    }
    if (o instanceof Typed) {
      return ((Typed) o).getType();
    }
    if (o instanceof Boolean) {
      return BOOLEAN;
    }
    if (o instanceof Double) {
      return NUMBER;
    }
    if (o instanceof String) {
      return STRING;
    }
    Type result = typeMap.get(o);
    if (result == null) {
      throw new IllegalArgumentException("Unrecognized type: " + o.getClass());
    }
    return result;
  }

  /**
   * Interprets null as wildcard.
   */
  // TODO: Remove when validate works for the declaration block.
  public static boolean match(Type type1, Type type2) {
    return type1 == null || type2 == null || type1.equals(type2);
  }

  public static EnumType wrapEnum(Object[] values) {
    EnumType type = new EnumType(values);
    for (Object value : values) {
      typeMap.put(value, type);
    }
    return type;
  }
}
