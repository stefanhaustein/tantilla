package org.kobjects.asde.lang;

import org.kobjects.asde.lang.type.Classifier;
import org.kobjects.asde.lang.parser.Parser;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Full implementation of <a href="http://goo.gl/kIIPc0">ECMA-55</a> with
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

  private TreeMap<String, Symbol> symbolMap = new TreeMap<>();
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
      TreeMap<String, Symbol> cleared = new TreeMap<String, Symbol>();
    synchronized (symbolMap) {
        for (Map.Entry<String, Symbol> entry : symbolMap.entrySet()) {
            Symbol symbol = entry.getValue();
            if (symbol.persistent) {
                cleared.put(entry.getKey(), symbol);
            }
        }
        symbolMap = cleared;
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

  public TreeMap<String, Symbol> getSymbolMap() {
      synchronized (symbolMap) {
          return new TreeMap<>(symbolMap);
      }
  }

  public Symbol getSymbol(String name) {
      synchronized (symbolMap) {
          return symbolMap.get(name);
      }
  }

  // Remove?
  public void setSymbol(String name, Symbol symbol) {
      synchronized (symbolMap) {
          symbolMap.put(name, symbol);
      }
  }

  public void println() {
    print("\n");
  }

}
