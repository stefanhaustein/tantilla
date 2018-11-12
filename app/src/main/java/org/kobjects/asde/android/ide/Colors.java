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
    final int primaryLight;
    final int accent;
    final int background;
    final int accentMedium;
    final boolean darkMode;
    final private float[] hsvHelper;

    final int red;
    final int cyan;
    final int purple;
    final int green;
    final int yellow;
    final int orange;

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


    Colors(Context context, Theme theme) {
        background = resolveColor(context, android.R.attr.windowBackground);
        darkMode = (background & 255 + ((background >> 8) & 255) + ((background >> 16) & 254)) < 3 * 128;
        primary = resolveColor(context, R.attr.colorPrimary);
        primaryMedium = interpolate(background, primary); // darkMode ? 0xff222222 : 0xffdde3e6;
        primaryLight = interpolate(background, primaryMedium);
        accent = resolveColor(context, R.attr.colorAccent);
        accentMedium = darkMode ?  0xffac1900 :  0xffff6659;
        hsvHelper = new float[] {0, darkMode ? 0.8f : 0.25f, darkMode ? 0.8f : 1f};

        switch(theme) {
            case C64:
                // Source: https://bigcode.wordpress.com/2016/10/30/commodore-64-color-codes/
                red = 0xff924a40;
                cyan = 0xff84c5cc;
                purple = 0xff9341b5;
                green = 0xff74b14b;
                yellow = 0xffd5df7c;
                orange =  0xff99692d;
                break;
             default:
                 // 500-colors from the material palette
                 int alpha = theme == Theme.ARCORN ? 0xaa000000 : 0x55000000;
                 red = alpha | 0xf44336;
                 cyan = alpha | 0x00bcd4;
                 purple = alpha | 0x9c27b0;
                 green = alpha | 0x4caf50;
                 yellow = alpha | 0xffeb3b;
                orange = alpha | 0xff9800                 ;

/*
            default:

            case SPECTRUM:
                // 200-colors from the material palette
                // https://material.io/tools/color/
                red = 0xffef9a9a;
                cyan = 0xff80deea;
                purple = 0xffce93d8;
                green = 0xffa5d6a7;
                yellow = 0xfffff59d;
                orange = 0xffffcc80;
                break;
             default:
                 // 800-colors from the material palette
                 // https://material.io/tools/color/
                 red = 0xffc62828;
                 cyan = 0xff00838f;
                 purple = 0xff6a1b9a;
                 green = 0xff2e7d32;
                 yellow = 0xfff9a825;
                 orange = 0xffef6c00;
                 break;*/
        }

    }

    synchronized int hueToColor(int h) {
        hsvHelper[0] = h;
        return Color.HSVToColor(hsvHelper);
    }

    public enum Theme {
        ARCORN, C64, SPECTRUM,
    }
}
