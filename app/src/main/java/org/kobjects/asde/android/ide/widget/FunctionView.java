package org.kobjects.asde.android.ide.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.kobjects.asde.R;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.StartStopListener;
import org.kobjects.asde.lang.Types;
import org.kobjects.asde.lang.node.Statement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class FunctionView extends LinearLayout {

    Activity context;
    TitleView titleView;
    public CallableUnit callableUnit;
    IconButton startStopIcon;
    Interpreter interpreter;
    final Program program;
    boolean expanded;
    int fullHeight;
    ValueAnimator heightAnimator;
    List<ExpandListener> expandListeners = new ArrayList<>();
    LinearLayout contentView;

    public FunctionView(final Activity context, String name, final CallableUnit callableUnit, final Interpreter interpreter) {
        super(context);
        this.context = context;
        this.program = interpreter.program;
        this.callableUnit = callableUnit;
        this.interpreter = interpreter;
        setOrientation(VERTICAL);
        titleView = new TitleView(context);
        setName(name);

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
        titleView.addView(startStopIcon);
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setExpanded(!expanded, true);
            }
        });
        addView(titleView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT));

        contentView = new LinearLayout(context);
        contentView.setOrientation(VERTICAL);
        addView(contentView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT));
        sync();
    }

    public void setName(String name) {
        StringBuilder sb = new StringBuilder(name);
        if (callableUnit.getType().getReturnType() != Types.VOID) {
            sb.append("(" + callableUnit.getType().getParameterCount() + ")");
        }
        titleView.setTitle(sb.toString());
    }

    public void setExpanded(final boolean expand, boolean animated) {
        if (expanded == expand) {
            return;
        }
        expanded = expand;

        if (expand) {
            sync();
            for (ExpandListener expandListener : expandListeners) {
                expandListener.notifyExpanding(this, animated);
            }
        } else {
            fullHeight = contentView.getMeasuredHeight();
        }

        if (heightAnimator != null) {
            heightAnimator.cancel();
            heightAnimator = null;
        }

        heightAnimator = ValueAnimator.ofInt(contentView.getHeight(), expanded ? fullHeight : 0);
        heightAnimator.setTarget(this);
        heightAnimator.setDuration(300);
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentView.getLayoutParams();
                layoutParams.height = (Integer) animation.getAnimatedValue();
                contentView.setLayoutParams(layoutParams);
            }
        });
        heightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                heightAnimator = null;
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) contentView.getLayoutParams();
                layoutParams.height = WRAP_CONTENT;
                contentView.setLayoutParams(layoutParams);
                if (!expanded) {
                   sync();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        heightAnimator.start();

        if (!animated) {
            heightAnimator.end();
        }
    }


    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
    }

    public void put(int lineNumber, List<Statement> statementList) {
        callableUnit.setLine(lineNumber, new CodeLine(statementList));
        sync();
    }

    public void sync() {
        titleView.setBackgroundColor(callableUnit.errors.size() > 0 ? Colors.SECONDARY : Colors.PRIMARY);
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
            codeLineView.setCodeLine(entry.getValue());
            index++;
        }
        while (index < contentView.getChildCount()) {
            contentView.removeViewAt(contentView.getChildCount() - 1);
        }
    }

    void remove(int lineNumber) {
        callableUnit.setLine(lineNumber, null);
        sync();
    }

    public interface ExpandListener {
        void notifyExpanding(FunctionView functionView, boolean animated);
    }

}
