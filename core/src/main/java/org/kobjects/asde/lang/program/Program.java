package org.kobjects.asde.lang.program;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.Module;
import org.kobjects.asde.lang.symbol.Declaration;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.symbol.SymbolOwner;
import org.kobjects.asde.lang.function.BuiltinFunction;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.symbol.SymbolChangeListener;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.node.NodeProcessor;
import org.kobjects.asde.lang.parser.ProgramParser;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.Builtin;
import org.kobjects.asde.lang.function.CodeLine;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.parser.StatementParser;
import org.kobjects.asde.lang.function.FunctionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class Program implements SymbolOwner {

  public static final String INVISIBLE_STRING = new String();

  public static String toString(double d) {
    return d == (int) d ? String.valueOf((int) d) : String.valueOf(d);
  }

  public static String toString(Object o) {
    if (Boolean.TRUE.equals(o)) {
      return "True";
    }
    if (Boolean.FALSE.equals(o)) {
      return "False";
    }
    if (o instanceof Number) {
      return toString(((Number) o).doubleValue());
    }
    return String.valueOf(o);
  }

  public ProgramReference reference;

  public final StatementParser parser = new StatementParser(this);
  public final UserFunction main = new UserFunction(this, new FunctionType(Types.VOID));
  public final GlobalSymbol mainSymbol = new GlobalSymbol(this, "", GlobalSymbol.Scope.PERSISTENT, main);
  private final ArrayList<SymbolChangeListener> programChangeListeners = new ArrayList<>();
  private final ArrayList<ProgramListener> programListeners = new ArrayList<>();

  // Program state

  private TreeMap<String, GlobalSymbol> symbolMap = new TreeMap<>();
  public Exception lastException;
  public int tabPos;
  public final Console console;
  private boolean loading;
  public int currentStamp;
  public boolean hasUnsavedChanges;

  private boolean notificationPending;
  private StaticSymbol notificationPendingForSymbol;
  private Timer notificationTimer = new Timer();
  public Module mainModule = new Module(this);

  public Program(Console console) {
    this.console = console;
    main.setDeclaringSymbol(mainSymbol);
    this.reference = console.nameToReference(null);
    // Primitives can't be registered because of ambiguities with conversion functions!
    addBuiltin("List", new ListType(Types.VOID));
    synchronized (this) {
      addBuiltin("input", new BuiltinFunction((a, b) -> console.input(), "Reads a string as input from the user.", Types.STR));
    }
    for (Builtin builtin : Builtin.values()) {
      mainModule.addBuiltin(builtin.name().toLowerCase(), builtin);
    }
  }

  public void processNodes(Consumer<Node> action) {
    new NodeProcessor(action).processProgram(this);
    notifyProgramChanged();
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
    ProgramReference newReference = console.nameToReference(null);
    if (!reference.equals(newReference)) {
      reference = newReference;
      sendProgramEvent(ProgramListener.Event.LOADED);
    }
    notifyProgramChanged();
  }


  public synchronized void clear(EvaluationContext evaluationContext) {
    console.clearScreen(Console.ClearScreenType.CLEAR_STATEMENT);
    TreeMap<String, GlobalSymbol> cleared = new TreeMap<String, GlobalSymbol>();
    for (Map.Entry<String, GlobalSymbol> entry : symbolMap.entrySet()) {
      GlobalSymbol symbol = entry.getValue();
      if (symbol != null
          && (symbol.scope != GlobalSymbol.Scope.TRANSIENT || symbol.stamp == currentStamp)) {
        cleared.put(entry.getKey(), symbol);
      }
    }
    symbolMap = cleared;

    HashSet<GlobalSymbol> initialized = new HashSet<>();

    for (GlobalSymbol symbol : symbolMap.values()) {
      symbol.init(evaluationContext, initialized);
    }
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

  @Override
  public synchronized GlobalSymbol getSymbol(String name) {
    return symbolMap.get(name);
  }

  /**
   * Used for deletion and in refactoring to remove a symbol temporarily for renaming.
   * In the latter case, the symbol will be re-added via addSymbol() under a different name.
   */
  @Override
  public synchronized void removeSymbol(StaticSymbol symbol) {
    symbolMap.remove(symbol.getName());
    notifyProgramChanged();
  }

  public synchronized void toString(AnnotatedStringBuilder sb) {
    sb.append("ASDE\n");
    for (GlobalSymbol symbol : symbolMap.values()) {
      if (symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT) {
        if (!(symbol.value instanceof UserFunction)) {
          sb.append(symbol.toString(false)).append('\n');
        }
      }
    }

    for (GlobalSymbol symbol : symbolMap.values()) {
      if (symbol != null && symbol.scope == GlobalSymbol.Scope.PERSISTENT) {
        if (symbol.value instanceof UserFunction) {
          ((UserFunction) symbol.value).toString(sb, symbol.getName(), symbol.getErrors());
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
    if (!programReference.equals(reference)) {
      reference = programReference;
      sendProgramEvent(ProgramListener.Event.RENAMED);
    }
    OutputStreamWriter writer = new OutputStreamWriter(console.openOutputStream(programReference.url), "utf8");
    writer.write(toString());
    writer.close();
    hasUnsavedChanges = false;
  }


  /**
   * Generates persistent symbols for "standalone" declarations, i.e. declarations not
   * inside a function. Called from the interactive shell and from program loading to process declarations.
   * Because it's called from loading, it can't include validation.
   */
  public void processStandaloneDeclarations(CodeLine codeLine) {
    for (int i = 0; i < codeLine.length(); i++) {
      Node node = codeLine.get(i);
      if (node instanceof DeclarationStatement) {
        DeclarationStatement declaration = (DeclarationStatement) node;
        setPersistentInitializer(declaration.getVarName(), declaration);
      }
    }
  }


  public void load(ProgramReference fileReference) throws IOException {

    console.startProgress("Loading " + fileReference.name);
    console.updateProgress("Url: " + fileReference.url);

    loading = true;
    System.out.println("########  lading set to true");

    hasUnsavedChanges = false;

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(console.openInputStream(fileReference.url), "utf-8"));

      deleteAll();
      this.reference = fileReference;

      new ProgramParser(this).parseProgram(reader);


    } finally {
      console.endProgress();
      System.out.println("########  lading set to false");
      loading = false;

      // change notification triggers validation
      sendProgramEvent(ProgramListener.Event.LOADED);
      notifyProgramChanged();
      hasUnsavedChanges = false;
    }
  }

  public synchronized void validate() {
    ProgramValidationContext context = new ProgramValidationContext(this);
    for (Map.Entry<String, GlobalSymbol> entry : new TreeMap<>(symbolMap).entrySet()) {
      context.startChain(entry.getKey());
      entry.getValue().validate(context);
    }
    mainSymbol.validate(context);
  }

  public synchronized void addBuiltin(String name, Object value) {
    mainModule.addBuiltin(name, value);
  }


  public synchronized void setDeclaration(String name, Declaration declaration) {
    //mainModule.setStaticValue(name, declaration);

    GlobalSymbol symbol = getSymbol(name);
    if (symbol == null) {
      symbol = new GlobalSymbol(this, name, GlobalSymbol.Scope.PERSISTENT, declaration);
      symbolMap.put(name, symbol);
    } else {
      if (symbol.getScope() == GlobalSymbol.Scope.BUILTIN) {
        throw new RuntimeException("Can't overwrite builtin '" + name + "'");
      }
      symbol.value = declaration;
    }
    symbol.setConstant(true);
    declaration.setDeclaringSymbol(symbol);

    notifySymbolChanged(symbol);
  }

  public synchronized void setPersistentInitializer(String name, DeclarationStatement expr) {
    GlobalSymbol symbol = getSymbol(name);
    if (symbol == null || symbol.scope == GlobalSymbol.Scope.TRANSIENT) {
      symbol = new GlobalSymbol(this, name, GlobalSymbol.Scope.PERSISTENT, null);
      symbolMap.put(name, symbol);
    } else if (symbol.getScope() == GlobalSymbol.Scope.BUILTIN) {
      throw new RuntimeException("Can't overwrite builtin '" + name + "'");
    }
    symbol.initializer = expr;
    symbol.setConstant(expr.kind == DeclarationStatement.Kind.CONST);
    notifyProgramChanged();
  }

  /*
  public void setLine(StaticSymbol symbol, CodeLine codeLine) {
    if (symbol.getValue() instanceof FunctionImplementation) {
      FunctionImplementation functionImplementation = (FunctionImplementation) symbol.getValue();
      functionImplementation.setLine(codeLine);
      notifySymbolChanged(symbol);
    }
  }*/

  public void deleteLine(StaticSymbol symbol, int line) {
    if (symbol.getStaticValue() instanceof UserFunction) {
      UserFunction userFunction = (UserFunction) symbol.getStaticValue();
      userFunction.deleteLine(line);
    }
  }


  public void addProgramNameChangeListener(ProgramListener listener) {
    programListeners.add(listener);
  }

  public void addSymbolChangeListener(SymbolChangeListener programChangeListener) {
    programChangeListeners.add(programChangeListener);
  }

  public void sendProgramEvent(ProgramListener.Event event) {
    if (event == ProgramListener.Event.CHANGED) {
      deferNotification(null);
    } else {
      for (ProgramListener programListener : programListeners) {
        programListener.programEvent(event);
      }
    }
  }

  synchronized void deferNotification(StaticSymbol symbol) {
    if (loading) {
      return;
    }
    hasUnsavedChanges = true;
    if (notificationPending) {
      if (symbol != notificationPendingForSymbol) {
        notificationPendingForSymbol = null;
      }
    } else {
      notificationPending = true;
      notificationPendingForSymbol = symbol;
      notificationTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          synchronized (Program.this) {
            notificationPending = false;
            if (loading) {
              return;
            }
            if (notificationPendingForSymbol != null) {
              notificationPendingForSymbol.validate();
              for (SymbolChangeListener changeListener : programChangeListeners) {
                changeListener.symbolChangedByUser(notificationPendingForSymbol);
              }
              notificationPendingForSymbol = null;
            } else {
              validate();
              for (ProgramListener programListener : programListeners) {
                programListener.programEvent(ProgramListener.Event.CHANGED);
              }
            }
          }
        }
      }, 200);
    }
  }


  public void notifySymbolChanged(StaticSymbol symbol) {
    deferNotification(symbol);
  }

  public void notifyProgramChanged() {
    deferNotification(null);
  }

  /**
   * Used in refactoring to re-add a symbol that was removed for renaming
   */
  @Override
  public void addSymbol(StaticSymbol symbol) {
    symbolMap.put(symbol.getName(), (GlobalSymbol) symbol);
//    notifyProgramChanged();  // crashes refactoring....
  }


  public boolean isEmpty() {
    if (main.getLineCount() > 0) {
      return false;
    }
    for (StaticSymbol symbol : symbolMap.values()) {
      if (symbol.getScope() == GlobalSymbol.Scope.PERSISTENT) {
        return false;
      }
    }
    return true;
  }
}
