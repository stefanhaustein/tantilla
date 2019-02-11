package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.event.ProgramChangeListener;
import org.kobjects.asde.lang.event.ProgramRenameListener;
import org.kobjects.asde.lang.io.Formatting;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.asde.lang.refactor.RenameGlobal;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.LetStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Builtin;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
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
    public final FunctionImplementation main = new FunctionImplementation(this, new FunctionType(Types.VOID));
    public final GlobalSymbol mainSymbol = new GlobalSymbol(this, "", GlobalSymbol.Scope.PERSISTENT, main);
    private final ArrayList<ProgramChangeListener> programChangeListeners = new ArrayList<>();
    private final ArrayList<ProgramRenameListener> programRenameListeners = new ArrayList<>();

    // Program state

    private TreeMap<String, GlobalSymbol> symbolMap = new TreeMap<>();
    public Exception lastException;
    public int tabPos;
    public final Console console;
    public boolean legacyMode;
    private boolean loading;

    public Program(Console console) {
      this.console = console;
      main.setDeclaringSymbol(mainSymbol);
      this.reference = new ProgramReference("Scratch", null, false);
      for (Builtin builtin : Builtin.values()) {
          setValue(GlobalSymbol.Scope.BUILTIN, builtin.name().toLowerCase(), builtin);
      }
    }

    public synchronized void renameGlobalSymbol(String oldName, String newName) {
        if (newName == null || newName.isEmpty() || newName.equals(oldName)) {
            return;
        }
        GlobalSymbol symbol = symbolMap.get(oldName);
        if (symbol == null) {
            throw new RuntimeException("Symbol '" + oldName + "' does not exist.");
        }
        symbolMap.remove(oldName);
        symbol.setName(newName);
        symbolMap.put(newName, symbol);
        accept(new RenameGlobal(oldName, newName));
    }


    public void accept(Visitor visitor) {
        visitor.visitProgram(this);
    }

  public synchronized void deleteAll() {
      main.clear();
      TreeMap<String, GlobalSymbol> cleared = new TreeMap<String, GlobalSymbol>();
      for (Map.Entry<String, GlobalSymbol> entry : symbolMap.entrySet()) {
          GlobalSymbol symbol = entry.getValue();
          if (symbol != null && symbol.scope == GlobalSymbol.Scope.BUILTIN) {
               cleared.put(entry.getKey(), symbol);
          }
      }
      symbolMap = cleared;
      notifyProgramChanged();
      reference = console.nameToReference("Unnamed");
      notifyProgramRenamed();
  }


  public synchronized void init(EvaluationContext evaluationContext) {
      TreeMap<String, GlobalSymbol> cleared = new TreeMap<String, GlobalSymbol>();
      for (Map.Entry<String, GlobalSymbol> entry : symbolMap.entrySet()) {
          GlobalSymbol symbol = entry.getValue();
          if (symbol != null && symbol.scope != GlobalSymbol.Scope.TRANSIENT) {
              cleared.put(entry.getKey(), symbol);
          }
      }
      symbolMap = cleared;

      HashSet<GlobalSymbol> initialized = new HashSet<>();

      for (GlobalSymbol symbol : symbolMap.values()) {
          symbol.init(evaluationContext, initialized);
      }
      Arrays.fill(evaluationContext.getDataPosition(), 0);
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

  public synchronized Iterable<GlobalSymbol> getSymbols() {
      return new ArrayList<>(symbolMap.values());
  }

  public synchronized GlobalSymbol getSymbol(String name) {
      return symbolMap.get(name);
  }

  public synchronized void toString(AnnotatedStringBuilder sb) {
      for (GlobalSymbol symbol : symbolMap.values()) {
          if (symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT) {
              if (!(symbol.value instanceof FunctionImplementation)) {
                sb.append(symbol.toString(false)).append('\n');
              }
          }
      }

      for (GlobalSymbol symbol : symbolMap.values()) {
          if (symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT) {
              if (symbol.value instanceof FunctionImplementation) {
                  ((FunctionImplementation) symbol.value).toString(sb, symbol.getName(), symbol.getErrors());
              }
          }
      }
      main.toString(sb, null, mainSymbol.getErrors());
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
       String typeName = Formatting.toUpperCamel(tokenizer.consumeIdentifier());
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


    // Can't include validate -- wouldn't work for loading.
    public void processDeclarations(CodeLine codeLine) {
        FunctionImplementation wrapper = new FunctionImplementation(this, new FunctionType(Types.VOID));
        wrapper.setLine(codeLine);
        for (int i = 0; i < codeLine.length(); i++) {
            Node node = codeLine.get(i);
            if (node instanceof LetStatement) {
                LetStatement let = (LetStatement) node;
                setPersistentInitializer(let.varName, let);
            } else if (node instanceof DimStatement) {
                DimStatement dim = (DimStatement) node;
                setPersistentInitializer(dim.varName, dim);
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
          HashSet<FunctionImplementation> functionImplementations = new HashSet<>();

          FunctionImplementation currentFunction = main;
          functionImplementations.add(main);
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
                  currentFunction.setLine(new CodeLine(lineNumber, statements));
              } else if (tokenizer.tryConsume("FUNCTION")) {
                  String functionName = tokenizer.consumeIdentifier();
                  console.updateProgress("Parsing function " + functionName);
                  ArrayList<String> parameterNames = new ArrayList();
                  FunctionType functionType = parseFunctionSignature(tokenizer, parameterNames);
                  currentFunction = new FunctionImplementation(this, functionType, parameterNames.toArray(new String[0]));
                  functionImplementations.add(currentFunction);
                  setPersistentFunction(functionName, currentFunction);
              } else if (tokenizer.tryConsume("SUB")) {
                  String functionName = tokenizer.consumeIdentifier();
                  console.updateProgress("Parsing subroutine " + functionName);
                  ArrayList<String> parameterNames = new ArrayList();
                  FunctionType functionType = parseSubroutineSignature(tokenizer, parameterNames);
                  currentFunction = new FunctionImplementation(this, functionType, parameterNames.toArray(new String[0]));
                  functionImplementations.add(currentFunction);
                  setPersistentFunction(functionName, currentFunction);
              } else if (tokenizer.tryConsume("END")) {
                  currentFunction = main;
              } else if (!tokenizer.tryConsume("")) {
                  List<? extends Node> statements = parser.parseStatementList(tokenizer, null);
                  CodeLine codeLine = new CodeLine(-2, statements);
                  processDeclarations(codeLine);
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
            context.startChain(entry.getKey());
            entry.getValue().validate(context);
        }
        main.validate(context);
    }

    public synchronized void setValue(GlobalSymbol.Scope scope, String name, Object value) {
        GlobalSymbol symbol = getSymbol(name);
        if (symbol == null) {
            symbol = new GlobalSymbol(this, name, scope, value);
            symbolMap.put(name, symbol);
        } else {
            // TODO: check scope!
            symbol.value = value;
        }
        if (scope == GlobalSymbol.Scope.PERSISTENT) {
            notifySymbolChanged(symbol);
        }
    }

    public synchronized void setPersistentFunction(String name, FunctionImplementation implementation) {
        GlobalSymbol symbol = getSymbol(name);
        if (symbol == null) {
            symbol = new GlobalSymbol(this, name, GlobalSymbol.Scope.PERSISTENT, implementation);
            symbolMap.put(name, symbol);
        } else {
            if (symbol.getScope() == GlobalSymbol.Scope.BUILTIN) {
                throw new RuntimeException("Can't overwriter builtin '" + name + "'");
            }
            symbol.value = implementation;
        }
        symbol.setConstant(true);
        implementation.setDeclaringSymbol(symbol);

        notifySymbolChanged(symbol);
    }

    public synchronized void setPersistentInitializer(String name, Node expr) {
      GlobalSymbol symbol = getSymbol(name);
      if (symbol == null) {
          symbol = new GlobalSymbol(this, name, GlobalSymbol.Scope.PERSISTENT, null);
          symbolMap.put(name, symbol);
      } else if (symbol.getScope() == GlobalSymbol.Scope.BUILTIN) {
          throw new RuntimeException("Can't overwriter builtin '" + name + "'");
      }
      symbol.initializer = expr;
      symbol.setConstant(expr instanceof LetStatement && ((LetStatement) expr).isConstant());
      notifyProgramChanged();
    }

    public void setLine(GlobalSymbol symbol, CodeLine codeLine) {
        if (symbol.value instanceof FunctionImplementation) {
            FunctionImplementation functionImplementation = (FunctionImplementation) symbol.value;
            functionImplementation.setLine(codeLine);
            notifySymbolChanged(symbol);
        }
    }

    public void deleteLine(GlobalSymbol symbol, int line) {
        if (symbol.value instanceof FunctionImplementation) {
            FunctionImplementation functionImplementation = (FunctionImplementation) symbol.value;
            functionImplementation.deleteLine(line);
        }
    }


    public void addProgramRenameListener(ProgramRenameListener listener) {
        programRenameListeners.add(listener);
    }

    public void addProgramChangeListener(ProgramChangeListener programChangeListener) {
        programChangeListeners.add(programChangeListener);
    }

    public void notifyProgramRenamed() {
        for (ProgramRenameListener renameListener : programRenameListeners) {
            renameListener.programRenamed(this, reference);
        }
    }

    public void notifySymbolChanged(GlobalSymbol symbol) {
        if (loading) {
            return;
        }
        symbol.validate();
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
