package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Identifier;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.asde.lang.parser.Parser;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



/**
 * Full implementation of <a href="http://goo.gl/kIIPc0">ECMA-55</a> with
 * some common additions.
 * <p>
 * Example for mixing the expresion parser with "outer" parsing.
 */
public class Program {
  private String name = "Scratch";

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

  public final Parser parser = new Parser(this);
  public final CallableUnit main = new CallableUnit(this, new FunctionType(Types.VOID));

 // Program state

  private TreeMap<String, GlobalSymbol> symbolMap = new TreeMap<>();
  public Exception lastException;
  public int tabPos;
  public final Console console;


  public Program(Console console) {
    this.console = console;
    // clear();

    for (Builtin builtin : Builtin.values()) {
        setValue(GlobalSymbol.Scope.BUILTIN, builtin.name().toLowerCase(), builtin);
    }
  }

  public void runInitializers(Interpreter interpreter) {

      synchronized (symbolMap) {
          for (GlobalSymbol symbol : symbolMap.values()) {
              if (symbol.initializer != null) {
                  symbol.initializer.eval(interpreter);
              }
          }
      }

  }

  public void clearAll() {
      main.clear();
      TreeMap<String, GlobalSymbol> cleared = new TreeMap<String, GlobalSymbol>();
      synchronized (symbolMap) {
          for (Map.Entry<String, GlobalSymbol> entry : symbolMap.entrySet()) {
              GlobalSymbol symbol = entry.getValue();
              if (symbol != null && symbol.scope == GlobalSymbol.Scope.BUILTIN) {
                  cleared.put(entry.getKey(), symbol);
              }
          }
          symbolMap = cleared;
      }
      name = "Scratch";
      console.programNameChangedTo(name);
  }


  public void clear(Interpreter interpreter) {
      console.clearOutput();
      console.clearCanvas();
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

    runInitializers(interpreter);
      Arrays.fill(interpreter.dataPosition, 0);
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

  private void setSymbol(String name, GlobalSymbol symbol) {
      synchronized (symbolMap) {
          symbolMap.put(name, symbol);
      }
  }

  public String getName() {
      return name;
  }

  public void toString(AnnotatedStringBuilder sb) {
      for (Map.Entry<String, GlobalSymbol> entry : getSymbolMap().entrySet()) {
          GlobalSymbol symbol = entry.getValue();
          if (symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT) {
              if (!(symbol.value instanceof CallableUnit)) {
                sb.append(symbol.toString(entry.getKey(), false)).append('\n');
              }
          }
      }

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


  @Override
  public String toString() {
      AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
      toString(asb);
      return asb.toString();
  }

  public void save(String name) {
      if (name != null) {
          this.name = name;
          console.programNameChangedTo(name);
      }
      File saveFile = new File(console.getProgramStoragePath(), this.name);

      try {
          OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(saveFile), "utf8");
          writer.write(toString());
          writer.close();
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
   }

    // move to parser
   private Type parseType(ExpressionParser.Tokenizer tokenizer) {
       String typeName = tokenizer.consumeIdentifier();
       if (typeName.equalsIgnoreCase("number")) {
           return Types.NUMBER;
       }
       if (typeName.equalsIgnoreCase("string")) {
           return Types.STRING;
       }
       GlobalSymbol symbol = getSymbol(typeName);
       if (symbol == null) {
          throw new RuntimeException("Unrecognized type: " + typeName);
       }
       if (!(symbol.value instanceof Type)) {
           throw new RuntimeException("'" + typeName + "' is not a type!");
       }
       return  (Type) symbol.value;
   }

   private Type[] parseParameterList(ExpressionParser.Tokenizer tokenizer, ArrayList<String> parameterNames) {
       tokenizer.consume("(");
       ArrayList<Type> parameterTypes = new ArrayList<>();
       while (!tokenizer.tryConsume(")")) {
           Type parameterType = parseType(tokenizer);
           parameterTypes.add(parameterType);
           String parameterName = tokenizer.consumeIdentifier();
           parameterNames.add(parameterName);

           if (!tokenizer.tryConsume(",")) {
               if (tokenizer.tryConsume(")")) {
                   break;
               }
               throw new RuntimeException("',' or ')' expected.");
           }
       }
       return parameterTypes.toArray(Type.EMTPY_ARRAY);
   }

   // move to parser
    private FunctionType parseFunctionSignature(ExpressionParser.Tokenizer tokenizer, ArrayList<String> parameterNames) {
      Type[] parameterTypes = parseParameterList(tokenizer, parameterNames);

      tokenizer.consume("->");
      Type returnType = parseType(tokenizer);

      return new FunctionType(returnType, parameterTypes);
    }

    // move to parser
    private FunctionType parseSubroutineSignature(ExpressionParser.Tokenizer tokenizer, ArrayList<String> parameterNames) {
        Type[] parameterTypes = parseParameterList(tokenizer, parameterNames);
        return new FunctionType(Types.VOID, parameterTypes);
    }


    public void load(String programName) {
      File programFile = programName.startsWith("/") ? new File(programName) : new File(console.getProgramStoragePath(), programName);
      try {
          int cut = programName.lastIndexOf("/");
          String displayName = programName.substring(cut + 1);
          cut = displayName.lastIndexOf(".");
          if (cut != -1) {
              displayName = displayName.substring(0, cut);
          }
          load(displayName, new FileInputStream(programFile));
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
    }

    public void processDeclarations(List<? extends Node> statements) {
        ResolutionContext resolutionContext = new ResolutionContext(this, ResolutionContext.ResolutionMode.SHELL, new FunctionType(Types.VOID));
        for (Node node : statements) {
            node.resolve(resolutionContext);
            if (node instanceof LetStatement) {
                setInitializer(GlobalSymbol.Scope.PERSISTENT, ((LetStatement) node).varName, node);
            } else if (node instanceof DimStatement) {
                DimStatement dim = (DimStatement) node;
                setInitializer(GlobalSymbol.Scope.PERSISTENT, dim.varName, new DimStatement(dim.varName, dim.children));
            }
        }
    }

    public void load(String programName, InputStream inputStream) {
      try {
          clearAll();
          this.name = programName;
          console.programNameChangedTo(programName);

          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
          CallableUnit currentFunction = main;
          while (true) {
              String line = reader.readLine();
              if (line == null) {
                  break;
              }
              System.out.println("Line: '" + line + "'");

              ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(line);
              tokenizer.nextToken();
              if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
                  int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
                  tokenizer.nextToken();

                  System.out.println("line number: " + lineNumber);

                  List<? extends Node> statements = parser.parseStatementList(tokenizer);

                  currentFunction.setLine(lineNumber, new CodeLine(statements));
              } else if (tokenizer.tryConsume("FUNCTION")) {
                  String functionName = tokenizer.consumeIdentifier();
                  ArrayList<String> parameterNames = new ArrayList();
                  FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames);
                  currentFunction = new CallableUnit(this, functionType, parameterNames.toArray(new String[0]));
                  setValue(GlobalSymbol.Scope.PERSISTENT, functionName, currentFunction);
              } else if (tokenizer.tryConsume("SUB")) {
                  String functionName = tokenizer.consumeIdentifier();
                  ArrayList<String> parameterNames = new ArrayList();
                  FunctionType functionType = parseSubroutineSignature(tokenizer, parameterNames);
                  currentFunction = new CallableUnit(this, functionType, parameterNames.toArray(new String[0]));
                  setValue(GlobalSymbol.Scope.PERSISTENT, functionName, currentFunction);
              } else if (tokenizer.tryConsume("END")) {
                  currentFunction = main;
              } else if (!tokenizer.tryConsume("")) {
                  List<? extends Node> statements = parser.parseStatementList(tokenizer);
                  processDeclarations(statements);
              }
          }
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
    }

    public void setValue(GlobalSymbol.Scope scope, String name, Object value) {
        GlobalSymbol symbol = getSymbol(name);
        if (symbol == null) {
            symbol = new GlobalSymbol(scope, value);
            setSymbol(name, symbol);
        } else {
            symbol.value = value;
        }
    }

    public void setInitializer(GlobalSymbol.Scope scope, String name, Node expr) {
      GlobalSymbol symbol = getSymbol(name);
      if (symbol == null) {
          symbol = new GlobalSymbol(scope, null);
          setSymbol(name, symbol);
      }
      symbol.initializer = expr;
    }



}
