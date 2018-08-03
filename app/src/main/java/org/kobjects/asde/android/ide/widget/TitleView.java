package org.kobjects.asde.android.ide.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitleView extends LinearLayout {

    TextView textView;

    public TitleView(Context context) {
        super(context);
        setBackgroundColor(Colors.PRIMARY);

        textView = new TextView(context);
        textView.setTextSize(20);
       // setTextColor(0x0ffffffff);

        int padding = Dimensions.dpToPx(context, 6);
        textView.setPadding(padding, padding, padding, padding);


        addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        textView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    }

    public void setTitle(String title) {
        textView.setText(title);
    }
}
