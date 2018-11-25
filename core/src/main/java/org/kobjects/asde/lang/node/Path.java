package org.kobjects.asde.lang.node;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.parser.ResolutionContext;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

import java.util.Map;


public class Path extends AssignableNode {
    String pathName;
    public Path(Node left, Node right) {
        super(left);
        if (!(right instanceof Identifier)) {
            throw new RuntimeException("Path name expected");
        }
        pathName = ((Identifier) right).name;
    }

    Property evalProperty(Interpreter interpreter) {
        Object base = children[0].eval(interpreter);
        if (!(base instanceof Instance)) {
            throw new RuntimeException("instance expected");
        }
        Instance instance = (Instance) base;
        PropertyDescriptor propertyDescriptor = instance.getType().getPropertyDescriptor(pathName);
        if (propertyDescriptor == null) {
            throw new RuntimeException("Property '" + pathName + "' does not exist.");
        }
        return instance.getProperty(propertyDescriptor);
    }

    @Override
    protected void onResolve(ResolutionContext resolutionContext) {
        // TODO: Implement.
    }

    @Override
    public Object eval(Interpreter interpreter) {
        return evalProperty(interpreter).get();
    }

    @Override
    public Type returnType() {
        return null;
    }

    @Override
    public void toString(AnnotatedStringBuilder asb, Map<Node, Exception> errors) {
        children[0].toString(asb, errors);
        asb.append(".");
        appendLinked(asb, pathName, errors);
    }

    @Override
    public void set(Interpreter interpreter, Object value) {
        evalProperty(interpreter).set(value);
    }
}
