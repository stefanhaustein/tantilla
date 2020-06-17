package org.kobjects.asde.lang.classifier.trait;

import org.kobjects.markdown.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.AbstractClassifier;
import org.kobjects.asde.lang.classifier.DeclaredBy;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.program.Program;

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
  public void toString(AnnotatedStringBuilder asb, String indent, boolean listContent, boolean forExport) {
    asb.append("trait", SyntaxColor.KEYWORD);
    asb.append(' ');
    asb.append(toString());
    if (forExport) {
      asb.append("\n");
     listProperties(asb, indent + " ", listContent, forExport);
    }
  }
}
