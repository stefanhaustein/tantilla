package org.kobjects.graphics;

import android.view.View;

public abstract class PositionedViewHolder<T extends View> extends ViewHolder<T> {
    protected float x;
    protected float y;
    protected float z;

    PositionedViewHolder(Viewport viewport, T view) {
        super(viewport, view);
        view.setTag(this);
        viewport.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (PositionedViewHolder.this.view.getParent() != null) {
                    requestSync();
                }
            }
        });
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

    public boolean setZ(float z) {
        if (z == this.z) {
            return false;
        }
            this.z = z;
            requestSync();

        return true;
    }
}
