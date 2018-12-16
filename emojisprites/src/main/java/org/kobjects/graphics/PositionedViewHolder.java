package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class PositionedViewHolder<T extends View> extends ViewHolder<AnchorView<T>> {
    protected float x;
    protected float y;
    protected float z;

    final Screen screen;
    boolean syncRequested;

    ViewHolder<?> anchor;

    PositionedViewHolder(Screen screen, T view) {
        super(new AnchorView<>(view));
        this.screen = screen;
        this.anchor = screen;
        view.setTag(this);
        screen.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (PositionedViewHolder.this.view.getParent() != null) {
                    requestSync();
                }
            }
        });
    }


    abstract void syncUi();

    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            screen.activity.runOnUiThread(() -> {
                syncRequested = false;
                if (anchor.view != view.getParent()) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    anchor.view.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                syncUi();
            });
        }
    }

    public ViewHolder<?> getAnchor() {
        return anchor;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public boolean setX(float x) {
        if (x == this.x) {
            return false;
        }
        this.x = x;
        requestSync();
        return true;
    }

    public boolean setY(float y) {
        if (y == this.y) {
            return false;
        }
         this.y = y;
         requestSync();
        return true;
    }

    public boolean setAnchor(ViewHolder<?> anchor) {
        if (this.anchor == anchor) {
            return false;
        }
        this.anchor = anchor;
        requestSync();
        return true;
    }

    public boolean setZ(float z) {
        if (z == this.z) {
            return false;
        }
            this.z = z;
            requestSync();

        return true;
    }

}
