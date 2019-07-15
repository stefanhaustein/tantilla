package org.kobjects.asde.lang;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.event.ProgramChangeListener;
import org.kobjects.asde.lang.event.ProgramRenameListener;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.node.Visitor;
import org.kobjects.asde.lang.parser.ProgramParser;
import org.kobjects.asde.lang.refactor.RenameGlobal;
import org.kobjects.asde.lang.statement.DimStatement;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Builtin;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.parser.StatementParser;
import org.kobjects.typesystem.FunctionTypeImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    public final StatementParser parser = new StatementParser(this);
    public final FunctionImplementation main = new FunctionImplementation(this, new FunctionTypeImpl(Types.VOID));
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


  public synchronized void clear(EvaluationContext evaluationContext) {
      console.clearScreen(Console.ClearScreenType.CLEAR_STATEMENT);
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




    // Can't include validate -- wouldn't work for loading.
    public void processDeclarations(CodeLine codeLine) {
        for (int i = 0; i < codeLine.length(); i++) {
            Node node = codeLine.get(i);
            if (node instanceof DeclarationStatement) {
                DeclarationStatement declaration = (DeclarationStatement) node;
                setPersistentInitializer(declaration.varName, declaration);
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

          new ProgramParser(this).parseProgram(reader);

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
        FunctionValidationContext functionValidationContext = new FunctionValidationContext(context,
                FunctionValidationContext.ResolutionMode.FUNCTION,
                main);
        main.validate(functionValidationContext);
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

    public synchronized void setDeclaration(String name, Declaration declaration) {
        GlobalSymbol symbol = getSymbol(name);
        if (symbol == null) {
            symbol = new GlobalSymbol(this, name, GlobalSymbol.Scope.PERSISTENT, declaration);
            symbolMap.put(name, symbol);
        } else {
            if (symbol.getScope() == GlobalSymbol.Scope.BUILTIN) {
                throw new RuntimeException("Can't overwriter builtin '" + name + "'");
            }
            symbol.value = declaration;
        }
        symbol.setConstant(true);
        declaration.setDeclaringSymbol(symbol);

        notifySymbolChanged(symbol);
    }

    public synchronized void setPersistentInitializer(String name, Node expr) {
      GlobalSymbol symbol = getSymbol(name);
      if (symbol == null || symbol.scope == GlobalSymbol.Scope.TRANSIENT) {
          symbol = new GlobalSymbol(this, name, GlobalSymbol.Scope.PERSISTENT, null);
          symbolMap.put(name, symbol);
      } else if (symbol.getScope() == GlobalSymbol.Scope.BUILTIN) {
          throw new RuntimeException("Can't overwrite builtin '" + name + "'");
      }
      symbol.initializer = expr;
      symbol.setConstant(expr instanceof DeclarationStatement && ((DeclarationStatement) expr).kind == DeclarationStatement.Kind.CONST);
      notifyProgramChanged();
    }

    public void setLine(StaticSymbol symbol, CodeLine codeLine) {
        if (symbol.getValue() instanceof FunctionImplementation) {
            FunctionImplementation functionImplementation = (FunctionImplementation) symbol.getValue();
            functionImplementation.setLine(codeLine);
            notifySymbolChanged(symbol);
        }
    }

    public void deleteLine(StaticSymbol symbol, int line) {
        if (symbol.getValue() instanceof FunctionImplementation) {
            FunctionImplementation functionImplementation = (FunctionImplementation) symbol.getValue();
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

    public void notifySymbolChanged(StaticSymbol symbol) {
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
        notifyProgramChanged();
    }

}
