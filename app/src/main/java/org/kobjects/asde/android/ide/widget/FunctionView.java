package org.kobjects.asde.android.ide.widget;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.kobjects.asde.R;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.lang.node.Statement;

import java.util.List;
import java.util.Map;

public class FunctionView extends LinearLayout {

    Activity context;
    TitleView titleView;
    CallableUnit callableUnit;
    IconButton startStopIcon;
    Interpreter interpreter;
    final Program program;

    public FunctionView(final Activity context, final Interpreter interpreter, String name, final CallableUnit callableUnit) {
        super(context);
        this.context = context;
        this.program = interpreter.program;
        setOrientation(VERTICAL);
        titleView = new TitleView(context);
        titleView.setTitle(name == null ? "Main Program" : (name + callableUnit.getType()));
        startStopIcon = new IconButton(context, R.drawable.baseline_play_arrow_black_24);
        titleView.addView(startStopIcon);

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
            public void programStopped() {
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
                    interpreter.stop();
                } else {
                    interpreter.runAsync(callableUnit);
                };
            }
        });


        addView(titleView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //setPadding(0, 0, 0, 16);
        this.callableUnit = callableUnit;

        sync();
    }


    public void put(int lineNumber, List<Statement> statementList) {

        callableUnit.code.put(lineNumber, statementList);
        sync();
    }

    public void sync() {
        int index = 1;
        for (Map.Entry<Integer, List<Statement>> entry : callableUnit.code.entrySet()) {
            CodeLineView codeLineView;
            if (index < getChildCount()) {
                codeLineView = (CodeLineView) getChildAt(index);
            } else {
                codeLineView = new CodeLineView(getContext());
                if (index % 2 == 0) {
                    codeLineView.setBackgroundColor(Colors.PRIMARY_LIGHT);
                }
                addView(codeLineView);
            }
            codeLineView.setLineNumber(entry.getKey());
            codeLineView.setStatement(entry.getValue());
            index++;
        }
        while (index < getChildCount()) {
            removeViewAt(getChildCount() - 1);
        }
        setVisibility(index > 1 ? VISIBLE : GONE);
    }

    void remove(int lineNumber) {
        callableUnit.code.remove(lineNumber);
        sync();
    }


}
