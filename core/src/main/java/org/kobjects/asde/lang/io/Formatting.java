package org.kobjects.asde.lang.io;

public class Formatting {

    public static final String toUpperCamel(String s) {
        return s.isEmpty() ? s : (Character.toUpperCase(s.charAt(0)) + s.substring(1));
    }

    public static final String toLowerCamel(String s) {
        return s.isEmpty() ? s : (Character.toLowerCase(s.charAt(0)) + s.substring(1));
    }

}
