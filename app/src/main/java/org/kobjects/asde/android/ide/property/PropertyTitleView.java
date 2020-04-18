package org.kobjects.asde.android.ide.property;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import androidx.appcompat.widget.AppCompatTextView;

import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.Dimensions;
import org.kobjects.asde.android.ide.widget.IconButton;

import java.util.List;

public class PropertyTitleView extends LinearLayout {

  final LinearLayout vertical;
  final AppCompatTextView textView;
  View typeView;
  IconButton moreButton;

  public PropertyTitleView(Context context, String name) {
    super(context);
    // setBackgroundCol

    textView = new AppCompatTextView(context);
    textView.setTextSize(16);
    textView.setTypeface(Typeface.MONOSPACE);
    textView.setText(name);
    // textView.setPadding(0, padding, 0, 0);

    vertical = new LinearLayout(context);
    vertical.setOrientation(VERTICAL);
    vertical.addView(textView);

    // setTextColor(0x0ffffffff);

//        int padding = Dimensions.dpToPx(context, 6);
    //      textView.setPadding(padding, padding, padding, padding);

    LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
    layoutParams.gravity = Gravity.CENTER_VERTICAL;
    addView(vertical, layoutParams);
//        textView.setGravity(Gravity.CENTER_VERTICAL);

  }

  public void setTitle(String title) {
    textView.setText(title);
  }


  public void setTypeIndicator(String s, int color, boolean small) {

    if (typeView != null) {
      removeView(typeView);
    }

    TextView typeView = new AppCompatTextView(getContext());
    typeView.setGravity(Gravity.CENTER);
    typeView.setTextSize((s.length() == 1 ? 24 :  12) * (small ? 0.8f : 1));
    if (!small) {
      typeView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
    }
    int size = Dimensions.dpToPx(getContext(), 48);
    LayoutParams typeLayoutParams =  new LayoutParams(size, size);
    typeLayoutParams.gravity = Gravity.TOP;
    addView(typeView, 0, typeLayoutParams);

    float r = Dimensions.dpToPx(getContext(), 2);
    float[] radii = new float[]{r, r, r, r, r, r, r, r};

    Path path = new Path();
    path.moveTo(-0.5f, 0);
    path.lineTo(9, 0);
    path.lineTo(10.5f, 5);
    path.lineTo(9, 10);
    path.lineTo(-0.5f, 10);
    path.lineTo(1, 5);
    path.close();

    ShapeDrawable shape = new ShapeDrawable(s.equals("def") ? new PathShape(path, 10, 10) :  new RoundRectShape(radii, null, null));// : new OvalShape());
    shape.getPaint().setColor(color);
    int padding = Dimensions.dpToPx(getContext(), small ? 11 : 6);

    typeView.setBackground(new InsetDrawable(shape, padding));
    typeView.setText(s);
    /*  if (small) {
      typeView.setPadding(Dimensions.dpToPx(getContext(), 8), 0, 0, 0);
    }*/

    this.typeView = typeView;
  }

  public void setTypeIndicator(int res, int color, boolean small) {

    if (typeView != null) {
      removeView(typeView);
    }

    ImageView typeView = new ImageView(getContext());
    typeView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

    typeView.setImageResource(res);

    //  typeView.setGravity(Gravity.CENTER);
     // typeView.setTextSize(20);
     // typeView.setTypeface(Typeface.MONOSPACE);
      int size = Dimensions.dpToPx(getContext(), 48);
      LayoutParams typeLayoutParams =  new LayoutParams(size, size);
      typeLayoutParams.gravity = Gravity.TOP;
      addView(typeView, 0, typeLayoutParams);


    ShapeDrawable shape = new ShapeDrawable(new OvalShape());
    shape.getPaint().setColor(color);

    if (small) {
      typeView.setScaleX(0.75f);
      typeView.setScaleY(0.75f);
    }
    int padding = Dimensions.dpToPx(getContext(), 6);

    typeView.setBackground(new InsetDrawable(shape, padding));
//    typeView.setText(String.valueOf(c));
    /*  if (small) {
      typeView.setPadding(Dimensions.dpToPx(getContext(), 8), 0, 0, 0);
    }*/
  }


  public void setMoreClickListener(OnClickListener moreClickListener) {
    if (moreButton == null) {
      moreButton = new IconButton(getContext(), R.drawable.baseline_more_vert_24);
      addView(moreButton);
    }
    moreButton.setOnClickListener(moreClickListener);
  }


  public void setSubtitles(List<String> subtitles) {

    while (vertical.getChildCount() > 1) {
      vertical.removeViewAt(vertical.getChildCount() - 1);
    }

    for (String s: subtitles) {
      AppCompatTextView parameterView = new AppCompatTextView(getContext());
      parameterView.setMaxLines(1);
      parameterView.setText(s);
      // parameterView.setTextSize(10);
      parameterView.setTypeface(Typeface.MONOSPACE);
      vertical.addView(parameterView);
    }

  }

}
