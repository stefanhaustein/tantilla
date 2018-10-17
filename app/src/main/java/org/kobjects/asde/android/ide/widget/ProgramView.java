package org.kobjects.asde.android.ide.widget;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Function;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.HashMap;
import java.util.Map;

public class ProgramView extends LinearLayout implements FunctionView.ExpandListener {

    private boolean expanded;
    private ExpandableList symbolList;
    public final FunctionView mainFunctionView;
    private final Program program;
    private final Interpreter mainInterpreter;
    private final Activity context;
    private final LineEditor lineEditor;

    private HashMap<String, View> symbolViewMap = new HashMap<>();
    public FunctionView currentFunctionView;

    public ProgramView(Activity context, Program program, Interpreter mainInterpreter, LineEditor lineEditor) {
        super(context);
        setOrientation(VERTICAL);

        this.context = context;
        this.program = program;
        this.mainInterpreter = mainInterpreter;
        this.lineEditor = lineEditor;

        TitleView titleView = new TitleView(context);
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

        mainFunctionView = new FunctionView(context, "Main", program.main, mainInterpreter, lineEditor);
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
                    FunctionView functionView = new FunctionView(context, name, (CallableUnit) symbol.value, mainInterpreter, lineEditor);
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
}
