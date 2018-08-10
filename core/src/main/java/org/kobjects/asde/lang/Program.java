package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.typesystem.Classifier;
import org.kobjects.asde.lang.parser.Parser;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

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
  public CallableUnit main = new CallableUnit(this, new FunctionType(Type.VOID));
  public Map<String, Classifier> classifiers = new TreeMap<>();

  // Program state

  private TreeMap<String, GlobalSymbol> symbolMap = new TreeMap<>();
  public Exception lastException;
  public int[] stopped;
  public int tabPos;
  public boolean trace;
  public final Console console;

  public Program(Console console) {
    this.console = console;
    clear();

    for (Builtin builtin : Builtin.values()) {
        setSymbol(builtin.name().toLowerCase(), new GlobalSymbol(GlobalSymbol.Scope.BUILTIN, builtin));
    }
  }

  public void clear() {
      TreeMap<String, GlobalSymbol> cleared = new TreeMap<String, GlobalSymbol>();
    synchronized (symbolMap) {
        for (Map.Entry<String, GlobalSymbol> entry : symbolMap.entrySet()) {
            GlobalSymbol symbol = entry.getValue();
            if (symbol != null && symbol.scope != GlobalSymbol.Scope.TRANSIENT) {
                cleared.put(entry.getKey(), symbol);
            }
        }
        symbolMap = cleared;
    }
    stopped = null;
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

  public TreeMap<String, GlobalSymbol> getSymbolMap() {
      synchronized (symbolMap) {
          return new TreeMap<>(symbolMap);
      }
  }

  public GlobalSymbol getSymbol(String name) {
      synchronized (symbolMap) {
          return symbolMap.get(name);
      }
  }

  // Remove?
  public void setSymbol(String name, GlobalSymbol symbol) {
      synchronized (symbolMap) {
          symbolMap.put(name, symbol);
      }
  }


  public void toString(AnnotatedStringBuilder sb) {
      for (Map.Entry<String, GlobalSymbol> entry : getSymbolMap().entrySet()) {
          GlobalSymbol symbol = entry.getValue();
          if (symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT) {
              String name = entry.getKey();
              if (symbol.value instanceof CallableUnit) {
                  ((CallableUnit) symbol.value).toString(sb, name);
              }
          }
      }
      main.toString(sb, null);
  }

  public void println() {
    print("\n");
  }

}
