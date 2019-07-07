package org.kobjects.asde.lang;

import org.kobjects.asde.InstanceImpl;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.statement.DeclarationStatement;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.InstantiableType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceType;
import org.kobjects.typesystem.MetaType;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.ArrayList;
import java.util.TreeMap;

public class ClassImplementation implements InstanceType, InstantiableType {

  String name;
  final TreeMap<String, ClassPropertyDescriptor> propertyMap = new TreeMap<>();
  ArrayList<Node> resolvedInitializers = new ArrayList<>();

  public ClassImplementation(String name) {
    this.name = name;
  }

  @Override
  public PropertyDescriptor getPropertyDescriptor(String name) {
    return propertyMap.get(name);
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public void setMethod(String functionName, FunctionImplementation currentFunction) {
    System.err.println("NYI: setMethod");
  }

  public void processDeclarations(CodeLine codeLine) {
    for (int i = 0; i < codeLine.length(); i++) {
      Node node = codeLine.get(i);
      if (node instanceof DeclarationStatement) {
        DeclarationStatement declaration = (DeclarationStatement) node;
        propertyMap.put(declaration.varName, new ClassPropertyDescriptor(declaration.varName, declaration.children[0]));
      } else {
        throw new RuntimeException("Unsupported declaration in class: " + node);
      }
    }
  }

  public void validate(ClassValidationContext classValidationContext) {
    resolvedInitializers.clear();
    for (ClassPropertyDescriptor propertyDescriptor : propertyMap.values()) {
      propertyDescriptor.validate(classValidationContext);
    }
  }

  @Override
  public Instance createInstance(EvaluationContext evaluationContext) {
    int fieldCount = resolvedInitializers.size();
    Property[] properties = new Property[fieldCount];
    for (int i = 0; i < fieldCount; i++) {
      properties[i] = new PhysicalProperty(resolvedInitializers.get(i).eval(evaluationContext));
    }
    return new InstanceImpl(this, properties);
  }

  public class ClassPropertyDescriptor implements PropertyDescriptor {
    String name;
    Node initializer;
    Type type;
    int index;

    ClassPropertyDescriptor(String name, Node initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    void validate(ClassValidationContext classValidationContext) {

      FunctionValidationContext context = new FunctionValidationContext(classValidationContext.programValidationContext, FunctionValidationContext.ResolutionMode.INTERACTIVE, null);
      initializer.resolve(context, 0, 0);
  //    this.errors = context.errors;

      type = initializer.returnType();
      index = resolvedInitializers.size();
      resolvedInitializers.add(initializer);

      //      this.dependencies = context.dependencies;


    }


    @Override
    public String name() {
      return name;
    }

    @Override
    public Type type() {
      return type;
    }

    public int getIndex() {
      return index;
    }
  }

}
