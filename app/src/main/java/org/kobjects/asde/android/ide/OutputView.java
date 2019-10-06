package org.kobjects.asde.android.ide;

import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.io.Console;

import java.util.ArrayList;

public class OutputView extends LinearLayout {

  private final MainActivity mainActivity;
  final TitleView titleView;
  private final ArrayList<View> viewList = new ArrayList<>();

  private ExpandableList contentView;


   static void populateMenu(MainActivity mainActivity, Menu menu) {

     menu.add("Clear").setOnMenuItemClickListener(menuItem -> {
       mainActivity.clearScreen(Console.ClearScreenType.CLS_STATEMENT);
       return true;
     });


     menu.add(1, 0, 0, "Overlay Graphics").setChecked(!mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = false;
      mainActivity.arrangeUi();
      return true;
    });
    menu.add(1, 0, 0, "Graphics Window").setChecked(mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = true;
      mainActivity.arrangeUi();
      return true;
    });
    menu.setGroupCheckable(1, true, true);

  }

  OutputView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
    setOrientation(LinearLayout.VERTICAL);

    titleView = new TitleView(mainActivity, Colors.PRIMARY_FILTER, view -> {
      PopupMenu popupMenu = new PopupMenu(mainActivity, view);
      populateMenu(mainActivity, popupMenu.getMenu());
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
