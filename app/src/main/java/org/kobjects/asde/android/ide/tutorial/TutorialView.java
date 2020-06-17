package org.kobjects.asde.android.ide.tutorial;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.Dimensions;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.TextRenderer;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.markdown.Text;
import org.kobjects.markdown.parser.MarkdownParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class TutorialView extends LinearLayout {

  static final int HINT_COUNT = 2;

  IconButton prevButton;
  IconButton nextButton;
  MainActivity mainActivity;
  LinearLayout contentView;
  private boolean expanded = true;
  private int hintIndex;
  HashMap<Integer, Text> hintCache = new HashMap<>();

  public TutorialView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;

    setOrientation(LinearLayout.VERTICAL);

    Toolbar titleView = new Toolbar(mainActivity);
    titleView.setTitle("Tutorial");
    titleView.setBackgroundColor(Colors.PRIMARY_FILTER);

    Toolbar.LayoutParams nextParams =new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
    nextParams.gravity = Gravity.END;
    nextButton = new IconButton(getContext(), R.drawable.baseline_arrow_forward_24);
    nextButton.setOnClickListener(view -> move(1));
    titleView.addView(nextButton, nextParams);

    Toolbar.LayoutParams prevParams =new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
    prevParams.gravity = Gravity.END;
    prevButton = new IconButton(getContext(), R.drawable.baseline_arrow_back_24);
    prevButton.setOnClickListener(view -> move(-1));
    titleView.addView(prevButton, prevParams);

    titleView.setOnClickListener(view -> {
      if (mainActivity.sharedCodeViewAvailable()) {
        expanded = false;
        expand(true);
      } else {
        ((ExpandableList) getContentView()).animateNextChanges();
        expand(!expanded);
      }
    });

    addView(titleView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Dimensions.dpToPx(mainActivity, 48)));

    move(0);
  }

  void move(int dir) {
    hintIndex += dir;
    nextButton.setEnabled(hintIndex < HINT_COUNT - 1);
    prevButton.setEnabled(hintIndex > 0);
    if (hintIndex < 0) {
      hintIndex = 0;
    } else if (hintIndex >= HINT_COUNT) {
      hintIndex = HINT_COUNT - 1;
    }

    if (hintCache.containsKey(hintIndex)) {
      expanded = false;
      expand(true);
    } else {
      final int finalIndex = hintIndex;
      new Thread(() -> {
        try {
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(
                  mainActivity.getAssets().open("tutorial/lesson0" + (finalIndex + 1) + ".md"), "utf-8"));
          Text text = MarkdownParser.parse(reader);
          hintCache.put(finalIndex, text);
          mainActivity.runOnUiThread(() -> {
            move(0);
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }).start();
    }
  }

  void expand(boolean expanded) {
    if (expanded != this.expanded) {
      this.expanded = expanded;
      syncContent();
    }
  }


  public void syncContent() {
    getContentView().removeAllViews();
    if (expanded) {
      Text text = hintCache.get(hintIndex);
      if (text != null) {
        TextRenderer.render(mainActivity, getContentView(), text, null);
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

}
