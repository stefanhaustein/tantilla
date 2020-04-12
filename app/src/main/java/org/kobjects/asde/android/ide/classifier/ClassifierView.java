package org.kobjects.asde.android.ide.classifier;

import android.view.Menu;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.field.FieldFlow;
import org.kobjects.asde.android.ide.property.DeleteFlow;
import org.kobjects.asde.android.ide.function.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.property.PropertyListView;
import org.kobjects.asde.android.ide.property.RenameFlow;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.property.PropertyView;
import org.kobjects.asde.android.ide.property.ExpandListener;
import org.kobjects.asde.lang.classifier.AdapterType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.ClassType;
import org.kobjects.asde.lang.classifier.Trait;

import java.util.ArrayList;
import java.util.Collections;

public class ClassifierView extends PropertyView {

  PropertyView currentPropertyView;

  public final ExpandListener expandListener = new ExpandListener() {
    @Override
    public void notifyExpanding(PropertyView propertyView, boolean animated) {
      if (propertyView != currentPropertyView) {
        if (currentPropertyView != null) {
          currentPropertyView.setExpanded(false, animated);
        }
        currentPropertyView = propertyView;
        if (propertyView instanceof FunctionView) {
          mainActivity.programView.currentFunctionView = (FunctionView) propertyView;
        }
      }
    }
  };


  public ClassifierView(MainActivity mainActivity, Property symbol) {
    super(mainActivity, symbol);

    if (symbol.getStaticValue() instanceof Trait) {
      titleView.setTypeIndicator("trait", Colors.LIGHT_GREEN, false);
    } else if (symbol.getStaticValue() instanceof AdapterType){
      titleView.setTypeIndicator("impl", Colors.LIGHT_CYAN, false);
    } else {
      titleView.setTypeIndicator("class", Colors.LIGHT_BLUE, false);
    }

    titleView.setMoreClickListener(clicked -> {
      PopupMenu popupMenu = new PopupMenu(mainActivity, clicked);

      Menu addMenu = popupMenu.getMenu().addSubMenu("Add");

      if (symbol.getStaticValue() instanceof ClassType) {
        addMenu.add("Add Constant").setOnMenuItemClickListener(item -> {
          FieldFlow.createStaticProperty(mainActivity, (ClassType) symbol.getStaticValue(), false);
          return true;
        });
      }
      addMenu.add("Add Property").setOnMenuItemClickListener(item -> {
        FieldFlow.createInstanceProperty(mainActivity, (Classifier) symbol.getStaticValue());
        return true;
      });
      addMenu.add("Add Method").setOnMenuItemClickListener(item -> {
        FunctionSignatureFlow.createMethod(mainActivity, (Classifier) symbol.getStaticValue());
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
  public PropertyListView getContentView() {
    if (contentView == null) {
      contentView = new PropertyListView(mainActivity);
      addView(contentView);
    }
    return (PropertyListView) contentView;
  }


  @Override
  public void syncContent() {
    refresh();

    if (!expanded) {
      getContentView().synchronizeTo(Collections.emptyList(), expandListener, null);
      return;
    }

    Iterable<? extends Property> symbols;
    if (field.getStaticValue() instanceof Classifier) {
      symbols = ((Classifier) field.getStaticValue()).getAllProperties();
    } else {
      symbols = new ArrayList<>();
   //   symbols = ((Trait) symbol.getStaticValue()).propertyMap.values();
    }
    getContentView().synchronizeTo(symbols, expandListener, null);

  }

}
