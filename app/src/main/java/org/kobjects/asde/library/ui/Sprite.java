package org.kobjects.asde.library.ui;

import android.app.Activity;

import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.PropertyDescriptor;
import org.kobjects.asde.lang.classifier.PhysicalProperty;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.emojisprites.EmojiSprite;

public class Sprite extends Instance implements Runnable {

    final Screen screen;
    final EmojiSprite sprite;

    enum SpriteMetaProperty implements PropertyDescriptor {
        x(Double.class), y(Double.class), size(Double.class);

        private final Object type;

        SpriteMetaProperty(Object type) {
            this.type = type;
        }

        @Override
        public Object type() {
            return type;
        }
    }

    final SyncProperty<Double> x = new SyncProperty<>(Double.valueOf(0));
    final SyncProperty<Double> y = new SyncProperty<>(Double.valueOf(0));
    final SyncProperty<Double> size = new SyncProperty<>(Double.valueOf(10));

    boolean syncRequested;

    public Sprite(Classifier classifier, Screen screen) {
        super(classifier);
        this.screen = screen;
        sprite = new EmojiSprite(screen.view);

        requestSync();
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((SpriteMetaProperty) property) {
            case x: return x;
            case y: return y;
            case size: return size;
        }
        throw new IllegalArgumentException();
    }

    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            ((Activity) screen.view.getContext()).runOnUiThread(this);
        }
    }

    public void run() {
        syncRequested = false;
        if (!sprite.isVisible()) {
            sprite.show();
        }
        sprite.setScale(size.get().floatValue() / 10);
        sprite.setX(x.get().floatValue());
        sprite.setY(y.get().floatValue());
    }


    class SyncProperty<T> extends PhysicalProperty<T> {
        public SyncProperty(T initialValue) {
            super(initialValue);
        }

        @Override
        public boolean set(T value) {
            if (super.set(value)) {
                requestSync();
                return true;
            }
            return false;
        }
    }
}
