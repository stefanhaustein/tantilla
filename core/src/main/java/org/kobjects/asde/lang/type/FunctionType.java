package org.kobjects.asde.lang.type;

public class FunctionType implements Type {
    private final Type returnType;
    private Parameter[] parameters;

    public FunctionType(Type returnType, Parameter... paramters) {
        this.returnType = returnType;
        this.parameters = paramters;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Parameter getParameter(int index) {
        return parameters[index];
    }

    public int getParameterCount() {
        return parameters.length;
    }

}
