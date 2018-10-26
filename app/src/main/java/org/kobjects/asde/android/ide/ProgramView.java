package org.kobjects.asde.android.ide;

import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.HashMap;
import java.util.Map;

public class ProgramView extends LinearLayout implements FunctionView.ExpandListener {

    private boolean expanded;
    private ExpandableList symbolList;
    public final FunctionView mainFunctionView;
    private final Program program;
    private final MainActivity context;
    private CodeLineView highlightedLine;
    TitleView titleView;
    private HashMap<String, View> symbolViewMap = new HashMap<>();
    public FunctionView currentFunctionView;

    public ProgramView(MainActivity context, Program program) {
        super(context);
        setOrientation(VERTICAL);

        this.context = context;
        this.program = program;

        titleView = new TitleView(context, Colors.PRIMARY);
        titleView.setTitle("Unnamed Program");
        addView(titleView);
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(!expanded);
            }
        });

        symbolList = new ExpandableList(context);
        addView(symbolList);

        mainFunctionView = new FunctionView(context, "Main", program.main);
        mainFunctionView.addExpandListener(this);
        mainFunctionView.setVisibility(View.GONE);
        currentFunctionView = mainFunctionView;

        expanded = true;
        sync();
    }

    void expand(boolean expand) {
        if (this.expanded != expand) {
            this.expanded = expand;
            symbolList.animateNextChanges();
            sync();
        }
    }

    void setName(String name) {
        titleView.setTitle(name.equals("Scratch") ? "Unnamed Program" : name);
    }

    public void sync() {
        symbolList.removeAllViews();
        if (!expanded) {
            symbolViewMap.clear();
            return;
        }

        int varCount = 0;

        HashMap<String, View> newSymbolViewMap = new HashMap<>();
        for (Map.Entry<String, GlobalSymbol> entry : program.getSymbolMap().entrySet()) {
            GlobalSymbol symbol = entry.getValue();
            if (symbol == null || symbol.scope != GlobalSymbol.Scope.PERSISTENT) {
                continue;
            }
            String name = entry.getKey();
            View symbolView = symbolViewMap.get(name);
            int index;
            if (symbol.value instanceof CallableUnit) {
                if (!(symbolView instanceof FunctionView)) {
                    FunctionView functionView = new FunctionView(context, name, (CallableUnit) symbol.value);
                    symbolView = functionView;
                    functionView.addExpandListener(this);

                  //  if (expandNew) {
                        //     functionView.setExpanded(true, false);
                 //   }
                }
                index = symbolList.getChildCount();
            } else {
                if (!(symbolView instanceof VariableView)) {
                    VariableView variableView = new VariableView(context, name, symbol);
                    symbolView = variableView;
                }
                index = varCount++;
            }
            symbolList.addView(symbolView, index);
            newSymbolViewMap.put(name, symbolView);
        }
        symbolViewMap = newSymbolViewMap;
        symbolList.addView(mainFunctionView);

        if (program.main.getLineCount() > 0 && mainFunctionView.getVisibility() == View.GONE) {
            mainFunctionView.setVisibility(View.VISIBLE);
            //mainFunctionView.setExpanded(true, false);
        }

        mainFunctionView.syncContent();
    }


    @Override
    public void notifyExpanding(FunctionView functionView, boolean animated) {

            if (functionView != currentFunctionView && functionView instanceof FunctionView) {
                if (currentFunctionView != null) {
                    currentFunctionView.setExpanded(false, animated);
                }
                currentFunctionView = (FunctionView) functionView;
            }
           }

    public void trace(CallableUnit function, int lineNumber) {
        unHighlight();
        FunctionView targetView = null;
        if (currentFunctionView != null && currentFunctionView.callableUnit == function) {
            targetView = currentFunctionView;
        } else {
            for (int i = 0; i < symbolList.getChildCount(); i++) {
                if (symbolList.getChildAt(i) instanceof FunctionView) {
                    FunctionView functionView = (FunctionView) symbolList.getChildAt(i);
                    if (functionView.callableUnit == function) {
                        targetView = functionView;
                        break;
                    }
                }
            }
        }
        if (targetView != null) {
            boolean exapnded = targetView.expanded;
            if (!exapnded) {
                targetView.setExpanded(true, true);
                targetView.requestChildFocus(targetView.titleView, targetView.titleView);
            }
            highlightedLine = targetView.findLine(lineNumber);
            if (highlightedLine != null) {
                highlightedLine.setHighlighted(true);
                if (!exapnded) {
                    // TODO(hausteint): Build into expandableView?
                    final View toFocus = highlightedLine;
                    postDelayed(() -> {
                        ViewParent parent = toFocus.getParent();
                        if (parent != null) {
                            parent.requestChildFocus(toFocus, toFocus);
                        }
                    }, 400);
                } else {
                    highlightedLine.getParent().requestChildFocus(highlightedLine, highlightedLine);
                }
            }
        }
    }

    public void unHighlight() {
        if (highlightedLine != null) {
            highlightedLine.setHighlighted(false);
            highlightedLine = null;
        }
    }
}
