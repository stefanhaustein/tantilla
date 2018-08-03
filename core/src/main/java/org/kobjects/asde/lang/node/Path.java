package org.kobjects.asde.lang.node;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.PropertyDescriptor;


public class Path extends AssignableNode {
    String pathName;
    public Path(Node left, Node right) {
        super(left);
        if (!(right instanceof Variable)) {
            throw new RuntimeException("Path name expected");
        }
        pathName = ((Variable) right).name;
    }

    Property evalProperty(Interpreter interpreter) {
        Object base = children[0].eval(interpreter);
        if (!(base instanceof Instance)) {
            throw new RuntimeException("instance expected");
        }
        Instance instance = (Instance) base;
        PropertyDescriptor propertyDescriptor = instance.getClassifier().getPropertyDescriptor(pathName);
        return instance.getProperty(propertyDescriptor);
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return evalProperty(interpreter).get();
    }

    @Override
    public Object returnType() {
        return null;
    }

    @Override
    public String toString() {
        return children[0] + "." + pathName;
    }

    @Override
    public void set(Interpreter interpreter, Object value) {
        evalProperty(interpreter).set(value);
    }
}
