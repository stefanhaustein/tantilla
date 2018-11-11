package org.kobjects.asde.android.ide;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import org.kobjects.asde.R;

public class Colors {

    final int primary;
    final int primaryMedium;
    final int accent;
    final int background;
    final int accentMedium;
    final boolean darkMode;
    final private float[] hsvHelper;

    // public static final int PRIMARY_LIGHT = 0xffeceff1;
    // static final int SECONDARY_LIGHT = 0xffff6659;


    static int resolveColor(Context context, int id) {
        TypedValue outValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        boolean wasResolved =
                theme.resolveAttribute(
                        id, outValue, true);
        if (!wasResolved) {
            throw new RuntimeException("Missing resource " + id);
        }
        return outValue.resourceId == 0
                    ? outValue.data
                    : ContextCompat.getColor(
                    context, outValue.resourceId);
    }


    int interpolate(int color1, int color2) {
        long mask = 255;
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (((color1 & mask) + (color2 & mask)) / 2) & mask;
            mask <<= 8;
        }
        return (int) result;
    }


    Colors(Context context) {
        background = resolveColor(context, android.R.attr.windowBackground);
        darkMode = (background & 255 + ((background >> 8) & 255) + ((background >> 16) & 254)) < 3 * 128;
        primary = resolveColor(context, R.attr.colorPrimary);
        primaryMedium = interpolate(background, primary); // darkMode ? 0xff222222 : 0xffdde3e6;
        accent = resolveColor(context, R.attr.colorAccent);
        accentMedium = darkMode ?  0xffac1900 :  0xffff6659;
        hsvHelper = new float[] {0, darkMode ? 0.8f : 0.25f, darkMode ? 0.8f : 1f};
    }

    synchronized int hueToColor(int h) {
        hsvHelper[0] = h;
        return Color.HSVToColor(hsvHelper);
    }

}
