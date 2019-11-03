package org.kobjects.asde.android.ide.program;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.MainMenu;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.Program;

public class ProgramTitleView extends TitleView {
  private final MainActivity mainActivity;

  public ProgramTitleView(final MainActivity mainActivity) {
    super(mainActivity, Colors.PRIMARY_FILTER, view -> {
      MainMenu.show(mainActivity, view);
    });
    this.mainActivity = mainActivity;

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
            + (program.legacyMode ? " (legacy mode)️" : "")
        + (mainActivity.isUnsaved() ? "*" : ""));

  }
}
