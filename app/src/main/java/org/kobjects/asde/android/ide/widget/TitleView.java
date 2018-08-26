package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TitleView extends LinearLayout {

    AppCompatTextView textView;

    public TitleView(Context context) {
        super(context);
        setBackgroundColor(Colors.PRIMARY);

        textView = new AppCompatTextView(context);
        textView.setTextSize(20);
       // setTextColor(0x0ffffffff);

        int padding = Dimensions.dpToPx(context, 6);
        textView.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        addView(textView, layoutParams);
//        textView.setGravity(Gravity.CENTER_VERTICAL);
    }

    public void setTitle(String title) {
        textView.setText(title);
    }
}
