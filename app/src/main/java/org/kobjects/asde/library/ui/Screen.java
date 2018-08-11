package org.kobjects.asde.library.ui;

import android.view.View;
import android.widget.FrameLayout;

import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Property;

public class Screen extends Instance implements View.OnLayoutChangeListener{
    private final FrameLayout view;
    private float scale;

    public static Classifier CLASSIFIER = new Classifier() {
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

    public Screen(FrameLayout view) {
        super(CLASSIFIER);
        this.view = view;
        view.addOnLayoutChangeListener(this);
    }

    public void clear() {
        while (view.getChildCount() > 1) {
            view.removeViewAt(view.getChildCount() -1 );
        }
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        throw new IllegalArgumentException();
    }

    float getScale() {
        return scale;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int width = right - left;
        int height = bottom - top;

        scale = Math.min(width, height) / 100;

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
}
