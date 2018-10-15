package org.kobjects.asde.android.ide.widget;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.symbol.GlobalSymbol;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ProgramView extends LinearLayout implements FunctionView.ExpandListener {

    private boolean expanded;
    private ExpandableList symbolList;
    public final FunctionView mainFunctionView;
    private final Program program;
    private final TreeMap<String,FunctionView> functionViews = new TreeMap<>();
    private final Interpreter mainInterpreter;
    private final Activity context;
    private final LineEditor lineEditor;

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
        if (!expanded) {
            symbolList.removeAllViews();
            functionViews.clear();
            return;
        }

        if (mainFunctionView.getParent() == null) {
            symbolList.addView(mainFunctionView);
        }

        Set<String> removeViews = new HashSet<String>(functionViews.keySet());
        for (Map.Entry<String, GlobalSymbol> entry : program.getSymbolMap().entrySet()) {
            GlobalSymbol symbol = entry.getValue();
            if (symbol == null) {
                continue;
            }
            String name = entry.getKey();
            if (symbol.scope == GlobalSymbol.Scope.PERSISTENT && symbol.value instanceof CallableUnit) {
                removeViews.remove(name);
                if (!functionViews.containsKey(name)) {
                    FunctionView functionView = new FunctionView(context, name, (CallableUnit) symbol.value, mainInterpreter, lineEditor);
                    functionView.addExpandListener(this);
                    symbolList.addView(functionView, symbolList.getChildCount() - 1);
                    functionViews.put(name, functionView);
                  //  if (expandNew) {
                        //     functionView.setExpanded(true, false);
                 //   }
                }
            }
        }
        for (String remove : removeViews) {
            symbolList.removeView(functionViews.get(remove));
            functionViews.remove(remove);
        }

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
