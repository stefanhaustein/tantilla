package org.kobjects.asde.lang.io;

public class Format {

  public static String exceptionToString(Throwable e) {
    StringBuilder sb = new StringBuilder();
    while (true) {
      if (e instanceof IndexOutOfBoundsException) {
        sb.append("Array index out of bounds: " + e.getMessage());
      } else if (e.getClass() == RuntimeException.class) {
        if (e.getCause() != null) {
          e = e.getCause();
          continue;
        }
        sb.append(e.getMessage());
      } else {
        sb.append(e.toString());
      }

      if (e.getCause() == null) {
        break;
      }
      sb.append(" caused by ");
      e = e.getCause();
    }
    return sb.toString();
  }
}
