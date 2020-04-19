package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.Collection;
import java.util.TreeMap;

public class Trait extends AbstractClassifier implements DeclaredBy {
  Property declaringSymbol;

  public Trait(Program program) {
    super(program);
  }

  @Override
  public void setDeclaredBy(Property declaringSymbol) {
    this.declaringSymbol = declaringSymbol;
  }

  @Override
  public Property getDeclaringSymbol() {
    return declaringSymbol;
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putProperty(Property property) {
    if (!(property instanceof TraitProperty)) {
      throw new RuntimeException("TraitProperty required");
    }
    super.putProperty(property);
  }

  @Override
  public String toString() {
    return getDeclaringSymbol() != null ? getDeclaringSymbol().getName() : super.toString();
  }

  @Override
  public void toString(AnnotatedStringBuilder asb) {
    asb.append("trait", SyntaxColor.KEYWORD);
    if (declaringSymbol != null) {
      asb.append(' ');
      asb.append(declaringSymbol.getName());
    }
  }
}
