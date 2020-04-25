package org.kobjects.asde.lang.classifier.trait;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.AbstractClassifier;
import org.kobjects.asde.lang.classifier.DeclaredBy;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.clazz.ClassType;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

public class AdapterType extends AbstractClassifier implements DeclaredBy {
  public final ClassType classType;
  public final Trait trait;
  private Property declaringSymbol;

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
  public void toString(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean forExport) {
    asb.append(indent);
    asb.append("impl ");
    asb.append(classType.toString());
    asb.append(" as ");
    asb.append(trait.toString());
    if (includeContent) {
      asb.append(":\n");
      listProperties(asb, indent + " ", includeContent, forExport);
    }
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

  @Override
  public void setDeclaredBy(Property declaringSymbol) {
    this.declaringSymbol = declaringSymbol;
  }

  @Override
  public Property getDeclaringSymbol() {
    return declaringSymbol;
  }
}
