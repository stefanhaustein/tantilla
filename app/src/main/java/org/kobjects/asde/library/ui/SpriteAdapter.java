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
    private ScreenAdapter screen;

    final NumberProperty x = new NumberProperty(SpriteMetaProperty.x);
    final NumberProperty y = new NumberProperty(SpriteMetaProperty.y);
    final NumberProperty z = new NumberProperty(SpriteMetaProperty.z);
    final NumberProperty size = new NumberProperty(SpriteMetaProperty.size);
    final NumberProperty angle = new NumberProperty(SpriteMetaProperty.angle);
    final NumberProperty left = new NumberProperty(SpriteMetaProperty.left);
    final NumberProperty right = new NumberProperty(SpriteMetaProperty.right);
    final NumberProperty top = new NumberProperty(SpriteMetaProperty.top);
    final NumberProperty bottom = new NumberProperty(SpriteMetaProperty.bottom);
    final StringProperty text = new StringProperty(SpriteMetaProperty.text);
    final StringProperty label = new StringProperty(SpriteMetaProperty.label);
    final StringProperty face = new StringProperty(SpriteMetaProperty.face);

    public SpriteAdapter(Classifier classifier, final ScreenAdapter screen) {
        super(classifier);
        this.screen = screen;
        sprite = new Sprite(screen.getViewport());
        sprite.setSize(10);
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((SpriteMetaProperty) property) {
            case x: return x;
            case y: return y;
            case z: return z;
            case left: return left;
            case right: return right;
            case top: return top;
            case bottom: return bottom;
            case size: return size;
            case angle: return angle;
            case label: return label;
            case text: return text;
            case face: return face;
        }
        throw new IllegalArgumentException();
    }


    class NumberProperty extends Property<Double> {
        private final SpriteMetaProperty target;

        NumberProperty(SpriteMetaProperty target) {
            this.target = target;
        }

        @Override
        public Double get() {
            switch (target) {
                case x:
                    return Double.valueOf(sprite.getX());
                case y:
                    return Double.valueOf(sprite.getY());
                case z:
                    return Double.valueOf(sprite.getZ());
                case angle:
                    return Double.valueOf(sprite.getAngle());
                case size:
                    return Double.valueOf(sprite.getSize());
                case left:
                    return Double.valueOf(sprite.getX() + (screen.width - sprite.getSize()) / 2);
                case right:
                    return Double.valueOf((screen.width - sprite.getSize()) / 2 - sprite.getX());
                case top:
                    return Double.valueOf((screen.height - sprite.getSize()) / 2 - sprite.getY());
                case bottom:
                    return Double.valueOf(sprite.getY() + (screen.height - sprite.getSize()) / 2);
            }
            throw new RuntimeException();
        }

        @Override
        public boolean set(Double value) {
            switch (target) {
                    case x:
                        return sprite.setX(value.floatValue());
                    case y:
                        return sprite.setY(value.floatValue());
                    case z:
                        return sprite.setZ(value.floatValue());
                    case angle:
                        return sprite.setAngle(value.floatValue());
                    case size:
                        return sprite.setSize(value.floatValue());
                case left:
                    return sprite.setX(value.floatValue() + (sprite.getSize() - screen.width) / 2);
                case right:
                    return sprite.setX((screen.width - sprite.getSize()) / 2 - value.floatValue());
                case top:
                    return sprite.setY((screen.height - sprite.getSize()) / 2 - value.floatValue());
                case bottom:
                    return sprite.setY(value.floatValue() + (sprite.getSize() - screen.height) / 2);
            }
            throw new RuntimeException();
        }

    }

    class StringProperty extends Property<String> {
        private final SpriteMetaProperty target;

        StringProperty(SpriteMetaProperty target) {
            this.target = target;
        }

        public String get() {
            switch (target) {
                case text:
                    return sprite.getText();
                case face:
                    return sprite.getFace();
                case label:
                    return sprite.getLabel();
            }
            throw new RuntimeException();
        }

        public boolean set(String value) {
            switch (target) {
                case text: return sprite.setText(value);
                case label: return sprite.setLabel(value);
                case face:return sprite.setFace(value);
            }
            throw new RuntimeException();
        }
    }


    enum SpriteMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), z(Types.NUMBER), size(Types.NUMBER),
        left(Types.NUMBER), right(Types.NUMBER), top(Types.NUMBER), bottom(Types.NUMBER),
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
