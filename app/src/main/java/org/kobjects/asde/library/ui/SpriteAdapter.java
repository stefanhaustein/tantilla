package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Method;
import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.Type;
import org.kobjects.graphics.Sprite;

public class SpriteAdapter extends Instance {

    public static final Classifier CLASSIFIER =
            new Classifier(SpriteAdapter.SpriteMetaProperty.values()) {
                @Override
                public SpriteAdapter createInstance() {
                    throw new RuntimeException("Use screen.createSprite()");
                }
            };


    final Sprite sprite;
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
    final ObjectProperty text = new ObjectProperty(SpriteMetaProperty.text);
    final ObjectProperty label = new ObjectProperty(SpriteMetaProperty.label);
    final ObjectProperty face = new ObjectProperty(SpriteMetaProperty.face);
    final ObjectProperty anchor = new ObjectProperty(SpriteMetaProperty.anchor);

    public SpriteAdapter(final ScreenAdapter screen) {
        super(CLASSIFIER);
        this.screen = screen;
        sprite = new Sprite(screen.getScreen());
        sprite.setTag(this);
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
            case anchor: return anchor;
            case say: return new Method((FunctionType) SpriteMetaProperty.say.type()) {
                        @Override
                        public Object call(Interpreter interpreter, int paramCount) {
                            text.set(interpreter.localStack.getLocal(0));
                            return null;
                        }
                };
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
                    return (double) sprite.getX();
                case y:
                    return (double) sprite.getY();
                case z:
                    return (double) sprite.getZ();
                case angle:
                    return (double) sprite.getAngle();
                case size:
                    return (double) sprite.getSize();
                case left:
                    return sprite.getX() - (sprite.getAnchor().getWidthForAnchoring() + sprite.getSize()) / 2.0;
                case right:
                    return -sprite.getX() - (sprite.getAnchor().getWidthForAnchoring() + sprite.getSize()) / 2.0;
                case top:
                    return -sprite.getY() - (sprite.getAnchor().getHeightForAnchoring() + sprite.getSize()) / 2.0;
                case bottom:
                    return sprite.getY() - (sprite.getAnchor().getHeightForAnchoring() + sprite.getSize()) / 2.0;

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
                    return sprite.setX(value.floatValue() + (sprite.getSize() + sprite.getAnchor().getWidthForAnchoring()) / 2);
                case right:
                    return sprite.setX(-(value.floatValue() + (sprite.getAnchor().getWidthForAnchoring() + sprite.getSize()) / 2));
                case top:
                    return sprite.setY(-(value.floatValue() + (sprite.getAnchor().getHeightForAnchoring() + sprite.getSize()) / 2));
                case bottom:
                    return sprite.setY(value.floatValue() + (sprite.getSize() + sprite.getAnchor().getHeightForAnchoring()) / 2);
            }
            throw new RuntimeException();
        }

    }

    class ObjectProperty extends Property<Object> {
        private final SpriteMetaProperty target;

        ObjectProperty(SpriteMetaProperty target) {
            this.target = target;
        }

        public Object get() {
            switch (target) {
                case text:
                    return sprite.getBubbleText();
                case face:
                    return sprite.getFace();
                case label:
                    return sprite.getLabelText();
                case anchor:
                    return sprite.getAnchor().getTag();


            }
            throw new RuntimeException();
        }

        public boolean set(Object value) {
            switch (target) {
                case text: return sprite.setBubbleText((String) value);
                case label: return sprite.setLabelText((String) value);
                case face:return sprite.setFace((String) value);
                case anchor: return sprite.setAnchor(((SpriteAdapter) value).sprite);
            }
            throw new RuntimeException();
        }
    }


    enum SpriteMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), z(Types.NUMBER), size(Types.NUMBER),
        left(Types.NUMBER), right(Types.NUMBER), top(Types.NUMBER), bottom(Types.NUMBER),
        angle(Types.NUMBER), label(Types.STRING), text(Types.STRING), face(Types.STRING),
        anchor(SpriteAdapter.CLASSIFIER),
        say(new FunctionType(Types.VOID, Types.STRING));

        final Type type;

        SpriteMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
