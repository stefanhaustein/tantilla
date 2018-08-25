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

    final SyncProperty<Double> x = new SyncProperty<>(TextMetaProperty.x, 0.0);
    final SyncProperty<Double> y = new SyncProperty<>(TextMetaProperty.y, 0.0);
    final SyncProperty<Double> size = new SyncProperty<>(TextMetaProperty.size, 10.0);
    final SyncProperty<String> text = new SyncProperty<>(TextMetaProperty.text, "");

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


    class SyncProperty<T> extends PhysicalProperty<T> {
        private final TextMetaProperty target;

        public SyncProperty(TextMetaProperty target, T initialValue) {
            super(initialValue);
            this.target = target;
        }

        @Override
        public boolean set(T value) {
            if (!super.set(value)) {
                return false;
            }
            switch (target) {
                    case x:
                        view.setX(((Double) value).floatValue());
                        break;
                    case y:
                        view.setY(((Double) value).floatValue());
                        break;
                    case size:
                        view.setSize(((Double) value).floatValue());
                        break;
                    case text:
                        view.setText((String) value);
                        break;
            }
            return true;
        }

    }


    enum TextMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), size(Types.NUMBER),
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
