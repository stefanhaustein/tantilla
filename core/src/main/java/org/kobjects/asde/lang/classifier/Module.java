package org.kobjects.asde.lang.classifier;

import org.kobjects.asde.lang.function.Function;
import org.kobjects.asde.lang.program.Program;

import java.util.HashSet;

public class Module extends UserClass {
  HashSet<String> builtins = new HashSet<>();

  public Module(Program program) {
    super(program);
  }

  public void addBuiltin(String name, Object value) {
    builtins.add(name);
    setStaticValue(name, value);
  }
}
