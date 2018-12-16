package org.kobjects.graphics;

import android.view.ViewGroup;

public class ViewHolder<T extends ViewGroup> {

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

}
