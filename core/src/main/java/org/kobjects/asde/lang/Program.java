package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.Classifier;
import org.kobjects.asde.lang.parser.Parser;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Full implementation of <a href="http://goo.gl/kIIPc0">ECMA-55</a> minimal program with
 * some common additions.
 * <p>
 * Example for mixing the expresion parser with "outer" parsing.
 */
public class Program {
  public static final String INVISIBLE_STRING = new String();

  public static String toString(double d) {
    if (d == (int) d) {
      return String.valueOf((int) d);
    }
    return String.valueOf(d);
  }

  public static String toString(Object o) {
    return o instanceof Number ? toString(((Number) o).doubleValue()) : String.valueOf(o);
  }

  public Parser parser = new Parser(this);
  public CallableUnit main = new CallableUnit();
  public Map<String, Classifier> classifiers = new TreeMap<>();

  // Program state

  public TreeMap<String, Object> variables = new TreeMap<>();
  public Exception lastException;
 // TreeMap<String, double[]> forMap = new TreeMap<>();
  public TreeMap<String, DefFn> functionDefinitions = new TreeMap<>();

  public int[] stopped;
  public int tabPos;
  public boolean trace;
  public final Console console;

  public Program(Console console) {
    this.console = console;
    clear();
  }

  public void clear() {
    synchronized (variables) {
      variables.clear();
      variables.put("pi", Math.PI);
      variables.put("tau", 2 * Math.PI);
    }
    stopped = null;
    functionDefinitions.clear();
  }


  public String tab(int pos) {
    pos = Math.max(0, pos - 1);
    char[] fill;
    if (pos < tabPos) {
      fill = new char[pos + 1];
      Arrays.fill(fill, ' ');
      fill[0] = '\n';
    } else {
      fill = new char[pos - tabPos];
      Arrays.fill(fill, ' ');
    }
    return new String(fill);
  }

  public void println(Object o) {
    print(o + "\n");
  }

  public void print(Object o) {
    String s = String.valueOf(o);
    console.print(s);
    int cut = s.lastIndexOf('\n');
    if (cut == -1) {
      tabPos += s.length();
    } else {
      tabPos = s.length() - cut - 1;
    }
  }

  public void println() {
    print("\n");
  }

}
