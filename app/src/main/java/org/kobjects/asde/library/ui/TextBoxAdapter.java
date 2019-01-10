package org.kobjects.asde.library.ui;

import org.kobjects.asde.lang.Types;
import org.kobjects.graphics.TextBox;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class TextBoxAdapter extends Instance {

    public static final Classifier CLASSIFIER =
            new Classifier(TextBoxAdapter.TextMetaProperty.values()) {
                @Override
                public TextBoxAdapter createInstance() {
                    throw new RuntimeException("call screen.newTextBox");
                }
            };


    private final TextBox textBox;

    final NumberProperty x = new NumberProperty(TextMetaProperty.x);
    final NumberProperty y = new NumberProperty(TextMetaProperty.y);
    final NumberProperty z = new NumberProperty(TextMetaProperty.z);
    final NumberProperty left = new NumberProperty(TextMetaProperty.left);
    final NumberProperty right = new NumberProperty(TextMetaProperty.right);
    final NumberProperty top = new NumberProperty(TextMetaProperty.top);
    final NumberProperty bottom = new NumberProperty(TextMetaProperty.bottom);

    final NumberProperty size = new NumberProperty(TextMetaProperty.size);
    final StringProperty text = new StringProperty(TextMetaProperty.text);
    final ObjectProperty anchor = new ObjectProperty(TextMetaProperty.anchor);

    public TextBoxAdapter(final ScreenAdapter screen) {
        super(CLASSIFIER);
        textBox = new TextBox(screen.getScreen());
        textBox.setTag(this);
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((TextMetaProperty) property) {
            case x: return x;
            case y: return y;
            case size: return size;
            case text: return text;
            case anchor: return anchor;
            case left: return left;
            case right: return right;
            case top: return top;
            case bottom: return bottom;
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
                    return Double.valueOf(textBox.getX());
                case y:
                    return Double.valueOf(textBox.getY());
                case z:
                    return Double.valueOf(textBox.getZ());
                case size:
                    return Double.valueOf(textBox.getSize());
                case left:
                    return textBox.getX() - (textBox.getAnchor().getWidthForAnchoring() + textBox.getWidthForAnchoring()) / 2.0;
                case right:
                    return -textBox.getX() - (textBox.getAnchor().getWidthForAnchoring() + textBox.getWidthForAnchoring()) / 2.0;
                case top:
                    return -textBox.getY() - (textBox.getAnchor().getHeightForAnchoring() + textBox.getHeightForAnchoring()) / 2.0;
                case bottom:
                    return textBox.getY() - (textBox.getAnchor().getHeightForAnchoring() + textBox.getHeightForAnchoring()) / 2.0;

            }
            throw new RuntimeException();
        }

        @Override
        public boolean set(Double value) {
            switch (target) {
                case x:
                    return textBox.setX(value.floatValue());
                case y:
                    return textBox.setY(value.floatValue());
                case z:
                    return textBox.setZ(value.floatValue());
                case size:
                    return textBox.setSize(value.floatValue());
                case left:
                    return textBox.setX(value.floatValue() + (textBox.getWidthForAnchoring() + textBox.getAnchor().getWidthForAnchoring()) / 2);
                case right:
                    return textBox.setX(-(value.floatValue() + (textBox.getAnchor().getWidthForAnchoring() + textBox.getWidthForAnchoring()) / 2));
                case top:
                    return textBox.setY(-(value.floatValue() + (textBox.getAnchor().getHeightForAnchoring() + textBox.getHeightForAnchoring()) / 2));
                case bottom:
                    return textBox.setY(value.floatValue() + (textBox.getHeightForAnchoring() + textBox.getAnchor().getHeightForAnchoring()) / 2);            }
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
                    return textBox.getText();
            }
            throw new RuntimeException();
        }

        public boolean set(String value) {
            switch (target) {
                case text: return textBox.setText(value);
            }
            throw new RuntimeException();
        }
    }


    class ObjectProperty extends Property<Object> {
        private final TextMetaProperty target;

        ObjectProperty(TextMetaProperty target) {
            this.target = target;
        }

        public Object get() {
            switch (target) {
                case anchor:
                    return textBox.getAnchor().getTag();
            }
            throw new RuntimeException();
        }

        public boolean set(Object value) {
            switch (target) {
                case anchor: return textBox.setAnchor(((SpriteAdapter) value).sprite);
            }
            throw new RuntimeException();
        }
    }


    enum TextMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), z(Types.NUMBER), size(Types.NUMBER),
        left(Types.NUMBER), right(Types.NUMBER), top(Types.NUMBER), bottom(Types.NUMBER),
        text(Types.STRING), anchor(SpriteAdapter.CLASSIFIER);

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
