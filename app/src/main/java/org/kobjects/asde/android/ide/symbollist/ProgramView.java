package org.kobjects.asde.android.ide.symbollist;

import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainMenu;
import org.kobjects.asde.lang.ClassImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.event.ProgramChangeListener;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.TitleView;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.Program;
import org.kobjects.asde.lang.event.StartStopListener;

import java.util.Collections;

public class ProgramView extends LinearLayout {
  private boolean expanded;
  private SymbolListView symbolList;
  public final FunctionView mainFunctionView;
  private final Program program;
  private final MainActivity mainActivity;
  private CodeLineView highlightedLine;
  TitleView titleView;
  public FunctionView currentFunctionView;
  public SymbolView currentSymbolView;
  int syncRequestCount;
  StaticSymbol expandOnSync;
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

  public ProgramView(MainActivity context, Program program) {
    super(context);
    setOrientation(VERTICAL);

    this.mainActivity = context;
    this.program = program;

    titleView = new TitleView(context, Colors.PRIMARY_FILTER, view -> {
      MainMenu.show(mainActivity, view);
    });
    addView(titleView);
    titleView.setOnClickListener(view -> {
        if (mainActivity.sharedCodeViewAvailable()) {
          mainActivity.outputView.syncContent();
        } else {
          expand(!expanded);
      }
    });

    symbolList = new SymbolListView(context);
    addView(symbolList);

    mainFunctionView = new FunctionView(context, program.mainSymbol);
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

    program.addProgramChangeListener(new ProgramChangeListener() {
      @Override
      public void programChanged(Program program) {
        requestSynchronization();
      }

      @Override
      public void symbolChangedByUser(Program program, StaticSymbol symbol) {
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
    for (SymbolView symbolView : symbolList.nameViewMap.values()) {
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
    titleView.setTitle(
        (program.reference.name.equals("Unnamed") ? "ASDE" : program.reference.name)
        + (program.hasUnsavedChanges ? "*" : ""));

    if (!expanded) {
      symbolList.synchronizeTo(Collections.emptyList(), expandListener, null);
      return;
    }

    SymbolView expandView = symbolList.synchronizeTo(program.getSymbols(), expandListener, expandOnSync);
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
        } else if (symbolList.getChildAt(i) instanceof ClassView) {
          ClassView classView = (ClassView) symbolList.getChildAt(i);
          ClassImplementation classImplementation = (ClassImplementation) classView.symbol.getValue();
          StaticSymbol symbolFound = null;
          for (ClassImplementation.ClassPropertyDescriptor descriptor : classImplementation.propertyMap.values()) {
            if (descriptor.getValue() == function) {
              symbolFound = descriptor;
              break;
            }
          }
          targetView = (FunctionView) classView.getContentView().synchronizeTo(classImplementation.propertyMap.values(), classView.expandListener, symbolFound);
          break;
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
