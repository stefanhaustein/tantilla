package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.Interpreter;
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
        public boolean set(Double argb) {
            return pen.setFillColor((int) argb.longValue());
        }

        @Override
        public Double get() {
            return Double.valueOf(pen.getFillColor());
        }
    };

    private Property<Double> strokeColor = new Property<Double>() {
        @Override
        public boolean set(Double argb) {
            return pen.setStrokeColor((int) argb.longValue());
        }

        @Override
        public Double get() {
            return Double.valueOf(pen.getStrokeColor());
        }
    };


    PenAdapter(Pen pen) {
        super(CLASSIFIER);
        this.pen = pen;
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((PenPropertyDescriptor) property) {
            case fillcolor: return fillColor;
            case strokecolor: return strokeColor;
            case clearrect:
                return new Method((FunctionType) PenPropertyDescriptor.clearrect.type()) {
                    @Override
                    public Object eval(Interpreter interpreter, Object[] args) {
                        pen.clearRect(((Double) args[0]).floatValue(),
                                ((Double) args[1]).floatValue(),
                                ((Double) args[2]).floatValue(),
                                ((Double) args[3]).floatValue());
                        return null;
                    }
                };
            case drawrect:
                return new Method((FunctionType) PenPropertyDescriptor.drawrect.type()) {
                    @Override
                    public Object eval(Interpreter interpreter, Object[] args) {
                        pen.drawRect(((Double) args[0]).floatValue(),
                                ((Double) args[1]).floatValue(),
                                ((Double) args[2]).floatValue(),
                                ((Double) args[3]).floatValue());
                        return null;
                    }
                };
            case drawline:
                return new Method((FunctionType) PenPropertyDescriptor.drawrect.type()) {
                    @Override
                    public Object eval(Interpreter interpreter, Object[] args) {
                        pen.drawLine(((Double) args[0]).floatValue(),
                                ((Double) args[1]).floatValue(),
                                ((Double) args[2]).floatValue(),
                                ((Double) args[3]).floatValue());
                        return null;
                    }
                };
        }
        throw new RuntimeException();
    }



    enum PenPropertyDescriptor implements PropertyDescriptor {
        fillcolor(Types.NUMBER),
        strokecolor(Types.NUMBER),
        clearrect(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER)),
        drawrect(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER)),
        drawline(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER));

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
