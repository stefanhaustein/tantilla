package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

public class IconButton extends AppCompatImageView {
    public IconButton(Context context, int resId) {
        super(context);
        setImageResource(resId);
        int padding = Dimensions.dpToPx(context, 12);
        setPadding(padding, padding, padding, padding);
    }

    public IconButton(Context context, int resId, OnClickListener listener) {
        this(context, resId);
        setOnClickListener(listener);
    }
}
