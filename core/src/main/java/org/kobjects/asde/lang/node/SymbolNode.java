package org.kobjects.asde.lang.node;


import org.kobjects.asde.lang.classifier.UserProperty;

/**
 * Used for signature changes. Not needed for rename!
 */
public abstract class SymbolNode extends AssignableNode {

  SymbolNode(Node... children) {
    super(children);
  }

  public abstract boolean matches(UserProperty symbol, String name);


}
