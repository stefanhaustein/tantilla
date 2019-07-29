package org.kobjects.asde.lang;

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
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ClassImplementation implements InstanceType, InstantiableType, Declaration, SymbolOwner {

  final Program program;
  public final TreeMap<String, ClassPropertyDescriptor> propertyMap = new TreeMap<>();
  ArrayList<Node> resolvedInitializers = new ArrayList<>();
  GlobalSymbol declaringSymbol;

  public ClassImplementation(Program program) {
    this.program = program;
  }


  @Override
  public ClassPropertyDescriptor getPropertyDescriptor(String name) {
    return propertyMap.get(name);
  }

  @Override
  public Type getType() {
    return new MetaType(this);
  }

  public void setMethod(String functionName, FunctionImplementation methodImplementation) {
    propertyMap.put(functionName, new ClassPropertyDescriptor(functionName, methodImplementation));
  }

  public void setProperty(String propertyName, Node initializer) {
    propertyMap.put(propertyName, new ClassPropertyDescriptor(propertyName, initializer));
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

  @Override
  public void setDeclaringSymbol(StaticSymbol declaringSymbol) {
    this.declaringSymbol = (GlobalSymbol) declaringSymbol;
  }

  @Override
  public StaticSymbol getSymbol(String name) {
    return propertyMap.get(name);
  }

  @Override
  public void removeSymbol(StaticSymbol symbol) {
    propertyMap.remove(symbol.getName());
  }

  @Override
  public void addSymbol(StaticSymbol symbol) {
    propertyMap.put(symbol.getName(), (ClassPropertyDescriptor) symbol);
  }

  public class ClassPropertyDescriptor implements PropertyDescriptor, ResolvedSymbol, StaticSymbol {
    String name;
    Node initializer;
    FunctionImplementation methodImplementation;
    int index = -1;

    ClassPropertyDescriptor(String name, Node initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    ClassPropertyDescriptor(String name, FunctionImplementation methodImplementation) {
      this.name = name;
      this.methodImplementation = methodImplementation;
      methodImplementation.setDeclaringSymbol(this);
    }

    void validate(ClassValidationContext classValidationContext) {
      if (classValidationContext.validated.contains(this)) {
        return;
      }

      FunctionValidationContext context = new FunctionValidationContext(classValidationContext, methodImplementation);

      if (methodImplementation != null) {
        methodImplementation.validate(context);

      } else {
        initializer.resolve(context, 0, 0);
        //    this.errors = context.errors;

        index = resolvedInitializers.size();
        resolvedInitializers.add(initializer);
      }

      if (context.errors.size() > 0) {
        System.err.println("Validation errors for property " + name + ": " + context.errors);
      }

      classValidationContext.errors.putAll(context.errors);

      classValidationContext.validated.add(this);

      // We don't need to track dependencies (as in GlobalSymbol.validate) as they are ordered in resolvedInitializers.
    }


    @Override
    public String name() {
      return name;
    }

    @Override
    public Type type() {
      return methodImplementation != null ? methodImplementation.type : initializer.returnType();
    }

    public int getIndex() {
      return index;
    }

    @Override
    public Object get(EvaluationContext evaluationContext) {
      return evaluationContext.self.getProperty(this).get();
    }

    @Override
    public void set(EvaluationContext evaluationContext, Object value) {
      evaluationContext.self.getProperty(this).set(value);
    }

    @Override
    public ClassImplementation getOwner() {
      return ClassImplementation.this;
    }

    @Override
    public Map<Node, Exception> getErrors() {
      return Collections.emptyMap();
    }

    @Override
    public Object getValue() {
      return methodImplementation;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Type getType() {
      return type();
    }

    @Override
    public Node getInitializer() {
      return initializer;
    }

    @Override
    public void validate() {
      System.err.println("ClassImplementation.ClassPropertyDescriptor.validate() NYI");
    }

    @Override
    public GlobalSymbol.Scope getScope() {
      return GlobalSymbol.Scope.PERSISTENT;
    }

    @Override
    public boolean isConstant() {
      return initializer == null;
    }

    @Override
    public void setName(String newName) {
      name = newName;
    }

    public void setInitializer(Node initializer) {
      this.initializer = initializer;
    }
  }

}
