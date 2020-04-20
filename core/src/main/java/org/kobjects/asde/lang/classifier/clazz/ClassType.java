package org.kobjects.asde.lang.classifier.clazz;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.classifier.AbstractClassifier;
import org.kobjects.asde.lang.classifier.DeclaredBy;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.Parameter;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Arrays;

public class ClassType extends AbstractClassifier implements InstantiableClassType, DeclaredBy {

  // Theoretically, this could be handled by turning the meta-class into a function type.

  Property declaringSymbol;

  public ClassType(Program program) {
    super(program);
  }

  @Override
  public CharSequence getDocumentation() {
    return null;
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, String indent, boolean includeContent, boolean forExport) {
    asb.append(indent);
    asb.append("class", SyntaxColor.KEYWORD);
    if (declaringSymbol != null) {
      asb.append(' ');
      asb.append(declaringSymbol.getName());
    }
    asb.append(':');
    if (includeContent) {
      listProperties(asb, indent + " ", includeContent, forExport);
    } else {
      asb.append("[...]");
    }
  }

  @Override
  public ClassInstance createInstance(EvaluationContext evaluationContext, Object... ctorValues) {
    System.out.println("****** Create Instance of " + toString() + " Values: " + Arrays.toString(ctorValues));
    return new ClassInstance(this, ctorValues);
  }


  @Override
  public FunctionType getConstructorSignature() {
    ArrayList<Parameter> parameters = new ArrayList<>();
    for (Property property : getProperties()) {
      if (property.isInstanceField()) {
        parameters.add(property.getInitializer() == null
            ? Parameter.create(property.getName(), property.getType())
            : Parameter.create(property.getName(), property.getInitializer()));
      }
    }
    return new FunctionType(this, parameters.toArray(Parameter.EMPTY_ARRAY));
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
  public boolean hasDefaultValue() {
    return false;
  }

  @Override
  public Object getDefaultValue() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return getDeclaringSymbol() != null ? getDeclaringSymbol().getName() : super.toString();
  }

}
