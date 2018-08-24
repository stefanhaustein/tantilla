package org.kobjects.asde.library.ui;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.Type;

public class Screen extends Instance implements View.OnLayoutChangeListener{
    private final FrameLayout view;
    final Activity activity;
    private float scale;

    final PhysicalProperty<Double> width = new PhysicalProperty<>(0.0);
    final PhysicalProperty<Double> height = new PhysicalProperty<>(0.0);

    public static Classifier CLASSIFIER = new Classifier(ScreenMetaProperty.values()) {
        @Override
        public Object createInstance() {
            throw new RuntimeException("Singleton");
        }
    };

    public final Classifier spriteClassifier = new Classifier(Sprite.SpriteMetaProperty.values()) {
        @Override
        public Object createInstance() {
            return new Sprite(spriteClassifier, Screen.this);
        }
    };

    public Screen(Activity activity, FrameLayout view) {
        super(CLASSIFIER);
        this.activity = activity;
        this.view = view;
        view.setClipChildren(false);

        view.addOnLayoutChangeListener(this);
    }

    public void clear() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //view.removeAllViews();
            }
        });
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((ScreenMetaProperty) property) {
            case width: return width;
            case height: return height;
        }
        throw new IllegalArgumentException();
    }

    float getScale() {
        return scale;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int widthPx = right - left;
        int heightPx = bottom - top;

        scale = Math.min(widthPx, heightPx) / 100;

        width.set(Double.valueOf(widthPx / scale));
        height.set(Double.valueOf(heightPx / scale));

        for (int i = 0; i < view.getChildCount(); i++) {
            View childView = view.getChildAt(i);
            Object tag = childView.getTag();
            if (tag instanceof Sprite) {
                ((Sprite) tag).requestSync();
            }
        }

    }

    public FrameLayout getView() {
        return view;
    }

    enum ScreenMetaProperty implements PropertyDescriptor {
        width(Types.NUMBER), height(Types.NUMBER);

        private final Type type;

        ScreenMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
