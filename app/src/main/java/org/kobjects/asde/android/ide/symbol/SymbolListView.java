package org.kobjects.asde.android.ide.symbol;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.classifier.ClassifierView;
import org.kobjects.asde.android.ide.program.VariableView;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.symbol.StaticSymbol;
import org.kobjects.asde.lang.classifier.Classifier;

import java.util.Arrays;
import java.util.HashMap;


public class SymbolListView extends ExpandableList {
  public HashMap<String, SymbolView> nameViewMap = new HashMap<>();
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

    System.out.println("########  synchronizeTo: " + symbolList);

    removeAllViews();
    int varCount = 0;

    SymbolView matchedView = null;

    HashMap<String, SymbolView> newNameViewMap = new HashMap<>();
    HashMap<StaticSymbol, SymbolView> newSymbolViewMap = new HashMap<>();

    for (StaticSymbol symbol : symbolList) {
      if (symbol == null) {
        continue;
      }
      // Qualified needs to be sufficiently specific to disallow cross-type mismatches
      String qualifiedName = symbol.getName() + " " + symbol.getType();
      if (symbol.getStaticValue() instanceof UserFunction) {
        qualifiedName += Arrays.toString(((UserFunction) symbol.getStaticValue()).parameterNames);
      }
      SymbolView symbolView = nameViewMap.get(qualifiedName);
      if (symbolView != null) {
        symbolView.syncContent();
      } else {
        if (symbol.getStaticValue() instanceof Classifier) {
          ClassifierView classifierView = new ClassifierView(mainActivity, symbol);
          symbolView = classifierView;
          classifierView.addExpandListener(expandListener);
          if (matchedView == null && returnViewForSymbol != null) {
            matchedView = classifierView.getContentView().findViewBySymbol(returnViewForSymbol);
          }
        } else if (symbol.getStaticValue() instanceof UserFunction) {
          FunctionView functionView = new FunctionView(mainActivity, symbol);
          symbolView = functionView;
          functionView.addExpandListener(expandListener);
        } else {
          VariableView variableView = new VariableView(mainActivity, symbol);
          variableView.addExpandListener(expandListener);
          symbolView = variableView;
        }
        if (symbol == returnViewForSymbol) {
          matchedView = symbolView;
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
