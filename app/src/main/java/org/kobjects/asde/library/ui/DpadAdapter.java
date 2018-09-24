package org.kobjects.asde.library.ui;

import android.view.MotionEvent;
import android.view.View;

import org.kobjects.asde.lang.Types;
import org.kobjects.graphics.Dpad;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Type;

public class DpadAdapter extends Instance {

    final Dpad dpad;
    final TouchProperty left;
    final TouchProperty up;
    final TouchProperty down;
    final TouchProperty right;
    final TouchProperty fire;
    final Property<Number> visible;

    static Classifier CLASSIFER = new Classifier(DpadMetaProperty.values()) {
        @Override
        public Instance createInstance() {
            throw new RuntimeException("Singleton");
        }
    };


    public DpadAdapter(final Dpad dpad) {
        super(CLASSIFER);
        this.dpad = dpad;
        left = new TouchProperty(dpad.left);
        up = new TouchProperty(dpad.up);
        down = new TouchProperty(dpad.down);
        right = new TouchProperty(dpad.right);
        fire = new TouchProperty(dpad.fire);
        visible = new Property<Number>() {
            @Override
            public boolean set(Number number) {
                return dpad.setVisible(number.doubleValue() != 0);
            }

            @Override
            public Number get() {
                return dpad.getVisible() ? 1.0 : 0.0;
            }
        };
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((DpadMetaProperty) property) {
            case up: return up;
            case left: return left;
            case right: return right;
            case down: return down;
            case fire: return fire;
            case visible: return visible;
        }
        throw new RuntimeException("Unrecognized property: " + property);
    }

    enum DpadMetaProperty implements PropertyDescriptor {
        left(Types.NUMBER),
        right(Types.NUMBER),
        up(Types.NUMBER),
        down(Types.NUMBER),
        fire(Types.NUMBER),
        visible(Types.NUMBER);

        private final Type type;

        DpadMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }


    static class TouchProperty extends PhysicalProperty<Double> implements View.OnTouchListener {

        public TouchProperty(View view) {
            super(0.0);
            view.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                set(1.0);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                set(0.0);
                return true;
            }
            return false;
        }
    }

}
