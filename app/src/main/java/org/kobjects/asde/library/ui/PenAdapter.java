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


    PenAdapter(Pen pen) {
        super(CLASSIFIER);
        this.pen = pen;
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((PenPropertyDescriptor) property) {
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
        }
        throw new RuntimeException();
    }



    enum PenPropertyDescriptor implements PropertyDescriptor {
        drawrect(new FunctionType(Types.VOID, Types.NUMBER, Types.NUMBER, Types.NUMBER, Types.NUMBER))
        ;

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