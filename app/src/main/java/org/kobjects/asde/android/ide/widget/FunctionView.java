package org.kobjects.asde.android.ide.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import org.kobjects.asde.R;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.lang.node.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class FunctionView extends LinearLayout {

    Activity context;
    TitleView titleView;
    CallableUnit callableUnit;
    IconButton startStopIcon;
    IconButton foldIcon;
    Interpreter interpreter;
    final Program program;
    boolean collapsed;
    int fullHeight;
    ValueAnimator heightAnimator;
    List<ExpandListener> expandListeners = new ArrayList<>();

    public FunctionView(final Activity context, String name, final CallableUnit callableUnit, final Interpreter interpreter) {
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


        foldIcon = new IconButton(context, R.drawable.baseline_expand_less_black_24);
        titleView.addView(foldIcon);
        foldIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCollapsed(!collapsed);
            }
        });


        addView(titleView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT));
        //setPadding(0, 0, 0, 16);
        this.callableUnit = callableUnit;

        sync();
    }


    public void setCollapsed(boolean collapse) {
        if (collapsed == collapse) {
            return;
        }
        if (heightAnimator != null) {
            heightAnimator.cancel();
            heightAnimator = null;
        } else if (collapse) {
            fullHeight = getMeasuredHeight();
        }
        collapsed = collapse;
        if (collapse) {
            heightAnimator = ValueAnimator.ofInt(fullHeight, titleView.getHeight());
        } else {
            heightAnimator = ValueAnimator.ofInt(getHeight(), fullHeight);
            for (ExpandListener expandListener : expandListeners) {
                expandListener.notifyExpanding(this);
            }
        }
        heightAnimator.setTarget(this);
        heightAnimator.setDuration(300);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
                layoutParams.height = (Integer) animation.getAnimatedValue();
                setLayoutParams(layoutParams);
            }
        });
        heightAnimator.start();
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (collapsed) {

                } else {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
                    layoutParams.height = WRAP_CONTENT;
                }
                heightAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        foldIcon.animate().rotation(collapse ? 180 : 0).setDuration(300).start();
    }

    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
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

    public interface ExpandListener {
        void notifyExpanding(FunctionView functionView);
    }

}
