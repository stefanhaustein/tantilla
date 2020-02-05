package org.kobjects.asde.android.library.ui;

import org.kobjects.asde.lang.runtime.EvaluationContext;
import org.kobjects.asde.lang.classifier.Method;
import org.kobjects.asde.lang.function.Types;
import org.kobjects.graphics.Pen;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceTypeImpl;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class PenAdapter extends Instance {

    public static final InstanceTypeImpl TYPE = new InstanceTypeImpl("Pen",
        "Object that provides methods for drawing on the screen. "
        +"Its properties influences the drawing style");

    static {
        TYPE.addProperties(PenPropertyDescriptor.values());
    }

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
        super(TYPE);
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
                    public Object call(EvaluationContext evaluationContext, int paramCount) {
                        pen.clearRect(
                                ((Number) evaluationContext.getParameter(0)).floatValue(),
                                ((Number) evaluationContext.getParameter(1)).floatValue(),
                                ((Number) evaluationContext.getParameter(2)).floatValue(),
                                ((Number) evaluationContext.getParameter(3)).floatValue());
                        return null;
                    }
                };
            case rect:
                return new Method((FunctionType) PenPropertyDescriptor.rect.type()) {
                    @Override
                    public Object call(EvaluationContext evaluationContext, int paramCount) {
                        pen.drawRect(
                                ((Number) evaluationContext.getParameter(0)).floatValue(),
                                ((Number) evaluationContext.getParameter(1)).floatValue(),
                                ((Number) evaluationContext.getParameter(2)).floatValue(),
                                ((Number) evaluationContext.getParameter(3)).floatValue());
                        return null;
                    }
                };
            case write:
                return new Method((FunctionType) PenPropertyDescriptor.write.type()) {
                    @Override
                    public Object call(EvaluationContext evaluationContext, int paramCount) {
                        pen.drawText(
                                ((Number) evaluationContext.getParameter(0)).floatValue(),
                                ((Number) evaluationContext.getParameter(1)).floatValue(),
                                ((String) evaluationContext.getParameter(2)));
                        return null;
                    }
                };
            case line:
                return new Method((FunctionType) PenPropertyDescriptor.line.type()) {
                    @Override
                    public Object call(EvaluationContext evaluationContext, int paramCount) {
                        pen.drawLine(
                                ((Number) evaluationContext.getParameter(0)).floatValue(),
                                ((Number) evaluationContext.getParameter(1)).floatValue(),
                                ((Number) evaluationContext.getParameter(2)).floatValue(),
                                ((Number) evaluationContext.getParameter(3)).floatValue());
                        return null;
                    }
                };
        }
        throw new RuntimeException();
    }



    enum PenPropertyDescriptor implements PropertyDescriptor {
        fillColor(Types.FLOAT),
        strokeColor(Types.FLOAT),
        textSize(Types.FLOAT),
        clear(new FunctionType(Types.VOID, Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT)),
        rect(new FunctionType(Types.VOID, Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT)),
        write(new FunctionType(Types.VOID, Types.FLOAT, Types.FLOAT, Types.STR)),
        line(new FunctionType(Types.VOID, Types.FLOAT, Types.FLOAT, Types.FLOAT, Types.FLOAT));

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
