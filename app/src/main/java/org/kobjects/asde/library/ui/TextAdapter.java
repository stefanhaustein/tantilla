package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.Types;
import org.kobjects.graphics.Text;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class TextAdapter extends Instance {
    private final Text view;

    final NumberProperty x = new NumberProperty(TextMetaProperty.x);
    final NumberProperty y = new NumberProperty(TextMetaProperty.y);
    final NumberProperty z = new NumberProperty(TextMetaProperty.z);
    final NumberProperty size = new NumberProperty(TextMetaProperty.size);
    final StringProperty text = new StringProperty(TextMetaProperty.text);

    public TextAdapter(Classifier classifier, final ScreenAdapter screen) {
        super(classifier);
        view = new Text(screen.getViewport());
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((TextMetaProperty) property) {
            case x: return x;
            case y: return y;
            case size: return size;
            case text: return text;
        }
        throw new IllegalArgumentException();
    }


    private class NumberProperty extends Property<Double> {
        private final TextMetaProperty target;

        NumberProperty(TextMetaProperty target) {
            this.target = target;
        }

        @Override
        public Double get() {
            switch (target) {
                case x:
                    return Double.valueOf(view.getX());
                case y:
                    return Double.valueOf(view.getY());
                case z:
                    return Double.valueOf(view.getZ());
                case size:
                    return Double.valueOf(view.getSize());
            }
            throw new RuntimeException();
        }

        @Override
        public boolean set(Double value) {
            switch (target) {
                case x:
                    return view.setX(((Double) value).floatValue());
                case y:
                    return view.setY(((Double) value).floatValue());
                case z:
                    return view.setZ(((Double) value).floatValue());
                case size:
                    return view.setSize(((Double) value).floatValue());
            }
            throw new RuntimeException();
        }

    }

    private class StringProperty extends Property<String> {
        private final TextMetaProperty target;

        StringProperty(TextMetaProperty target) {
            this.target = target;
        }

        public String get() {
            switch (target) {
                case text:
                    return view.getText();
            }
            throw new RuntimeException();
        }

        public boolean set(String value) {
            switch (target) {
                case text: return view.setText(value);
            }
            throw new RuntimeException();
        }
    }

    enum TextMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), z(Types.NUMBER), size(Types.NUMBER),
        text(Types.STRING);

        private final Type type;

        TextMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
