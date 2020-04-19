package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Consumer;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.node.Node;

public class Classifiers {
  public static void list(AnnotatedStringBuilder asb, Iterable<? extends Property> properties, String indent) {
    for (Property property : properties) {
      asb.append(indent);
      if (!property.toString(asb)) {
        asb.append(": [\u2026]");
      }
      asb.append("\n");
    }
  }

  public static void processNodes(Classifier classifier, Consumer<Node> action) {
    for (Property symbol : classifier.getProperties()) {
      if (!symbol.isInstanceField()) {
        if (symbol.getInitializer() != null) {
          symbol.getInitializer().process(action);
        }
        if (symbol.getStaticValue() instanceof Classifier) {
          processNodes(((Classifier) symbol.getStaticValue()), action);
        }
        if (symbol.getStaticValue() instanceof UserFunction) {
          ((UserFunction) symbol.getStaticValue()).processNodes(action);
        }
      }
    }
  }
}
