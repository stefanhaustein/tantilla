package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.asde.lang.refactor.RenameGlobal;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.symbol.GlobalSymbol;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.asde.lang.parser.Parser;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    public ProgramReference reference;

    public final Parser parser = new Parser(this);
    public final CallableUnit main = new CallableUnit(this, new FunctionType(Types.VOID));
    public final GlobalSymbol mainSymbol = new GlobalSymbol(GlobalSymbol.Scope.PERSISTENT, main);
    private final ArrayList<ProgramChangeListener> programChangeListeners = new ArrayList<>();

    // Program state

    private TreeMap<String, GlobalSymbol> symbolMap = new TreeMap<>();
    public Exception lastException;
    public int tabPos;
    public final Console console;
    public boolean legacyMode;
    private boolean loading;

    public Program(Console console) {
      this.console = console;
      // init();
      this.reference = new ProgramReference("Scratch", null, false);

      for (Builtin builtin : Builtin.values()) {
          setValue(GlobalSymbol.Scope.BUILTIN, builtin.name().toLowerCase(), builtin);
        }
    }


    public void renameGlobalSymbol(String oldName, String newName) {
        if (newName == null || newName.isEmpty() || newName.equals(oldName)) {
            return;
        }
        symbolMap.put(newName, symbolMap.get(oldName));
        symbolMap.remove(oldName);
        accept(new RenameGlobal(oldName, newName));
    }


    public void accept(Visitor visitor) {
        visitor.visitProgram(this);
    }

  public void deleteAll() {
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
      notifyProgramChanged();
      reference = console.nameToReference("Unnamed");
      notifyProgramRenamed();
  }


  public void init(Interpreter interpreter) {
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


      HashSet<GlobalSymbol> initialized = new HashSet<>();

      // It's a new symbolMap now!
      synchronized (symbolMap) {
          for (GlobalSymbol symbol : symbolMap.values()) {
              symbol.init(interpreter, initialized);
          }
      }

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
                  ((CallableUnit) symbol.value).toString(sb, name, symbol.errors);
              }
          }
      }
      main.toString(sb, null, mainSymbol.errors);
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

    public void save(ProgramReference programReference) throws IOException {
      if (!programReference.urlWritable) {
          throw new IOException("Can't write to URL: " + programReference.url);
      }
      reference = programReference;
      notifyProgramRenamed();
      OutputStreamWriter writer = new OutputStreamWriter(console.openOutputStream(programReference.url), "utf8");
      writer.write(toString());
      writer.close();
   }

    // move to parser
   private Type parseType(ExpressionParser.Tokenizer tokenizer) {
       String typeName = Case.toUpperCamel(tokenizer.consumeIdentifier());
       if (typeName.equals("Number")) {
           return Types.NUMBER;
       }
       if (typeName.equals("String")) {
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


    public void processDeclarations(List<? extends Node> statements) {
        CallableUnit wrapper = new CallableUnit(this, new FunctionType(Types.VOID));
        wrapper.setLine(-2, new CodeLine(statements));
        boolean syncNeeded = false;
        for (int i = 0; i < statements.size(); i++) {
            Node node = statements.get(i);
            if (node instanceof LetStatement) {
                LetStatement let = (LetStatement) node;
                setInitializer(GlobalSymbol.Scope.PERSISTENT, let.varName, let);
            } else if (node instanceof DimStatement) {
                DimStatement dim = (DimStatement) node;
                setInitializer(GlobalSymbol.Scope.PERSISTENT, dim.varName, dim);
            }
        }
    }


    public void load(ProgramReference fileReference) throws IOException {

      console.startProgress("Loading " + fileReference.name);
      console.updateProgress("Url: " + fileReference.url);

      loading = true;

      try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(console.openInputStream(fileReference.url), "utf-8"));

          deleteAll();
          this.reference = fileReference;
          notifyProgramRenamed();
          HashSet<CallableUnit> callableUnits = new HashSet<>();

          CallableUnit currentFunction = main;
          callableUnits.add(main);
          while (true) {
              String line = reader.readLine();
              if (line == null) {
                  break;
              }
              System.out.println("Parsing: '" + line + "'");

              ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(line);
              tokenizer.nextToken();
              if (tokenizer.currentType == ExpressionParser.Tokenizer.TokenType.NUMBER) {
                  int lineNumber = (int) Double.parseDouble(tokenizer.currentValue);
                  tokenizer.nextToken();
                  List<? extends Node> statements = parser.parseStatementList(tokenizer, currentFunction);
                  currentFunction.setLine(lineNumber, new CodeLine(statements));
              } else if (tokenizer.tryConsume("FUNCTION")) {
                  String functionName = tokenizer.consumeIdentifier();
                  console.updateProgress("Parsing function " + functionName);
                  ArrayList<String> parameterNames = new ArrayList();
                  FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames);
                  currentFunction = new CallableUnit(this, functionType, parameterNames.toArray(new String[0]));
                  callableUnits.add(currentFunction);
                  setValue(GlobalSymbol.Scope.PERSISTENT, functionName, currentFunction);
              } else if (tokenizer.tryConsume("SUB")) {
                  String functionName = tokenizer.consumeIdentifier();
                  console.updateProgress("Parsing subroutine " + functionName);
                  ArrayList<String> parameterNames = new ArrayList();
                  FunctionType functionType = parseSubroutineSignature(tokenizer, parameterNames);
                  currentFunction = new CallableUnit(this, functionType, parameterNames.toArray(new String[0]));
                  callableUnits.add(currentFunction);
                  setValue(GlobalSymbol.Scope.PERSISTENT, functionName, currentFunction);
              } else if (tokenizer.tryConsume("END")) {
                  currentFunction = main;
              } else if (!tokenizer.tryConsume("")) {
                  List<? extends Node> statements = parser.parseStatementList(tokenizer, null);
                  processDeclarations(statements);
              }
          }

          validate();

      } finally {
          console.endProgress();
          loading = false;
      }

      notifyProgramChanged();
    }

    public void validate() {
        ProgramValidationContext context = new ProgramValidationContext(this);
        for (Map.Entry<String,GlobalSymbol> entry : symbolMap.entrySet()) {
            context.resetChain(entry.getKey());
            entry.getValue().validate(context);
        }
        main.validate(context);
    }



    public void setValue(GlobalSymbol.Scope scope, String name, Object value) {
        GlobalSymbol symbol = getSymbol(name);
        if (symbol == null) {
            symbol = new GlobalSymbol(scope, value);
            setSymbol(name, symbol);
        } else {
            // TODO: check scope!
            symbol.value = value;
        }
        if (scope == GlobalSymbol.Scope.PERSISTENT) {
            notifySymbolChanged(symbol);
        }
    }

    public void setInitializer(GlobalSymbol.Scope scope, String name, Node expr) {
      GlobalSymbol symbol = getSymbol(name);
      if (symbol == null) {
          symbol = new GlobalSymbol(scope, null);
          setSymbol(name, symbol);
      }
      symbol.initializer = expr;
      notifyProgramChanged();
    }

    public void setLine(GlobalSymbol symbol, int lineNumber, CodeLine codeLine) {
        if (symbol.value instanceof CallableUnit) {
            CallableUnit callableUnit = (CallableUnit) symbol.value;
            callableUnit.setLine(lineNumber, codeLine);
            notifySymbolChanged(symbol);
        }
    }

    public void addProgramChangeListener(ProgramChangeListener programChangeListener) {
        programChangeListeners.add(programChangeListener);
    }

    public void notifyProgramRenamed() {
        for (ProgramChangeListener changeListener : programChangeListeners) {
            changeListener.programRenamed(this, reference);
        }
    }


    public void notifySymbolChanged(GlobalSymbol symbol) {
        if (loading) {
            return;
        }validate();
        for (ProgramChangeListener changeListener : programChangeListeners) {
            changeListener.symbolChangedByUser(this, symbol);
        }
    }

    public void notifyProgramChanged() {
        if (loading) {
            return;
        }
        validate();
        for (ProgramChangeListener changeListener : programChangeListeners) {
            changeListener.programChanged(this);
        }
    }

    public void deleteSymbol(String name) {
        symbolMap.remove(name);
    }
}
