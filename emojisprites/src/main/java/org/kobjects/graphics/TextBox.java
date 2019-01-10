package org.kobjects.graphics;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Objects;

public class TextBox extends PositionedViewHolder<TextView> {

    private boolean layoutDirty;
    private String text;
    private float size = 10;

    public TextBox(Screen screen) {
        super(screen, new TextView(screen.activity));
        view.wrapped.setTextColor(Color.GRAY);
        // view.wrapped.setBackgroundColor(0x88ff0000);  // Debugging...
        // view.setBackgroundColor(0x8800ff00);
    }

    @Override
    public void syncUi() {
        if (layoutDirty) {
            layoutDirty = false;
            view.wrapped.setText(text);
            view.wrapped.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * screen.scale);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            view.wrapped.requestLayout();
        }

        float centerXpx = anchor.view.getWidth() / 2 + x * screen.scale;
        float centerYpx = anchor.view.getHeight() / 2 - y * screen.scale;

        view.setTranslationX(centerXpx - view.getMeasuredWidth() / 2);
        view.setTranslationY(centerYpx - view.getMeasuredHeight() / 2);
        view.setTranslationZ(z);
    }

    public boolean setSize(float size) {
        if (size == this.size) {
            return false;
        }
        this.size = size;
        layoutDirty = true;
        requestSync();
        return true;
    }

    public boolean setText(String text) {
        if (Objects.equals(text, this.text)) {
            return false;
        }
        this.text = text;
        layoutDirty = true;
        requestSync();
        return true;
    }

    public float getSize() {
        return size;
    }

    public String getText() {
        return text;
    }

    @Override
    public float getWidthForAnchoring() {
        return view.getWidth() / screen.scale;
    }

    @Override
    public float getHeightForAnchoring() {
        return view.getHeight() / screen.scale;
    }
}
