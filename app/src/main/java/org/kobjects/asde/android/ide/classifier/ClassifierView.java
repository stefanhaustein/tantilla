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
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.Struct;
import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.classifier.Trait;

import java.util.ArrayList;
import java.util.Collections;

public class ClassifierView extends SymbolView {

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


  public ClassifierView(MainActivity mainActivity, Property symbol) {
    super(mainActivity, symbol);

    if (symbol.getStaticValue() instanceof Trait) {
      titleView.setTypeIndicator("T", Colors.LIGHT_GREEN, false);
    } else {
      titleView.setTypeIndicator("C", Colors.LIGHT_BLUE, false);
    }

    titleView.setMoreClickListener(clicked -> {
      PopupMenu popupMenu = new PopupMenu(mainActivity, clicked);
      popupMenu.getMenu().add("Add Property").setOnMenuItemClickListener(item -> {
        PropertyFlow.createProperty(mainActivity, (Struct) symbol.getStaticValue());
        return true;
      });
      popupMenu.getMenu().add("Add Method").setOnMenuItemClickListener(item -> {
        FunctionSignatureFlow.createMethod(mainActivity, (Struct) symbol.getStaticValue());
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

    Iterable<? extends Property> symbols;
    if (symbol.getStaticValue() instanceof Classifier) {
      symbols = ((Classifier) symbol.getStaticValue()).getAllProperties();
    } else {
      symbols = new ArrayList<>();
   //   symbols = ((Trait) symbol.getStaticValue()).propertyMap.values();
    }
    getContentView().synchronizeTo(symbols, expandListener, null);

  }

}
