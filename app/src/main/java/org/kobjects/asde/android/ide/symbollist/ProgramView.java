package org.kobjects.asde.android.ide.symbollist;

import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

import org.kobjects.asde.lang.event.ProgramChangeListener;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.event.StartStopListener;
import org.kobjects.asde.lang.GlobalSymbol;

import java.util.Arrays;
import java.util.HashMap;

public class ProgramView extends LinearLayout implements ExpandListener {

    private boolean expanded;
    private ExpandableList symbolList;
    public final FunctionView mainFunctionView;
    private final Program program;
    private final MainActivity mainActivity;
    private CodeLineView highlightedLine;
    TitleView titleView;
    private HashMap<String, SymbolView> symbolViewMap = new HashMap<>();
    public FunctionView currentFunctionView;
    public SymbolView currentSymbolView;
    int syncRequestCount;
    GlobalSymbol expandOnSync;

    public ProgramView(MainActivity context, Program program) {
        super(context);
        setOrientation(VERTICAL);

        this.mainActivity = context;
        this.program = program;

        titleView = new TitleView(context, context.colors.primary);
        addView(titleView);
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                expand(!expanded);
            }
        });

        symbolList = new ExpandableList(context);
        addView(symbolList);

        mainFunctionView = new FunctionView(context, program.mainSymbol);
        mainFunctionView.addExpandListener(this);
        mainFunctionView.setExpanded(true, false);
        currentFunctionView = mainFunctionView;
        currentSymbolView = mainFunctionView;

        StartStopListener startStopRefresher = new StartStopListener() {
            @Override
            public void programStarted() {
                refresh();
            }

            @Override
            public void programAborted(Exception cause) {
                refresh();
            }

            @Override
            public void programPaused() {
                refresh();
            }

            @Override
            public void programEnded() {
                refresh();
            }
        };

        context.shell.mainInterpreter.addStartStopListener(startStopRefresher);
        context.shell.shellInterpreter.addStartStopListener(startStopRefresher);

        program.addProgramChangeListener(new ProgramChangeListener() {
            @Override
            public void programChanged(Program program) {
                requestSynchronization();
            }

            @Override
            public void symbolChangedByUser(Program program, GlobalSymbol symbol) {
                expandOnSync = symbol;
                requestSynchronization();
            }
        });

        expanded = true;
        synchronize();
    }

    void expand(boolean expand) {
        if (this.expanded != expand) {
            this.expanded = expand;
            symbolList.animateNextChanges();
            synchronize();
        }
    }

    public void refreshImpl() {
        for (SymbolView symbolView : symbolViewMap.values()) {
            symbolView.refresh();
        }
    }

    public void refresh() {
        mainActivity.runOnUiThread(() -> refreshImpl());
    }

    public void requestSynchronization() {
        final int thisSyncRequest = ++syncRequestCount;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (thisSyncRequest == syncRequestCount) {
                    synchronize();
                }
            }
        }, 10);
    }

    void synchronize() {
        if (program.main.getLineCount() == 0 && (program.reference == null || "Unnamed".equals(program.reference.name))) {
            boolean empty = true;
            for (GlobalSymbol symbol : program.getSymbols()) {
                if (symbol.getScope() == GlobalSymbol.Scope.PERSISTENT) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                setVisibility(GONE);
                expanded = true;
                return;
            }
        }
        setVisibility(VISIBLE);

        titleView.setTitle(program.reference.name + (program.reference.urlWritable ? "" : "*"));
        symbolList.removeAllViews();
        if (!expanded) {
            symbolViewMap.clear();
            return;
        }
        int varCount = 0;

        SymbolView expandView = null;

        HashMap<String, SymbolView> newSymbolViewMap = new HashMap<>();
        for (GlobalSymbol symbol : program.getSymbols()) {
            if (symbol == null || symbol.getScope() != GlobalSymbol.Scope.PERSISTENT) {
                continue;
            }
            String name = symbol.getName();
            String qualifiedName = name + " " + symbol.getType();
            if (symbol.getValue() instanceof FunctionImplementation) {
                qualifiedName += Arrays.toString(((FunctionImplementation) symbol.getValue()).parameterNames);
            }
            SymbolView symbolView = symbolViewMap.get(qualifiedName);

            int index;
            if (symbol.getValue() instanceof FunctionImplementation) {
                if (!(symbolView instanceof FunctionView)) {
                    FunctionView functionView = new FunctionView(mainActivity, symbol);
                    symbolView = functionView;
                    functionView.addExpandListener(this);
                }
                index = symbolList.getChildCount();
            } else {
                if (symbolView instanceof VariableView) {
                    symbolView.syncContent();
                } else {
                    VariableView variableView = new VariableView(mainActivity, symbol);
                    variableView.addExpandListener(this);
                    symbolView = variableView;
                }
                index = varCount++;
            }
            symbolList.addView(symbolView, index);
            newSymbolViewMap.put(qualifiedName, symbolView);
            if (symbol == expandOnSync) {
                expandView = symbolView;
            }
        }
        symbolViewMap = newSymbolViewMap;
        symbolList.addView(mainFunctionView);

        mainFunctionView.syncContent();
        if (expandOnSync == program.mainSymbol) {
            expandView = mainFunctionView;
        }

        if (expandView != null) {
            expandView.setExpanded(true, true);
        }
        expandOnSync = null;

    }


    @Override
    public void notifyExpanding(SymbolView symbolView, boolean animated) {
        if (symbolView != currentSymbolView) {
            if (currentSymbolView != null) {
                currentSymbolView.setExpanded(false, animated);
            }
            currentSymbolView = symbolView;
            currentFunctionView = symbolView instanceof FunctionView ? (FunctionView) symbolView : mainFunctionView;
        }
    }

    public void highlight(FunctionImplementation function, int lineNumber) {
        unHighlight();
        FunctionView targetView = null;
        if (currentFunctionView != null && currentFunctionView.functionImplementation == function) {
            targetView = currentFunctionView;
        } else {
            for (int i = 0; i < symbolList.getChildCount(); i++) {
                if (symbolList.getChildAt(i) instanceof FunctionView) {
                    FunctionView functionView = (FunctionView) symbolList.getChildAt(i);
                    if (functionView.functionImplementation == function) {
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
                    // TODO(haustein): Build into expandableView?
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