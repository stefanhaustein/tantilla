package org.kobjects.asde.android.ide.property;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.classifier.ClassifierView;
import org.kobjects.asde.android.ide.field.FieldView;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.Trait;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.type.MetaType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;


public class PropertyListView extends ExpandableList {
  protected HashMap<Property, PropertyView> propertyViewMap = new HashMap<>();
  private final MainActivity mainActivity;

  public PropertyListView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
  }

  public PropertyView findViewBySymbol(Property symbol) {
    return propertyViewMap.get(symbol);
  }

  private static int order(Property property) {
    if (property.getType() instanceof MetaType && ((MetaType) property.getType()).getWrapped() instanceof Classifier) {
      Classifier classifier = (Classifier) ((MetaType) property.getType()).getWrapped();
      if (classifier instanceof Trait) {
        return 4;
      }
      return 5;
    }
    if (property.isInstanceField()) {
      return property.isMutable() ? 7 : 8;
    }
    if (property.getType() instanceof FunctionType) {
      FunctionType functionType = (FunctionType) property.getType();
      return functionType.getParameterCount() > 0  && functionType.getParameter(0).getName().equals("self") ? 9 : property.getName().equals("main")? 10 : 3;
    }
    return property.isMutable() ? 2 : 1;
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
    //int varCount = 0;

    PropertyView matchedView = null;

    HashMap<Property, PropertyView> newPropertyViewMap = new HashMap<>();

    TreeSet<Property> sorted = new TreeSet<>(new Comparator<Property>() {
      @Override
      public int compare(Property p1, Property p2) {
        int cmp = Integer.compare(order(p1), order(p2));
        return cmp == 0 ? p1.getName().compareTo(p2.getName()) : cmp;
      }
    });
    for (Property property : symbolList) {
      sorted.add(property);
    }


    for (Property property : sorted) {
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
