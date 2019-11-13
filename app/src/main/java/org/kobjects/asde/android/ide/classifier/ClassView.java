package org.kobjects.asde.android.ide.classifier;

import android.widget.PopupMenu;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.symbol.DeleteFlow;
import org.kobjects.asde.android.ide.function.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.symbol.RenameFlow;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.symbol.SymbolListView;
import org.kobjects.asde.android.ide.symbol.SymbolView;
import org.kobjects.asde.android.ide.symbol.ExpandListener;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.StaticSymbol;

import java.util.Collections;

public class ClassView extends SymbolView {

  SymbolView currentSymbolView;

  public final ExpandListener expandListener = new ExpandListener() {
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


  public ClassView(MainActivity mainActivity, StaticSymbol symbol) {
    super(mainActivity, symbol);

    titleView.setTypeIndicator(R.drawable.outline_widgets_24, Colors.LIGHT_BLUE, false);

    titleView.setMoreClickListener(clicked -> {
      PopupMenu popupMenu = new PopupMenu(mainActivity, clicked);
      popupMenu.getMenu().add("Add Property").setOnMenuItemClickListener(item -> {
        PropertyFlow.createProperty(mainActivity, (ClassImplementation) symbol.getValue());
        return true;
      });
      popupMenu.getMenu().add("Add Method").setOnMenuItemClickListener(item -> {
        FunctionSignatureFlow.createMethod(mainActivity, (ClassImplementation) symbol.getValue());
        return true;
      });
      popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
        RenameFlow.start(mainActivity, symbol);
        return true;
      });
      popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
        DeleteFlow.start(mainActivity, symbol);
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
