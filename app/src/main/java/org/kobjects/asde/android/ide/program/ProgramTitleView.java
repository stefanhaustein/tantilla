package org.kobjects.asde.android.ide.program;

import android.view.Gravity;

import androidx.appcompat.widget.Toolbar;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.MainMenu;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.program.Program;

public class ProgramTitleView extends Toolbar {
  private final MainActivity mainActivity;

  public ProgramTitleView(final MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
    setBackgroundColor(Colors.PRIMARY_FILTER);

    Toolbar.LayoutParams moreParams =new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
    moreParams.gravity = Gravity.END;
    IconButton moreButton = new IconButton(getContext(), R.drawable.baseline_more_vert_24);
    addView(moreButton, moreParams);
    moreButton.setOnClickListener(view -> MainMenu.show(mainActivity, view));

    Toolbar.LayoutParams projectParams =new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
    projectParams.gravity = Gravity.END;
    /*IconButton projectButton = new IconButton(getContext(), R.drawable.baseline_folder_open_24);
    addView(projectButton, projectParams);
    projectButton.setOnClickListener(view -> MainMenu.showProjectMenu(mainActivity, view)); */


    setOnClickListener(view -> {
      if (mainActivity.sharedCodeViewAvailable()) {
        mainActivity.textOutputView.syncContent();
      } else {
        mainActivity.programView.expand(!mainActivity.programView.expanded);
        mainActivity.programView.requestLayout();
      }
    });

    refresh();
  }

  public void refresh() {
    mainActivity.runOnUiThread(() -> refreshImpl());
  }

  private void refreshImpl() {
    Program program = mainActivity.program;
    boolean isDefaultSaveLocation = program.reference.name.isEmpty();
    setTitle(
        (isDefaultSaveLocation ? "ASDE" : program.reference.name)
        + (mainActivity.isUnsaved() ? "*" : ""));

  }
}
