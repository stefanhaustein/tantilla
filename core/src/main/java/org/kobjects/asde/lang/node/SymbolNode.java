package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Property;

/**
 * Used for signature changes. Not needed for rename!
 */
public abstract class SymbolNode extends AssignableNode implements HasProperty {

  SymbolNode(Node... children) {
    super(children);
  }


}
