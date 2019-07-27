package org.kobjects.asde.android.ide.symbollist;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.GlobalSymbol;
import org.kobjects.asde.lang.StaticSymbol;

import java.util.Arrays;
import java.util.HashMap;


public class SymbolListView extends ExpandableList {
  HashMap<String, SymbolView> nameViewMap = new HashMap<>();
  HashMap<StaticSymbol, SymbolView> symbolViewMap = new HashMap<>();
  private final MainActivity mainActivity;

  public SymbolListView(MainActivity mainActivity) {
    super(mainActivity);
    this.mainActivity = mainActivity;
  }

  public SymbolView findViewBySymbol(StaticSymbol symbol) {
    return symbolViewMap.get(symbol);
  }

  /** 
   *
   * @param symbolList The new symbol list
   * @param expandListener Listener to add to newly created views
   * @param returnViewForSymbol Return the view for this symbol.
   * @return
   */
  public SymbolView synchronizeTo(Iterable<? extends StaticSymbol> symbolList, ExpandListener expandListener, StaticSymbol returnViewForSymbol) {

    removeAllViews();
    int varCount = 0;

    SymbolView matchedView = null;

    HashMap<String, SymbolView> newNameViewMap = new HashMap<>();
    HashMap<StaticSymbol, SymbolView> newSymbolViewMap = new HashMap<>();

    for (StaticSymbol symbol : symbolList) {
      if (symbol == null || symbol.getScope() != GlobalSymbol.Scope.PERSISTENT) {
        continue;
      }
      String name = symbol.getName();
      String qualifiedName = name + " " + symbol.getType();
      if (symbol.getValue() instanceof FunctionImplementation) {
        qualifiedName += Arrays.toString(((FunctionImplementation) symbol.getValue()).parameterNames);
      }
      SymbolView symbolView = nameViewMap.get(qualifiedName);
      SymbolView original = symbolView;
      int index;
      if (symbol.getValue() instanceof ClassImplementation) {
        ClassView classView;
        if (symbolView instanceof ClassView) {
          classView = (ClassView) symbolView;
        } else {
          classView = new ClassView(mainActivity, symbol);
          symbolView = classView;
          classView.addExpandListener(expandListener);
        }
        if (matchedView == null && returnViewForSymbol != null) {
          matchedView = classView.getContentView().findViewBySymbol(returnViewForSymbol);
        }
        index = getChildCount();
      } else if (symbol.getValue() instanceof FunctionImplementation) {
        if (!(symbolView instanceof FunctionView)) {
          FunctionView functionView = new FunctionView(mainActivity, symbol);
          symbolView = functionView;
          functionView.addExpandListener(expandListener);
        }
        index = getChildCount();
      } else {
        if (!(symbolView instanceof VariableView)) {
          VariableView variableView = new VariableView(mainActivity, symbol);
          variableView.addExpandListener(expandListener);
          symbolView = variableView;
        }
        index = varCount++;
      }
      addView(symbolView, index);
      newNameViewMap.put(qualifiedName, symbolView);
      newSymbolViewMap.put(symbol, symbolView);
      if (symbolView == original) {
        symbolView.syncContent();
      }
    }
    nameViewMap = newNameViewMap;
    symbolViewMap = newSymbolViewMap;

    return matchedView;
  }

}
