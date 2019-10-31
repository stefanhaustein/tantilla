package org.kobjects.asde.android.library.ui;

import android.view.MotionEvent;
import android.view.View;

import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.Dpad;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.InstanceTypeImpl;
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
    final Property<Boolean> visible;

    static InstanceTypeImpl TYPE = new InstanceTypeImpl("Dpad");
    static {
        TYPE.addProperties(DpadMetaProperty.values());
    }

    public DpadAdapter(final Dpad dpad) {
        super(TYPE);
        this.dpad = dpad;
        left = new TouchProperty(dpad.left);
        up = new TouchProperty(dpad.up);
        down = new TouchProperty(dpad.down);
        right = new TouchProperty(dpad.right);
        fire = new TouchProperty(dpad.fire);
        visible = new Property<Boolean>() {
            @Override
            public boolean setImpl(Boolean visible) {
                return dpad.setVisible(visible);
            }

            @Override
            public Boolean get() {
                return dpad.getVisible();
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
        left(Types.BOOLEAN),
        right(Types.BOOLEAN),
        up(Types.BOOLEAN),
        down(Types.BOOLEAN),
        fire(Types.BOOLEAN),
        visible(Types.BOOLEAN);

        private final Type type;

        DpadMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }


    static class TouchProperty extends PhysicalProperty<Boolean> implements View.OnTouchListener {

        public TouchProperty(View view) {
            super(false);
            view.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                set(true);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                set(false);
                return true;
            }
            return false;
        }
    }

}
