package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.classifier.Property;

public interface HasProperty {
  /**
   * Null if N/A
   */
  Property getResolvedProperty();
}
