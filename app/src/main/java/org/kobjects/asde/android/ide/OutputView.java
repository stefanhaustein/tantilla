package org.kobjects.asde.android.ide;

import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.TitleView;

import java.util.ArrayList;

public class OutputView extends LinearLayout {

  private final MainActivity mainActivity;
  final TitleView titleView;
  private final ArrayList<View> viewList = new ArrayList<>();

  private ExpandableList contentView;
  private ExpandableList lastContentView;

  OutputView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
    setOrientation(LinearLayout.VERTICAL);

    titleView = new TitleView(mainActivity, Colors.PRIMARY_FILTER, view -> {
      PopupMenu popupMenu = new PopupMenu(mainActivity, view);
      Menu menu = popupMenu.getMenu();
      menu.add("Clear").setOnMenuItemClickListener(menuItem -> {
        mainActivity.clearOutput();
        return true;
      });
      popupMenu.show();
    });
    titleView.setTitle("Output");

    titleView.setOnClickListener(view -> syncContent());

    addView(titleView);
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
      lastContentView = getContentView();
  }

  private ExpandableList getContentView() {
    if (mainActivity.codeView != null) {
      return mainActivity.codeView;
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
