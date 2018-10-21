package org.kobjects.asde.android.ide;

import android.view.View;
import android.widget.LinearLayout;

import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FunctionView extends LinearLayout {
    public CallableUnit callableUnit;
    OnLongClickListener lineClickListener;
    SymbolTitleView titleView;
    ExpandableList contentView;
    boolean expanded;
    List<ExpandListener> expandListeners = new ArrayList<>();

    public FunctionView(final MainActivity context, String name, final CallableUnit callableUnit) {
        super(context);
        setOrientation(VERTICAL);
        this.callableUnit = callableUnit;

        boolean isMain = callableUnit == callableUnit.program.main;
        boolean isVoid = callableUnit.getType().getReturnType() == Types.VOID;
        int color = isMain ? Colors.PRIMARY_DARK : isVoid ? Colors.DEEP_PURPLE : Colors.BLUE;
        char c = isMain ? 'M' : isVoid ? 'S' : 'F';

        ArrayList<String> subtitles = new ArrayList<>();
        for (int i = 0; i < callableUnit.getType().getParameterCount(); i++) {
            subtitles.add(" " + callableUnit.parameterNames[i] + ": " + callableUnit.getType().getParameterType(i));
        }
        if (!isVoid) {
            subtitles.add("-> " + callableUnit.getType().getReturnType());
        }

        this.titleView = new SymbolTitleView(context, color, c, name, subtitles);
        addView(titleView);
        contentView = new ExpandableList(context);
        addView(contentView);


        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpanded(!expanded, true);
            }
        });

        /*
        if (interpreter != null) {
            startStopIcon = new IconButton(context, R.drawable.baseline_play_arrow_black_24);
            interpreter.addStartStopListener(new StartStopListener() {
                @Override
                public void programStarted() {
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            startStopIcon.setImageResource(R.drawable.baseline_stop_black_24);
                        }
                    });
                }

                @Override
                public void programTerminated() {
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            startStopIcon.setImageResource(R.drawable.baseline_play_arrow_black_24);
                        }
                    });
                }
            });

            startStopIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (interpreter.isRunning()) {
                        interpreter.terminate();
                    } else {
                        interpreter.start();
                    }
                    ;
                }
            });
            titleView.addView(startStopIcon);
        }
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpanded(!expanded, true);
            }
        });

        lineClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                lineEditor.edit(((CodeLineView) v).toString());
                return true;
            }
        };

        syncContent(); */



    }
/*
    public void setName(String name) {
        StringBuilder sb = new StringBuilder(name);
        if (callableUnit.getType().getReturnType() != Types.VOID) {
            sb.append("(" + callableUnit.getType().getParameterCount() + ")");
        }
        titleView.setTitle(sb.toString());
    }
*/

    public void put(int lineNumber, List<? extends Node> statementList) {
        callableUnit.setLine(lineNumber, new CodeLine(statementList));
        syncContent();
    }

    public void setExpanded(final boolean expand, boolean animated) {
        if (expanded == expand) {
            return;
        }
        if (animated) {
            contentView.animateNextChanges();
        }
        expanded = expand;
        for (ExpandListener expandListener : expandListeners) {
            expandListener.notifyExpanding(this, animated);
        }
        syncContent();
    }



    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
    }

    public void syncContent() {
        titleView.setBackgroundColor(callableUnit.errors.size() > 0 ? Colors.SECONDARY_LIGHT : expanded ? Colors.PRIMARY_LIGHT : 0);
        if (!expanded) {
            contentView.removeAllViews();
            return;
        }
        int index = 0;
        for (Map.Entry<Integer, CodeLine> entry : callableUnit.entrySet()) {
            CodeLineView codeLineView;
            if (index < contentView.getChildCount()) {
                codeLineView = (CodeLineView) contentView.getChildAt(index);
            } else {
                codeLineView = new CodeLineView(getContext());
                if (index % 2 == 1) {
                    codeLineView.setBackgroundColor(Colors.PRIMARY_LIGHT);
                }
                contentView.addView(codeLineView);
            }
            codeLineView.setLineNumber(entry.getKey());
            codeLineView.setCodeLine(entry.getValue(), callableUnit.errors);
            codeLineView.setOnLongClickListener(lineClickListener);
            index++;
        }
        while (index < contentView.getChildCount()) {
            contentView.removeViewAt(contentView.getChildCount() - 1);
        }
    }

    void remove(int lineNumber) {
        callableUnit.setLine(lineNumber, null);
        syncContent();
    }


    public interface ExpandListener {
        void notifyExpanding(FunctionView expandableView, boolean animated);
    }

}
