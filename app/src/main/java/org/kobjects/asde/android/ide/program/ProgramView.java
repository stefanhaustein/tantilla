package org.kobjects.asde.android.ide.program;

import android.view.View;
import android.view.ViewParent;

import org.kobjects.asde.android.ide.classifier.ClassifierView;
import org.kobjects.asde.android.ide.function.CodeLineView;
import org.kobjects.asde.android.ide.function.FunctionView;
import org.kobjects.asde.android.ide.property.ExpandListener;
import org.kobjects.asde.android.ide.property.PropertyListView;
import org.kobjects.asde.android.ide.property.PropertyView;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.DeclaredBy;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.lang.classifier.module.Module;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.runtime.StartStopListener;

import java.util.Collections;

public class ProgramView extends PropertyListView {
  boolean expanded;
  private final MainActivity mainActivity;
  private CodeLineView highlightedLine;
  public FunctionView currentFunctionView;
  public PropertyView currentPropertyView;
  int syncRequestCount;
  Property expandOnSync;
  private final ExpandListener expandListener = new ExpandListener() {
    @Override
    public void notifyExpanding(PropertyView propertyView, boolean animated) {
      if (propertyView != currentPropertyView) {
        if (currentPropertyView != null) {
          currentPropertyView.setExpanded(false, animated);
        }
        currentPropertyView = propertyView;
        currentFunctionView = propertyView instanceof FunctionView ? (FunctionView) propertyView : null;
      }
    }
  };

  public ProgramView(MainActivity context) {
    super(context, context.program.mainModule);

    this.mainActivity = context;

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
    for (PropertyView propertyView : propertyViewMap.values()) {
      propertyView.refresh();
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
    classifier = mainActivity.program.mainModule;

    PropertyView expandView = synchronize(expanded, expandListener, expandOnSync);

    if (expandView != null) {
      expandView.setExpanded(true, true);
    }
    expandOnSync = null;
  }

  public void highlightImpl(UserFunction function, int lineNumber) {
    unHighlight();
    PropertyView selectedView = selectImpl(function.getDeclaringSymbol());
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
  public PropertyView selectImpl(Property symbol) {
    PropertyView targetView = null;
    if (currentPropertyView != null && currentPropertyView.property == symbol) {
      targetView = currentPropertyView;
    } else {
      for (int i = 0; i < getChildCount(); i++) {
        PropertyView childView = (PropertyView) getChildAt(i);
        if (childView.property == symbol) {
          targetView = childView;
          break;
        }
        if (childView instanceof ClassifierView) {
          ClassifierView classifierView = (ClassifierView) childView;
          Classifier classImplementation = (Classifier) classifierView.property.getStaticValue();
          Property symbolFound = null;
          for (Property descriptor : classImplementation.getProperties()) {
            if (descriptor == symbol) {
              symbolFound = descriptor;
              break;
            }
          }
          targetView = classifierView.getContentView().synchronize(true, classifierView.expandListener, symbolFound);
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
