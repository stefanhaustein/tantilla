package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.LocalStack;
import org.kobjects.asde.lang.Method;
import org.kobjects.asde.lang.Types;
import org.kobjects.graphics.Pen;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class PenAdapter extends Instance {

    static final Classifier CLASSIFIER = new Classifier(PenAdapter.PenPropertyDescriptor.values()) {
        @Override
        public PenAdapter createInstance() {
            throw new RuntimeException("Use screen.createPen()");
        }
    };

    private final Pen pen;

    private Property<Double> fillColor = new Property<Double>() {
        @Override
        public boolean setImpl(Double argb) {
            return pen.setFillColor((int) argb.longValue());
        }

        @Override
        public Double get() {
            return Double.valueOf(pen.getFillColor());
        }
    };

    private Property<Double> strokeColor = new Property<Double>() {
        @Override
        public boolean setImpl(Double argb) {
            return pen.setLineColor((int) argb.longValue());
        }

        @Override
        public Double get() {
            return Double.valueOf(pen.getLineColor());
        }
    };

    private Property<Double> textSize = new Property<Double>() {
        @Override
        public boolean setImpl(Double aDouble) {
            return pen.setTextSize(aDouble.floatValue());
        }

        @Override
        public Double get() {
            return Double.valueOf(pen.getTextSize());
        }
    };


    PenAdapter(Pen pen) {
        super(CLASSIFIER);
        this.pen = pen;
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((PenPropertyDescriptor) property) {
            case fillColor: return fillColor;
            case strokeColor: return strokeColor;
            case textSize: return textSize;
            case clear:
                return new Method((FunctionType) PenPropertyDescriptor.clear.type()) {
                    @Override
                    public Object call(Interpreter interpreter, int paramCount) {
                        LocalStack localStack = interpreter.localStack;
                        pen.clearRect(
                                ((Number) localStack.getParameter(0, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(1, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(2, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(3, paramCount)).floatValue());
                        return null;
                    }
                };
            case rect:
                return new Method((FunctionType) PenPropertyDescriptor.rect.type()) {
                    @Override
                    public Object call(Interpreter interpreter, int paramCount) {
                        LocalStack localStack = interpreter.localStack;
                        pen.drawRect(
                                ((Number) localStack.getParameter(0, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(1, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(2, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(3, paramCount)).floatValue());
                        return null;
                    }
                };
            case write:
                return new Method((FunctionType) PenPropertyDescriptor.write.type()) {
                    @Override
                    public Object call(Interpreter interpreter, int paramCount) {
                        LocalStack localStack = interpreter.localStack;
                        pen.drawText(
                                ((Number) localStack.getParameter(0, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(1, paramCount)).floatValue(),
                                ((String) localStack.getParameter(2, paramCount)));
                        return null;
                    }
                };
            case line:
                return new Method((FunctionType) PenPropertyDescriptor.line.type()) {
                    @Override
                    public Object call(Interpreter interpreter, int paramCount) {
                        LocalStack localStack = interpreter.localStack;
                        pen.drawLine(
                                ((Number) localStack.getParameter(0, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(1, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(2, paramCount)).floatValue(),
                                ((Number) localStack.getParameter(3, paramCount)).floatValue());
                        return null;
                    }
                };
        }
        throw new RuntimeException();
    }



    enum PenPropertyDescriptor implements PropertyDescriptor {
        fillColor(Types.NUMBER),
        strokeColor(Types.NUMBER),
        textSize(Types.NUMBER),
        clear(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER)),
        rect(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER)),
        write(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.STRING)),
        line(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER));

        private final Type type;

        PenPropertyDescriptor(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
