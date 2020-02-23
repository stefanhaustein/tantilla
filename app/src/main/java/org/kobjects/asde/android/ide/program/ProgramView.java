package org.kobjects.asde.android.ide.program;

import android.view.View;
import android.view.ViewParent;

import org.kobjects.asde.android.ide.classifier.ClassifierView;
import org.kobjects.asde.android.ide.function.CodeLineView;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.symbol.ExpandListener;
import org.kobjects.asde.android.ide.symbol.SymbolListView;
import org.kobjects.asde.android.ide.symbol.SymbolView;
import org.kobjects.asde.lang.classifier.UserClass;
import org.kobjects.asde.lang.classifier.UserProperty;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.runtime.StartStopListener;

import java.util.Collections;

public class ProgramView extends SymbolListView {
  boolean expanded;
  public final FunctionView mainFunctionView;
  private final MainActivity mainActivity;
  private CodeLineView highlightedLine;
  public FunctionView currentFunctionView;
  public SymbolView currentSymbolView;
  int syncRequestCount;
  UserProperty expandOnSync;
  private final ExpandListener expandListener = new ExpandListener() {
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
  };

  public ProgramView(MainActivity context) {
    super(context);

    this.mainActivity = context;

    mainFunctionView = new FunctionView(context, mainActivity.program.main.getDeclaringSymbol());
    mainFunctionView.addExpandListener(expandListener);
    mainFunctionView.setExpanded(true, false);
    currentFunctionView = mainFunctionView;
    currentSymbolView = mainFunctionView;

    // Makes sense to handle this here because of shellControl -- just for mainControl
    // it would be easier to let RunControlView handle this.
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
    context.shell.mainControl.addStartStopListener(startStopRefresher);
    context.shell.shellControl.addStartStopListener(startStopRefresher);

    mainActivity.program.addSymbolChangeListener(symbol -> {
        expandOnSync = symbol;
        requestSynchronization();
      }
    );

    expanded = true;
    synchronize();
  }

  void expand(boolean expand) {
    if (this.expanded != expand) {
      this.expanded = expand;
      animateNextChanges();
      synchronize();
    }
  }

  public void refreshImpl() {
    for (SymbolView symbolView : nameViewMap.values()) {
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
          mainActivity.programTitleView.refresh();
          synchronize();
        }
      }
    }, 10);
  }

  void synchronize() {
    /*
    boolean isDefaultSaveLocation = program.reference.name.isEmpty();
    titleView.setTitle(
        (isDefaultSaveLocation ? "ASDE" : program.reference.name)
            + (program.legacyMode ? " (legacy mode)️" : "")
        + (mainActivity.isUnsaved() ? "*" : "")); */

    if (!expanded) {
      synchronizeTo(Collections.emptyList(), expandListener, null);
      return;
    }

    SymbolView expandView = synchronizeTo(mainActivity.program.mainModule.getUserProperties(), expandListener, expandOnSync);
    addView(mainFunctionView);

    mainFunctionView.syncContent();
    if (expandOnSync == mainActivity.program.main.getDeclaringSymbol()) {
      expandView = mainFunctionView;
    }

    if (expandView != null) {
      expandView.setExpanded(true, true);
    }
    expandOnSync = null;
  }

  public void highlightImpl(UserFunction function, int lineNumber) {
    unHighlight();
    SymbolView selectedView = selectImpl(function.getDeclaringSymbol());
    if (selectedView instanceof FunctionView) {
      FunctionView targetView = (FunctionView) selectedView;
      highlightedLine = targetView.findLineIndex(lineNumber);
      if (highlightedLine != null) {
        highlightedLine.setHighlighted(true);
        if (!expanded) {
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

  /**
   * Users should call console.edit(symbol) instead.
   */
  public SymbolView selectImpl(UserProperty symbol) {
    SymbolView targetView = null;
    if (currentSymbolView != null && currentSymbolView.symbol == symbol) {
      targetView = currentSymbolView;
    } else {
      for (int i = 0; i < getChildCount(); i++) {
        SymbolView childView = (SymbolView) getChildAt(i);
        if (childView.symbol == symbol) {
          targetView = childView;
          break;
        }
        if (childView instanceof ClassifierView) {
          ClassifierView classifierView = (ClassifierView) childView;
          UserClass classImplementation = (UserClass) classifierView.symbol.getStaticValue();
          UserProperty symbolFound = null;
          for (UserProperty descriptor : classImplementation.getUserProperties()) {
            if (descriptor == symbol) {
              symbolFound = descriptor;
              break;
            }
          }
          targetView = classifierView.getContentView().synchronizeTo(classImplementation.getUserProperties(), classifierView.expandListener, symbolFound);
          break;
        }
      }
    }

    if (targetView != null) {
      boolean expanded = targetView.expanded;
      if (!expanded) {
        targetView.setExpanded(true, true);
        targetView.requestChildFocus(targetView.titleView, targetView.titleView);
      }
    }

    return targetView;
  }
}
