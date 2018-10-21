package org.kobjects.asde.android.ide;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.android.ide.widget.Dimensions;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Types;

import java.util.List;

public class SymbolTitleView extends LinearLayout {

    AppCompatTextView typeView;
    AppCompatTextView textView;

    public SymbolTitleView(Context context, int color, char c, String name, List<String> subtitles) {
        super(context);
       // setBackgroundColor(0x0ffeeeeee);

        typeView = new AppCompatTextView(getContext());
        typeView.setGravity(Gravity.CENTER);
        typeView.setTextSize(20);
        typeView.setTypeface(Typeface.MONOSPACE);

        ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        shape.getPaint().setColor(color);
        int padding = Dimensions.dpToPx(getContext(), 6);

        typeView.setBackground(new InsetDrawable(shape, padding));
        typeView.setText(String.valueOf(c));

        int size = Dimensions.dpToPx(getContext(), 48);

        LayoutParams typeLayoutParams =  new LayoutParams(size, size);
        typeLayoutParams.gravity = Gravity.TOP;

        addView(typeView, typeLayoutParams);

        textView = new AppCompatTextView(context);
        textView.setTextSize(16);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setText(name);
       // textView.setPadding(0, padding, 0, 0);

        LinearLayout vertical = new LinearLayout(context);
        vertical.setOrientation(VERTICAL);
        vertical.addView(textView);

        for (String s: subtitles) {
            AppCompatTextView parameterView = new AppCompatTextView(context);
            parameterView.setText(s);
            // parameterView.setTextSize(10);
            parameterView.setTypeface(Typeface.MONOSPACE);
            vertical.addView(parameterView);
        }

        // setTextColor(0x0ffffffff);

//        int padding = Dimensions.dpToPx(context, 6);
  //      textView.setPadding(padding, padding, padding, padding);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        addView(vertical, layoutParams);
//        textView.setGravity(Gravity.CENTER_VERTICAL);


    }

}
