package org.kobjects.asde.shell;

import org.kobjects.annotatedtext.AnnotatedString;
import org.kobjects.annotatedtext.Span;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.exceptions.WrappedExecutionException;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;
import org.kobjects.asde.lang.io.Shell;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.expressionparser.ParsingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsdeShell implements Console {

  public static void main(String[] args) throws IOException {
    new Thread(() -> {
      try {
        ServerSocket serverSocket = new ServerSocket(2323);
        while (true) {
          Socket socket = serverSocket.accept();
          new Thread(() -> {
            try {
              Reader reader = new InputStreamReader(socket.getInputStream(), "utf-8");
              Writer writer = new OutputStreamWriter(socket.getOutputStream(), "utf-8");
              new AsdeShell(reader, writer).run();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }).start();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    final Reader reader = new InputStreamReader(System.in, "utf-8");
    final Writer writer = new OutputStreamWriter(System.out, "utf-8");

    new AsdeShell(reader, writer).run();
  }



  private final BufferedReader reader;
  private final PrintWriter writer;
  private final Program program;
  private final Shell shell;

  private UserFunction selectedFunction;
  private Property selectedProperty;

  AsdeShell(Reader reader, Writer writer) {
    this.reader = new BufferedReader(reader);
    this.writer = new PrintWriter(writer, true);
    program = new Program(this);
    shell = new Shell(program);
  }

  public void run() throws IOException {
    boolean prompt = true;
    while (true) {
      if (prompt) {
        writer.println();
        writer.print("> ");
        writer.flush();
      }
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      try {
        prompt = false;
        shell.enter(line);
      } catch (ParsingException e) {
        prompt = true;
        char[] fill = new char[e.start + 1];
        Arrays.fill(fill, ' ');
        writer.println(new String(fill) + '^');
        writer.println("?SYNTAX ERROR: " + e.getMessage());
        program.lastException = e;
      } catch (WrappedExecutionException e) {
        prompt = true;
        e.printStackTrace(writer);
        writer.println("\nERROR in " + e.lineNumber + ": " + e.getMessage());
        writer.println("\nREADY.");
        program.lastException = e;
      } catch (Exception e) {
        prompt = true;
        e.printStackTrace(writer);
        writer.println("\nERROR: " + e);
        writer.println("\nREADY.");
        program.lastException = e;
      }
    }
  }


  @Override
  public void prompt() {
      writer.println();
      writer.print("> ");
      writer.flush();

  }

  @Override
  public void print(CharSequence s) {
    if (s instanceof AnnotatedString) {
      print((AnnotatedString) s);
    }
    writer.print(s);
  }

  static <K, V> void addToListInMap(Map<K, List<V>> map, K key, V value) {
    List<V> list = map.get(key);
    if (list == null) {
      list = new ArrayList<>();
      map.put(key, list);
    }
    list.add(value);
  }

  public void print(AnnotatedString s) {
    Map<Integer, List<Span>> spanMap = new HashMap<>();
    for (Span span : s.spans()) {
      addToListInMap(spanMap, span.start, span);
      addToListInMap(spanMap, span.end, span);
    }

    for (int i = 0; i < s.length(); i++) {
      List<Span> spanList = spanMap.get(i);
      if (spanList != null) {
        for (Span span : spanList) {
          if (span.start == i) {
            if (span.annotation == SyntaxColor.HIDE) {
              writer.write("\033[0;37m");
            } else if (span.annotation == SyntaxColor.KEYWORD) {
              writer.write("\033[0;33m");
            } else if (span.annotation == SyntaxColor.STRING) {
              writer.write("\033[0;32m");
            }
          }
          if (span.end == i) {
            writer.write("\033[0;39m");
          }
        }
      }
      writer.write(s.charAt(i));
    }
  }

    @Override
    public String input() {
      try {
        return reader.readLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void clearScreen(ClearScreenType clearScreenType) {

    }

    @Override
    public void highlight(UserFunction function, int lineNumber) {
      // TBD
    }

    @Override
    public InputStream openInputStream(String url) {
      throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream openOutputStream(String url) {
      throw new UnsupportedOperationException();
    }


    @Override
    public ProgramReference nameToReference(String name) {
      return new ProgramReference(name, name, true);
    }

    @Override
    public void startProgress(String title) {

    }

    @Override
    public void updateProgress(String update) {

    }

    @Override
    public void endProgress() {

    }

    @Override
    public void delete(int line) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void edit(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void selectProperty(Property property) {

    this.selectedProperty = property;
      if (!property.isInstanceField() && property.getStaticValue() instanceof UserFunction) {
        selectedFunction = ((UserFunction) property.getStaticValue());
      }
    }

    @Override
    public Property getSelectedProperty() {
      return selectedProperty;
    }

  @Override
  public UserFunction getSelectedFunction() {
    return selectedFunction == null ? program.getMain() : selectedFunction;
  }

  @Override
    public void showError(String message, Exception e) {
      print(message + e);
    }


}
