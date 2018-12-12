package org.kobjects.graphics;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Objects;

public class TextBox extends PositionedViewHolder<TextView> {

    private boolean textDirty;
    private String text;
    private float size = 10;

    public TextBox(Viewport viewport) {
        super(viewport, new TextView(viewport.activity));
        view.wrapped.setTextColor(Color.BLACK);
    }

    @Override
    public void syncUi() {
        if (textDirty) {
            textDirty = false;
            view.wrapped.setText(text);
        }

        if (view.getParent() == null) {
            viewport.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        view.setTranslationX(x * viewport.scale);
        view.setTranslationY(y * viewport.scale);
        view.wrapped.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * viewport.scale);
        view.setTranslationZ(z);
    }

    public boolean setSize(float size) {
        if (size == this.size) {
            return false;
        }
        this.size = size;
        requestSync();
        return true;
    }

    public boolean setText(String text) {
        if (Objects.equals(text, this.text)) {
            return false;
        }
        this.text = text;
        textDirty = true;
        requestSync();
        return true;
    }

    public float getSize() {
        return size;
    }

    public String getText() {
        return text;
    }
}
