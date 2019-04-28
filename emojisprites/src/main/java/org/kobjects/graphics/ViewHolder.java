package org.kobjects.graphics;

import android.view.ViewGroup;

public abstract class ViewHolder<T extends ViewGroup> {

    public T view;
    Object tag;

    ViewHolder(T view) {
        this.view = view;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public Object getTag() {
        return tag;
    }

    /**
     * Returns the normalized width of the view. For the screen, this value is negative.
     */
    public abstract float getWidthForAnchoring();

    /**
     * Returns the normalized height of the view. For the screen, this value is negative.
     */
    public abstract float getHeightForAnchoring();

}
