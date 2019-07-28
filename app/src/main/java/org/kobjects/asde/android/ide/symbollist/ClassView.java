package org.kobjects.asde.android.ide.symbollist;

import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.StaticSymbol;

import java.util.Collections;

public class ClassView extends SymbolView {

  SymbolView currentSymbolView;

  private final ExpandListener expandListener = new ExpandListener() {
    @Override
    public void notifyExpanding(SymbolView symbolView, boolean animated) {
      if (symbolView != currentSymbolView) {
        if (currentSymbolView != null) {
          currentSymbolView.setExpanded(false, animated);
        }
        currentSymbolView = symbolView;
        if (symbolView instanceof FunctionView) {
          mainActivity.programView.currentFunctionView = (FunctionView) symbolView;
        }
      }
    }
  };


  ClassView(MainActivity mainActivity, StaticSymbol symbol) {
    super(mainActivity, symbol);

    titleView.setTypeIndicator('C', mainActivity.colors.cyan);

    titleView.setMoreClickListener(clicked -> {
      PopupMenu popupMenu = new PopupMenu(mainActivity, clicked);
      popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
        new RenameFlow(mainActivity, symbol).start();
        return true;
      });
      popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
        new DeleteFlow(mainActivity, symbol).start();
        return true;
      });
      popupMenu.show();
    });
  }

  @Override
  public SymbolListView getContentView() {
    if (contentView == null) {
      contentView = new SymbolListView(mainActivity);
      addView(contentView);
    }
    return (SymbolListView) contentView;
  }

  @Override
  public void syncContent() {
    refresh();

    if (!expanded) {
      getContentView().synchronizeTo(Collections.emptyList(), expandListener, null);
      return;
    }

    getContentView().synchronizeTo(((ClassImplementation) symbol.getValue()).propertyMap.values(), expandListener, null);

  }

}
