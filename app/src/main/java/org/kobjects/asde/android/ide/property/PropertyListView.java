package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.classifier.ClassifierView;
import org.kobjects.asde.android.ide.field.FieldView;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.trait.Trait;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.MetaType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;


public class PropertyListView extends ExpandableList {
  protected HashMap<Property, PropertyView> propertyViewMap = new HashMap<>();
  private final MainActivity mainActivity;
  protected Classifier classifier;

  public PropertyListView(MainActivity mainActivity, Classifier classifier) {
    super(mainActivity);
    this.mainActivity = mainActivity;
    this.classifier = classifier;
  }

  public PropertyView findViewBySymbol(Property symbol) {
    return propertyViewMap.get(symbol);
  }


  /** 
   * @param expandListener Listener to add to newly created views
   * @param returnViewForSymbol Return the view for this symbol.
   * @return
   */
  public PropertyView synchronize(boolean expanded, ExpandListener expandListener, Property returnViewForSymbol) {
    removeAllViews();
    //int varCount = 0;

    if (!expanded) {
      return null;
    }

    PropertyView matchedView = null;

    HashMap<Property, PropertyView> newPropertyViewMap = new HashMap<>();

    for (Property property : classifier.getProperties()) {
      PropertyView propertyView = propertyViewMap.get(property);
      if (propertyView != null) {
        propertyView.syncContent();
      } else {
        if (property.getStaticValue() instanceof Classifier) {
          ClassifierView classifierView = new ClassifierView(mainActivity, property);
          propertyView = classifierView;
          classifierView.addExpandListener(expandListener);
          if (matchedView == null && returnViewForSymbol != null) {
            matchedView = classifierView.getContentView().findViewBySymbol(returnViewForSymbol);
          }
        } else if (property.getType() instanceof FunctionType) {
          FunctionView functionView = new FunctionView(mainActivity, property);
          propertyView = functionView;
          functionView.addExpandListener(expandListener);
        } else {
          FieldView variableView = new FieldView(mainActivity, property);
          variableView.addExpandListener(expandListener);
          propertyView = variableView;
        }
        if (property == returnViewForSymbol) {
          matchedView = propertyView;
        }
      }
      addView(propertyView);
      newPropertyViewMap.put(property, propertyView);
    }
    propertyViewMap = newPropertyViewMap;

    return matchedView;
  }

}
