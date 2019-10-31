package org.kobjects.asde.android.ide;

import android.content.Context;

public class Dimensions {
    public static float pxToDp(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static int dpToPx(final Context context, final float dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

}
