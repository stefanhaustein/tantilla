package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.Type;
import org.kobjects.graphics.Sprite;

public class SpriteAdapter extends Instance {
    private final Sprite sprite;

    final SyncProperty<Double> x = new SyncProperty<>(SpriteMetaProperty.x, 0.0);
    final SyncProperty<Double> y = new SyncProperty<>(SpriteMetaProperty.y, 0.0);
    final SyncProperty<Double> size = new SyncProperty<>(SpriteMetaProperty.size, 10.0);
    final SyncProperty<Double> angle = new SyncProperty<>(SpriteMetaProperty.angle, 0.0);
    final SyncProperty<String> text = new SyncProperty<>(SpriteMetaProperty.text, "");
    final SyncProperty<String> label = new SyncProperty<>(SpriteMetaProperty.label, "");
    final SyncProperty<String> face = new SyncProperty<>(SpriteMetaProperty.face, Sprite.DEFAULT_FACE);

    public SpriteAdapter(Classifier classifier, final ScreenAdapter screen) {
        super(classifier);
        sprite = new Sprite(screen.getViewport());
        sprite.setSize(10);
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((SpriteMetaProperty) property) {
            case x: return x;
            case y: return y;
            case size: return size;
            case angle: return angle;
            case label: return label;
            case text: return text;
            case face: return face;
        }
        throw new IllegalArgumentException();
    }


    class SyncProperty<T> extends PhysicalProperty<T> {
        private final SpriteMetaProperty target;

        public SyncProperty(SpriteMetaProperty target, T initialValue) {
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
                        sprite.setX(((Double) value).floatValue());
                        break;
                    case y:
                        sprite.setY(((Double) value).floatValue());
                        break;
                    case angle:
                        sprite.setRotation(((Double) value).floatValue());
                        break;
                    case size:
                        sprite.setSize(((Double) value).floatValue());
                        break;
                    case text:
                        sprite.setText((String) value);
                        break;
                    case label:
                        sprite.setLabel((String) value);
                        break;
                    case face:
                        sprite.setFace((String) value);
                        break;
            }
            return true;
        }

    }


    enum SpriteMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), size(Types.NUMBER),
        angle(Types.NUMBER), label(Types.STRING), text(Types.STRING), face(Types.STRING);

        private final Type type;

        SpriteMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
