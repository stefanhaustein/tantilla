package org.kobjects.asde.lang.program;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.classifier.module.Module;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.StaticProperty;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.function.BuiltinFunction;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.classifier.PropertyChangeListener;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.parser.ProgramParser;
import org.kobjects.asde.lang.expression.Node;
import org.kobjects.asde.lang.list.ListType;
import org.kobjects.asde.lang.function.Builtin;
import org.kobjects.asde.lang.type.Types;
import org.kobjects.asde.lang.parser.StatementParser;
import org.kobjects.asde.lang.function.FunctionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class Program {
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
  private Executor executor = Executors.newCachedThreadPool();

  public final StatementParser parser = new StatementParser(this);
  private final ArrayList<PropertyChangeListener> programChangeListeners = new ArrayList<>();
  private final ArrayList<ProgramListener> programListeners = new ArrayList<>();

  // Program state

  public Exception lastException;
  public int tabPos;
  public final Console console;
  private boolean loading;
  public boolean hasUnsavedChanges;

  private boolean notificationPending;
  private Property notificationPendingForSymbol;
  private Timer notificationTimer = new Timer();
  public Module mainModule = new Module(this);

  public Program(Console console) {
    this.console = console;
    this.reference = console.nameToReference(null);
    // Primitives can't be registered because of ambiguities with conversion functions!
    addBuiltin("List", new ListType(Types.VOID));
    synchronized (this) {
      addBuiltin("input", new BuiltinFunction((a, b) -> console.input(), "Reads a string as input from the user.", Types.STR));
    }
    for (Builtin builtin : Builtin.values()) {
      mainModule.addBuiltin(builtin.name().toLowerCase(), builtin);
    }
    mainModule.putProperty(StaticProperty.createMethod(mainModule, "main", new UserFunction(this, FunctionType.createFromTypes(Types.VOID))));
  }

  public void processNodes(Consumer<Node> action) {
    mainModule.processNodes(action);
    notifyProgramChanged();
  }

  public synchronized void deleteAll() {
    Module replacement = new Module(this);
    for (StaticProperty builtin : mainModule.builtins()) {
      replacement.addBuiltin(builtin);
    }
    mainModule = replacement;
    mainModule.putProperty(StaticProperty.createMethod(mainModule, "main", new UserFunction(this, FunctionType.createFromTypes(Types.VOID))));

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

    /*

    HashSet<GenericProperty> initialized = new HashSet<>();
    for (GenericProperty symbol : mainModule.getUserProperties()) {
      symbol.init(evaluationContext, initialized);
    }

     */
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



  @Override
  public String toString() {
    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
    mainModule.toString(asb, "", true, true);
    return asb.toString();
  }

  public void save(ProgramReference programReference) throws IOException {
    if (!programReference.urlWritable) {
      throw new IOException("Can't write to URL: " + programReference.url);
    }
    if (!programReference.equals(reference)) {
      reference = programReference;
      sendProgramEvent(ProgramListener.Event.RENAMED);
    }
    OutputStreamWriter writer = new OutputStreamWriter(console.openOutputStream(programReference.url), "utf8");
    String s = toString();
    System.out.println("Saving: " + s);
    writer.write(toString());
    writer.close();
    hasUnsavedChanges = false;
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

      // change notification triggers validation
      sendProgramEvent(ProgramListener.Event.LOADED);
      notifyProgramChanged();
      hasUnsavedChanges = false;
    }
  }

  public synchronized void validate() {
    ValidationContext.validateAll(this);
  }

  public synchronized void addBuiltin(String name, Object value) {
    mainModule.addBuiltin(name, value);
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

  public void addSymbolChangeListener(PropertyChangeListener programChangeListener) {
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
              ValidationContext.reValidate(Program.this, notificationPendingForSymbol);
              for (PropertyChangeListener changeListener : programChangeListeners) {
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
    if (getMain().getLineCount() > 0) {
      return false;
    }
    return mainModule.getProperties().isEmpty();
  }

  public UserFunction getMain() {
    return (UserFunction) mainModule.getProperty("main").getStaticValue();
  }

  public Executor getExecutor() {
    return executor;
  }
}
