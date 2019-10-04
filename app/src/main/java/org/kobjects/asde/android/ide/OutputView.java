package org.kobjects.asde.android.ide;

import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.widget.TitleView;

public class OutputView extends LinearLayout {

  final MainActivity mainActivity;
  final TitleView titleView;

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
    addView(titleView);
  }

}
