package org.kobjects.asde.lang.function;


import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.statement.BlockStatement;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.type.Type;

import java.util.HashMap;
import java.util.HashSet;

public class PropertyValidationContext {

  public static PropertyValidationContext createRootContext(Program program) {
    return new PropertyValidationContext(program, null, null, null);
  }

  public HashMap<Node, Exception> errors = new HashMap<>();

  /** Will be null when validating symbols! */
  public final UserFunction userFunction;

  private Property property;

  private int localSymbolCount;
  private Block currentBlock;
  public HashSet<UserProperty> dependencies = new HashSet<>();
  private PropertyValidationContext parentContext;
  public Program program;

  private PropertyValidationContext(Program program, PropertyValidationContext parentContext, Property property, UserFunction userFunction) {
    this.program = program;
    this.parentContext = parentContext;
    this.property = property;
    this.userFunction = userFunction;
    startBlock(null);
    if (userFunction != null) {
      for (int i = 0; i < userFunction.parameterNames.length; i++) {
        currentBlock.localSymbols.put(userFunction.parameterNames[i], new LocalSymbol(localSymbolCount++, userFunction.getType().getParameterType(i), false));
      }
    }
  }

  public static PropertyValidationContext createForFunction(UserFunction userFunction) {
    return new PropertyValidationContext(userFunction.program, null, null, userFunction);
  }

  public void startBlock(BlockStatement startStatement) {
    currentBlock = new Block(currentBlock, startStatement);
  }

  public BlockStatement endBlock() {
    BlockStatement startStatement = currentBlock.startStatement;
    currentBlock = currentBlock.parent;
    return startStatement;
  }


  public LocalSymbol declareLocalVariable(String name, Type type, boolean mutable) {
    if (currentBlock.localSymbols.containsKey(name)) {
      throw new RuntimeException("Local variable named '" + name + "' already exists");
    }
    LocalSymbol result = new LocalSymbol(localSymbolCount++, type, mutable);
    currentBlock.localSymbols.put(name, result);
    return result;
  }



  public void addError(Node node, Exception e) {
    errors.put(node, e);
  }

  public int getLocalVariableCount() {
    return localSymbolCount;
  }

  public Block getCurrentBlock() {
    return currentBlock;
  }

  public PropertyValidationContext createChildContext(Property property) {
    UserFunction userFunction = (!property.isMutable()
        && !property.isInstanceField() && property.getStaticValue() instanceof UserFunction) ?
        (UserFunction) property.getStaticValue() : null;
    return new PropertyValidationContext(this.program, this, property, userFunction);
  }

}
