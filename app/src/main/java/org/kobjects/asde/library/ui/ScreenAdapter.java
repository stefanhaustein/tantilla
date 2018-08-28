package org.kobjects.asde.library.ui;

import android.view.View;

import org.kobjects.asde.lang.Types;
import org.kobjects.graphics.Viewport;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.Type;

public class ScreenAdapter extends Instance implements View.OnLayoutChangeListener{
    private final Viewport viewport;
    private float scale;

    // Hack; access from viewport instead.
    float width;
    float height;

    final PhysicalProperty<Double> widthProperty = new PhysicalProperty<>(0.0);
    final PhysicalProperty<Double> heightProperty = new PhysicalProperty<>(0.0);

    public static Classifier CLASSIFIER = new Classifier(ScreenMetaProperty.values()) {
        @Override
        public Object createInstance() {
            throw new RuntimeException("Singleton");
        }
    };

    public final Classifier spriteClassifier = new Classifier(SpriteAdapter.SpriteMetaProperty.values()) {
        @Override
        public Object createInstance() {
            return new SpriteAdapter(spriteClassifier, ScreenAdapter.this);
        }
    };

    public final Classifier textClassifier = new Classifier(TextAdapter.TextMetaProperty.values()) {
        @Override
        public Object createInstance() {
            return new TextAdapter(textClassifier, ScreenAdapter.this);
        }
    };


    public ScreenAdapter(Viewport viewport) {
        super(CLASSIFIER);
        this.viewport = viewport;
        viewport.addOnLayoutChangeListener(this);
    }

    public void clear() {
        viewport.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewport.clear();
            }
        });
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((ScreenMetaProperty) property) {
            case width: return widthProperty;
            case height: return heightProperty;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int widthPx = right - left;
        int heightPx = bottom - top;

        scale = Math.min(widthPx, heightPx) / 100f;

        width = widthPx / scale;
        height = heightPx / scale;

        widthProperty.set(Double.valueOf(width));
        heightProperty.set(Double.valueOf(height));

    }

    public Viewport getViewport() {
        return viewport;
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
