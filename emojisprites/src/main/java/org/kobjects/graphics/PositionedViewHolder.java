package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;

public abstract class PositionedViewHolder<T extends View> {
    protected float x;
    protected float y;
    protected float z;

    final Screen screen;
    final AnchorView<T> view;
    boolean syncRequested;

    ViewGroup anchor;

    PositionedViewHolder(Screen screen, T view) {
        this.screen = screen;
        this.anchor = screen.view;
        this.view = new AnchorView<>(view);
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
                if (anchor != null && anchor != view.getParent()) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                }
                syncUi();
            });
        }
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

    public void setAnchor(ViewGroup anchor) {
        this.anchor = anchor;
        requestSync();
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
