package org.kobjects.asde.android.ide.symbollist;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.Function;

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

    if (!expanded) {
      getContentView().synchronizeTo(Collections.emptyList(), expandListener, null);
      return;
    }

    getContentView().synchronizeTo(((ClassImplementation) symbol.getValue()).propertyMap.values(), expandListener, null);

  }
}
