package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.TreeMap;

public class AdapterType extends AbstractClassifier {
  public final ClassType classType;
  public final Trait trait;

  public AdapterType(ClassType classType, Trait trait) {
    super(classType.program);
    this.classType = classType;
    this.trait = trait;
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }


  @Override
  public void toString(AnnotatedStringBuilder asb) {
    asb.append("impl ");
    asb.append(classType.toString());
    asb.append(" as ");
    asb.append(trait.toString());
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public String toString() {
    return classType + " as " + trait;
  }
}
