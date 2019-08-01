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
      // Qualified needs to be sufficiently specific to disallow cross-type mismatches
      String qualifiedName = symbol.getName() + " " + symbol.getType();
      if (symbol.getValue() instanceof FunctionImplementation) {
        qualifiedName += Arrays.toString(((FunctionImplementation) symbol.getValue()).parameterNames);
      }
      SymbolView symbolView = nameViewMap.get(qualifiedName);
      if (symbolView != null) {
        symbolView.syncContent();
      } else {
        if (symbol.getValue() instanceof ClassImplementation) {
          ClassView classView = new ClassView(mainActivity, symbol);
          symbolView = classView;
          classView.addExpandListener(expandListener);
          if (matchedView == null && returnViewForSymbol != null) {
            matchedView = classView.getContentView().findViewBySymbol(returnViewForSymbol);
          }
        } else if (symbol.getValue() instanceof FunctionImplementation) {
          FunctionView functionView = new FunctionView(mainActivity, symbol);
          symbolView = functionView;
          functionView.addExpandListener(expandListener);
        } else {
          VariableView variableView = new VariableView(mainActivity, symbol);
          variableView.addExpandListener(expandListener);
          symbolView = variableView;
        }
      }
      int index = (symbolView instanceof VariableView) ? varCount++ : getChildCount();

      addView(symbolView, index);
      newNameViewMap.put(qualifiedName, symbolView);
      newSymbolViewMap.put(symbol, symbolView);
    }
    nameViewMap = newNameViewMap;
    symbolViewMap = newSymbolViewMap;

    return matchedView;
  }

}
