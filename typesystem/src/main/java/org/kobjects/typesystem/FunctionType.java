package org.kobjects.typesystem;

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

    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(parameters[i].name);
        }
        sb.append("): ");
        sb.append(returnType.toString());
        return sb.toString();
    }

}
