package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.classifier.ClassifierView;
import org.kobjects.asde.android.ide.field.FieldView;
import org.kobjects.asde.android.ide.property.ExpandListener;
import org.kobjects.asde.android.ide.property.PropertyView;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Classifier;

import java.util.HashMap;


public class PropertyListView extends ExpandableList {
  public HashMap<String, PropertyView> nameViewMap = new HashMap<>();
  HashMap<Property, PropertyView> symbolViewMap = new HashMap<>();
  private final MainActivity mainActivity;

  public PropertyListView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
  }

  public PropertyView findViewBySymbol(Property symbol) {
    return symbolViewMap.get(symbol);
  }

  /** 
   *
   * @param symbolList The new symbol list
   * @param expandListener Listener to add to newly created views
   * @param returnViewForSymbol Return the view for this symbol.
   * @return
   */
  public PropertyView synchronizeTo(Iterable<? extends Property> symbolList, ExpandListener expandListener, Property returnViewForSymbol) {

    System.out.println("########  synchronizeTo: " + symbolList);

    removeAllViews();
    int varCount = 0;

    PropertyView matchedView = null;

    HashMap<String, PropertyView> newNameViewMap = new HashMap<>();
    HashMap<Property, PropertyView> newSymbolViewMap = new HashMap<>();

    for (Property symbol : symbolList) {
      if (symbol == null) {
        continue;
      }
      // TODO: Does the qualified name need to be sufficiently specific to disallow cross-type
      // mismatches? Adding the type check will close changed function signatures.
      String qualifiedName = symbol.getName(); // + " " + symbol.getType();
      PropertyView propertyView = nameViewMap.get(qualifiedName);
      if (propertyView != null) {
        propertyView.syncContent();
      } else {
        if (symbol.getStaticValue() instanceof Classifier) {
          ClassifierView classifierView = new ClassifierView(mainActivity, symbol);
          propertyView = classifierView;
          classifierView.addExpandListener(expandListener);
          if (matchedView == null && returnViewForSymbol != null) {
            matchedView = classifierView.getContentView().findViewBySymbol(returnViewForSymbol);
          }
        } else if (symbol.getType() instanceof FunctionType) {
          FunctionView functionView = new FunctionView(mainActivity, symbol);
          propertyView = functionView;
          functionView.addExpandListener(expandListener);
        } else {
          FieldView variableView = new FieldView(mainActivity, symbol);
          variableView.addExpandListener(expandListener);
          propertyView = variableView;
        }
        if (symbol == returnViewForSymbol) {
          matchedView = propertyView;
        }
      }
      int index = (propertyView instanceof FieldView) ? varCount++ : getChildCount();

      addView(propertyView, index);
      newNameViewMap.put(qualifiedName, propertyView);
      newSymbolViewMap.put(symbol, propertyView);
    }
    nameViewMap = newNameViewMap;
    symbolViewMap = newSymbolViewMap;

    return matchedView;
  }

}
