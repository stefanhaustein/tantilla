package org.kobjects.asde.android.ide;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.IconButton;

import java.util.ArrayList;

public class TextOutputView extends LinearLayout {

  private final MainActivity mainActivity;
  final Toolbar titleView;
  private final ArrayList<View> viewList = new ArrayList<>();

  private ExpandableList contentView;


  TextOutputView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
    setOrientation(LinearLayout.VERTICAL);

    titleView = new Toolbar(mainActivity);

 //   SpannableString spannableString = new SpannableString("Text Output");
   // spannableString.setSpan(new StyleSpan(Typeface.NORMAL), 0, 11, 0);

   /* TextView titleTextView = new TextView(mainActivity);
    titleTextView.setText("Output");
    titleTextView.setTextSize(24);*/

//    titleView.addView(titleTextView);
   // titleView.setTitle(spannableString);
//    titleView.setTitleTextColor(0xffaaaaaa);
    titleView.setTitle("Output");
    titleView.setBackgroundColor(Colors.PRIMARY_FILTER);

    Toolbar.LayoutParams clearParams =new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
    clearParams.gravity = Gravity.END;
    IconButton clearButton = new IconButton(getContext(), R.drawable.baseline_delete_24);
    clearButton.setOnClickListener(view -> clear());
    titleView.addView(clearButton, clearParams);

    addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Dimensions.dpToPx(mainActivity, 48)));
  }

  public void syncContent() {
      getContentView().removeAllViews();
      for (View view : viewList) {
        if (view.getParent() != getContentView()) {
          if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
          }
          getContentView().addView(view);
        }
      }
  }

  private LinearLayout getContentView() {
    if (mainActivity.sharedCodeViewAvailable()) {
      return mainActivity.obtainSharedCodeView(this);
    }

    if (contentView == null) {
      contentView = new ExpandableList(mainActivity);
      addView(contentView);
    }

    return contentView;
  }

  public void addContent(View view) {
    viewList.add(view);
    getContentView().addView(view);
  }


  public void removeContent(View view) {
    viewList.remove(view);
    getContentView().removeView(view);
  }

  public void clear() {
    viewList.clear();
    getContentView().removeAllViews();
  }
}
