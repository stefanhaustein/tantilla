package org.kobjects.asde.library.ui;

import android.app.Activity;

import org.kobjects.asde.lang.Types;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.Type;
import org.kobjects.emojisprites.EmojiSprite;

public class Sprite extends Instance implements Runnable {
    private final EmojiSprite sprite;
    private final Screen screen;

    final SyncProperty<Double> x = new SyncProperty<>(0.0);
    final SyncProperty<Double> y = new SyncProperty<>(0.0);
    final SyncProperty<Double> size = new SyncProperty<>(10.0);
    final SyncProperty<Double> angle = new SyncProperty<>(0.0);

    boolean syncRequested;

    public Sprite(Classifier classifier, Screen screen) {
        super(classifier);
        this.screen = screen;
        sprite = new EmojiSprite(screen.getView());

        requestSync();
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((SpriteMetaProperty) property) {
            case x: return x;
            case y: return y;
            case size: return size;
            case angle: return angle;
        }
        throw new IllegalArgumentException();
    }

    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            ((Activity) screen.getView().getContext()).runOnUiThread(this);
        }
    }

    public void run() {
        syncRequested = false;
        if (!sprite.isVisible()) {
            sprite.show();
        }

        float scale = screen.getScale();
        float spriteMax = Math.max(sprite.getIntrinsicWidth(), sprite.getIntrinsicHeight());

        sprite.setSize(size.get().floatValue() * sprite.getIntrinsicWidth() / spriteMax * scale,
                size.get().floatValue() * sprite.getIntrinsicHeight() / spriteMax * scale);
        sprite.setX(x.get().floatValue() * scale);
        sprite.setY(y.get().floatValue() * scale);
        sprite.getImageView().setRotation(angle.get().floatValue());
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


    enum SpriteMetaProperty implements PropertyDescriptor {
        x(Types.NUMBER), y(Types.NUMBER), size(Types.NUMBER), angle(Types.NUMBER);

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
