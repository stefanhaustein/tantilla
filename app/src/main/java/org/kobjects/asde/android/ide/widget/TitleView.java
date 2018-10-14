package org.kobjects.asde.android.ide.widget;

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

public class TitleView extends LinearLayout {

    AppCompatTextView textView;

    public TitleView(Context context) {
        super(context);
        setBackgroundColor(Colors.PRIMARY);

        textView = new AppCompatTextView(context);
        textView.setTextSize(20);
        textView.setTypeface(Typeface.MONOSPACE);
       // setTextColor(0x0ffffffff);

        int padding = Dimensions.dpToPx(context, 6);
        textView.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        addView(textView, layoutParams);
//        textView.setGravity(Gravity.CENTER_VERTICAL);


    }

    public void setType(char c) {

        TextView typeView = new TextView(getContext());
        typeView.setGravity(Gravity.CENTER);

        ShapeDrawable shape = new ShapeDrawable(new OvalShape());
        shape.getPaint().setColor(Colors.PRIMARY);
        int padding = Dimensions.dpToPx(getContext(), 6);


        typeView.setBackground(new InsetDrawable(shape, padding));
        typeView.setText("" + c);

        int size = Dimensions.dpToPx(getContext(), 48);

        LayoutParams typeLayoutParams =  new LayoutParams(size, size);
        typeLayoutParams.gravity = Gravity.TOP;

        addView(typeView, 0, typeLayoutParams);

    }

    public void setTitle(String title) {
        textView.setText(title);
    }
}
