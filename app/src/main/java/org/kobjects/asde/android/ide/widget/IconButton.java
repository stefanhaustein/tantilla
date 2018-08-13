package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.widget.ImageView;

public class IconButton extends ImageView {
    public IconButton(Context context, int resId) {
        super(context);
        setImageResource(resId);
        int padding = Dimensions.dpToPx(context, 12);
        setPadding(padding, padding, padding, padding);
        setAlpha(138);
    }
}
