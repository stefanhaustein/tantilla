package org.kobjects.asde.lang.program;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.Module;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.BuiltinFunction;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.classifier.UserPropertyChangeListener;
import org.kobjects.asde.lang.io.ProgramReference;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class Program {

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
  private final ArrayList<UserPropertyChangeListener> programChangeListeners = new ArrayList<>();
  private final ArrayList<ProgramListener> programListeners = new ArrayList<>();

  // Program state

  public Exception lastException;
  public int tabPos;
  public final Console console;
  private boolean loading;
  public int currentStamp;
  public boolean hasUnsavedChanges;

  private boolean notificationPending;
  private Property notificationPendingForSymbol;
  private Timer notificationTimer = new Timer();
  public Module mainModule = new Module(this);

  public Program(Console console) {
    this.console = console;
    this.reference = console.nameToReference(null);
    // Primitives can't be registered because of ambiguities with conversion functions!
    addBuiltin("List", new ListType(Types.VOID));
    synchronized (this) {
      addBuiltin("input", new BuiltinFunction((a, b) -> console.input(), "Reads a string as input from the user.", Types.STR));
    }
    for (Builtin builtin : Builtin.values()) {
      mainModule.addBuiltin(builtin.name().toLowerCase(), builtin);
    }
    mainModule.addBuiltin("main", main);
  }

  public void processNodes(Consumer<Node> action) {
    mainModule.processNodes(action);
    notifyProgramChanged();
  }

  public synchronized void deleteAll() {
    main.clear();

    Module replacement = new Module(this);
    for (Map.Entry<String, Object> builtin : mainModule.builtins().entrySet()) {
      replacement.addBuiltin(builtin.getKey(), builtin.getValue());
    }
    mainModule = replacement;

    ProgramReference newReference = console.nameToReference(null);
    if (!reference.equals(newReference)) {
      reference = newReference;
      sendProgramEvent(ProgramListener.Event.LOADED);
    }
    notifyProgramChanged();
  }


  public synchronized void clear(EvaluationContext evaluationContext) {
    console.clearScreen(Console.ClearScreenType.CLEAR_STATEMENT);



    /* TreeMap<String, GlobalSymbol> cleared = new TreeMap<String, GlobalSymbol>();
    for (Map.Entry<String, GlobalSymbol> entry : symbolMap.entrySet()) {
      GlobalSymbol symbol = entry.getValue();
      if (symbol != null
          && (symbol.scope != GlobalSymbol.Scope.TRANSIENT || symbol.stamp == currentStamp)) {
        cleared.put(entry.getKey(), symbol);
      }
    }
    symbolMap = cleared; */

    HashSet<GenericProperty> initialized = new HashSet<>();


    for (GenericProperty symbol : mainModule.getUserProperties()) {
      symbol.init(evaluationContext, initialized);
    }
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

  public synchronized void toString(AnnotatedStringBuilder sb) {
    for (GenericProperty symbol : mainModule.getUserProperties()) {
      if (!(symbol.getStaticValue() instanceof UserFunction)) {
        sb.append(symbol.getStaticValue().toString()).append('\n');
      }
    }

    for (GenericProperty symbol : mainModule.getUserProperties()) {
      if (symbol.getStaticValue() instanceof UserFunction) {
        ((UserFunction) symbol.getStaticValue()).toString(sb, symbol.getName(), symbol.getErrors());
      }
    }
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
    ValidationContext validationContext = ValidationContext.createRootContext(this);
    mainModule.validate(validationContext);
  }

  public synchronized void addBuiltin(String name, Object value) {
    mainModule.addBuiltin(name, value);
  }


  public synchronized void setDeclaration(String name, Object staticValue) {
    mainModule.putProperty(GenericProperty.createStatic(mainModule, name, staticValue));
  }

  public synchronized void setPersistentInitializer(String name, DeclarationStatement expr) {
    mainModule.putProperty(GenericProperty.createWithInitializer(
        mainModule,
        false,
        expr.kind == DeclarationStatement.Kind.MUT,
        name,
        expr.children[0]));
  }


  public void deleteLine(Property symbol, int line) {
    if (symbol.getStaticValue() instanceof UserFunction) {
      UserFunction userFunction = (UserFunction) symbol.getStaticValue();
      userFunction.deleteLine(line);
    }
  }


  public void addProgramNameChangeListener(ProgramListener listener) {
    programListeners.add(listener);
  }

  public void addSymbolChangeListener(UserPropertyChangeListener programChangeListener) {
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

  synchronized void deferNotification(Property symbol) {
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
              ValidationContext.createRootContext(Program.this).validateProperty(notificationPendingForSymbol);
              for (UserPropertyChangeListener changeListener : programChangeListeners) {
                changeListener.propertyDefinitionChanged(notificationPendingForSymbol);
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


  public void notifySymbolChanged(Property property) {
    deferNotification(property);
  }

  public void notifyProgramChanged() {
    deferNotification(null);
  }


  public boolean isEmpty() {
    if (main.getLineCount() > 0) {
      return false;
    }
    return mainModule.getUserProperties().isEmpty();
  }
}
