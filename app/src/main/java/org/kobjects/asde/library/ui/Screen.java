package org.kobjects.asde.library.ui;

import android.widget.FrameLayout;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.PropertyDescriptor;
import org.kobjects.asde.lang.classifier.Property;

public class Screen extends Instance {

    FrameLayout view;

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
}
