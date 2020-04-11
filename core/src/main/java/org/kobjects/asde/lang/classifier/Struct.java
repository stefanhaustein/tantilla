package org.kobjects.asde.lang.classifier;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.Parameter;
import org.kobjects.asde.lang.function.ValidationContext;
import org.kobjects.asde.lang.io.SyntaxColor;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.program.Program;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

public class Struct implements Classifier, InstantiableType, DeclaredBy {

  final Program program;
  public final TreeMap<String, Property> propertyMap = new TreeMap<>();

  // Theoretically, this could be handled by turning the meta-class into a function type.

  Property declaringSymbol;

  public Struct(Program program) {
    this.program = program;
  }

  public Collection<GenericProperty> getUserProperties() {
    ArrayList<GenericProperty> userProperties = new ArrayList<>();
    for (Property property : propertyMap.values()) {
      if (property instanceof GenericProperty) {
        userProperties.add((GenericProperty) property);
      }
    }
    return userProperties;
  }

  @Override
  public Property getProperty(String name) {
    return propertyMap.get(name);
  }


  @Override
  public Collection<? extends Property> getAllProperties() {
    return propertyMap.values();
  }

  @Override
  public void putProperty(Property property) {
    propertyMap.put(property.getName(), property);
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
  public void toString(AnnotatedStringBuilder asb) {
    asb.append("class", SyntaxColor.KEYWORD);
    if (declaringSymbol != null) {
      asb.append(' ');
      asb.append(declaringSymbol.getName());
    }
  }

  @Override
  public Instance createInstance(EvaluationContext evaluationContext, Object... ctorValues) {
    System.out.println("****** Create Instance of " + toString() + " Values: " + Arrays.toString(ctorValues));
    return new Instance(this, ctorValues);
  }

  @Override
  public FunctionType getConstructorSignature(ValidationContext validationContext) {
    // Ideally, this would be cached. Might make sense to have a hidden property for the constructor.
    ArrayList<Parameter> parameters = new ArrayList<>();
    for (Property property : propertyMap.values()) {
      if (property.isInstanceField()) {
        validationContext.validateProperty(property);
        ((GenericProperty) property).fieldIndex = parameters.size();
        parameters.add(property.getInitializer() == null
              ? Parameter.create(property.getName(), property.getType())
              : Parameter.create(property.getName(), property.getInitializer()));
      } else if (property.getInitializer() == null && property.getType() instanceof FunctionType) {
        // We need to initialize all methods, too -- as they can be called via traits.
        FunctionType functionType = (FunctionType) property.getType();
        if (functionType.getParameterCount() > 0 && functionType.getParameter(0).getName().equals("self")) {
          validationContext.validateProperty(property);
        }
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

  public void remove(String name) {
    propertyMap.remove(name);
  }

}
