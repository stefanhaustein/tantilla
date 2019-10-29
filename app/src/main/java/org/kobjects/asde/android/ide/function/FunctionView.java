package org.kobjects.asde.android.ide.function;

import android.app.AlertDialog;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.symbol.DeleteFlow;
import org.kobjects.asde.android.ide.symbol.RenameFlow;
import org.kobjects.asde.android.ide.symbol.SymbolView;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;

import java.util.ArrayList;

public class FunctionView extends SymbolView {
  public FunctionImplementation functionImplementation;

  private Selection selection;

  public FunctionView(final MainActivity mainActivity, StaticSymbol symbol) {
    super(mainActivity, symbol);
    this.functionImplementation = (FunctionImplementation) symbol.getValue();

    boolean isMain = functionImplementation == functionImplementation.program.main;
    boolean isVoid = functionImplementation.getType().getReturnType() == Types.VOID;
    boolean isMethod = functionImplementation.isMethod();

    titleView.setTypeIndicator(
        isMain ? 'M' : isMethod ? 'm' : isVoid ? 'S' : 'F',
        isMain ? Colors.PRIMARY_FILTER : Colors.DARK_PURPLE);

    titleView.setMoreClickListener(clicked -> {
      PopupMenu popup = new PopupMenu(mainActivity, clicked);
      Menu menu = popup.getMenu();
      if (!isMain) {
        menu.add("Rename").setOnMenuItemClickListener(item -> {
          RenameFlow.start(mainActivity, symbol);
          return true;
        });
        menu.add("Change Signature").setOnMenuItemClickListener(item -> {
          FunctionSignatureFlow.changeSignature(mainActivity, symbol, functionImplementation);
          return true;
        });
      }
      MenuItem renumberMenuItem = menu.add("Renumber");
      int[] lineNumberRange = functionImplementation.getLineNumberRange();
      if (functionImplementation.getLineNumberRange() == null) {
        renumberMenuItem.setEnabled(false);
      } else {
        renumberMenuItem.setOnMenuItemClickListener(item -> {
          RenumberFlow.start(mainActivity, symbol, lineNumberRange[0], lineNumberRange[1]);
          return true;
        });
      }
      if (!isMain) {
        menu.add("Delete").setOnMenuItemClickListener(item -> {
          DeleteFlow.start(mainActivity, symbol);
          return true;
        });
      }
      popup.show();
    });


    ArrayList<String> subtitles = new ArrayList<>();
    for (int i = 0; i < functionImplementation.getType().getParameterCount(); i++) {
      subtitles.add(" " + functionImplementation.parameterNames[i] + ": " + functionImplementation.getType().getParameterType(i));
    }
    if (!isVoid) {
      subtitles.add("-> " + functionImplementation.getType().getReturnType());
    }
    titleView.setSubtitles(subtitles);
  }

  int syncedTo;

  public void syncContent() {
    refresh();

    LinearLayout codeView = getContentView();

    if (!expanded) {
      // Only remove if we own the view.
      if (codeView == contentView) {
        codeView.removeAllViews();
      }
      syncedTo = 0;
      return;
    }
    int index = 0;

    if (codeView.getChildCount() > 0 && !(codeView.getChildAt(0) instanceof CodeLineView)) {
      codeView.removeAllViews();
    }

    int updated = 0;

    for (CodeLine codeLine : functionImplementation.allLines()) {
      if (index >= syncedTo) {
        updated++;
        CodeLineView codeLineView;
        if (index < codeView.getChildCount()) {
          codeLineView = (CodeLineView) codeView.getChildAt(index);
        } else {
          codeLineView = new CodeLineView(mainActivity, index % 2 == 1);
          codeView.addView(codeLineView);
        }
        codeLineView.setCodeLine(codeLine, symbol.getErrors());
      }
      index++;
      if (updated > 8) {
        syncedTo += updated;
        post(this::syncContent);
        return;
      }
    }

    syncedTo = 0;

    while (index < codeView.getChildCount()) {
      codeView.removeViewAt(codeView.getChildCount() - 1);
    }

    codeView.setClickable(true);
    codeView.setOnTouchListener(new TouchListener());
  }


  public CodeLineView findLineIndex(int lineNumber) {
    LinearLayout codeView = getContentView();
    for (int i = 0; i < codeView.getChildCount(); i++) {
      CodeLineView codeLineView = (CodeLineView) codeView.getChildAt(i);
      if (codeLineView.lineNumber == lineNumber) {
        return codeLineView;
      }
    }
    return null;
  }

  CodeLineView getCodeLineView(int index) {
    return (CodeLineView) getContentView().getChildAt(index);
  }

  private int findLineIndex(MotionEvent event) {
    LinearLayout codeView = getContentView();
    int rawX = (int) event.getRawX();
    int rawY = (int) event.getRawY();
    System.out.println("ev: " + event + " rx: " + rawX + " ry: " + rawY);
    int[] location = new int[2];
    for (int i = 0; i < codeView.getChildCount(); i++) {
      CodeLineView view = getCodeLineView(i);
      view.getLocationOnScreen(location);

      if (rawX > location[0] && rawX < location[0] + view.getWidth()
          && rawY > location[1] && rawY < location[1] + view.getHeight()) {
        return i;
      }
    }
    return -1;
  }


  private void startSelection(MotionEvent e) {
    int i = findLineIndex(e);
    if (i != -1) {
      selection = new Selection(i);
      moveSelection(e);
    }
  }

  private void moveSelection(MotionEvent e) {
    int index = findLineIndex(e);
    if (index != -1) {
      LinearLayout contentView = getContentView();
      for (int i = selection.getStartIndex(); i < selection.getEndIndex(); i++) {
        contentView.getChildAt(i).setSelected(false);
      }
      selection.setLastSelectedIndex(index);
      for (int i = selection.getStartIndex(); i < selection.getEndIndex(); i++) {
        contentView.getChildAt(i).setSelected(true);
      }
    }
  }

  private void endSelection() {
    if (selection == null) {
      return;
    }
    ViewGroup contentView = getContentView();
    CodeLineView lastSelected = (CodeLineView) contentView.getChildAt(selection.lastSelectedIndex);
    PopupMenu popup = new PopupMenu(mainActivity, lastSelected);

    popup.setOnDismissListener((a) -> {
      for (int i = selection.getStartIndex(); i < selection.getEndIndex(); i++) {
        contentView.getChildAt(i).setSelected(false);
      }
    });
    Menu menu = popup.getMenu();

    MenuItem editItem = menu.add("Edit");
    if (selection.firstSelectedIndex == selection.lastSelectedIndex) {
      editItem.setOnMenuItemClickListener(item -> {
        mainActivity.controlView.codeEditText.setText(lastSelected.toString());
        return true;
      });
    } else {
      editItem.setEnabled(false);
    }


    menu.add("Copy").setOnMenuItemClickListener(item -> {
      mainActivity.copyBuffer.clear();
      for (int i = selection.getStartIndex(); i < selection.getEndIndex(); i++) {
        CodeLineView codeLineView = getCodeLineView(i);
        mainActivity.copyBuffer.put(getCodeLineView(i).lineNumber, codeLineView.statementView.getText().toString());
      }
      return true;
    });

    if (mainActivity.copyBuffer.size() == 0) {
      menu.add("Paste").setEnabled(false);
    } else {
      Menu insertMenu = menu.addSubMenu("Paste");
      insertMenu.add("Before").setOnMenuItemClickListener(item -> {
        int targetLine = selection.getStartIndex() == 0 ? 1 : getCodeLineView(selection.getStartIndex() - 1).lineNumber + 1;
        InsertFlow.start(mainActivity, symbol, targetLine);
        return true;
      });
      insertMenu.add("After").setOnMenuItemClickListener((item -> {
        int targetLine = getCodeLineView(selection.getEndIndex() - 1).lineNumber + 1;
        InsertFlow.start(mainActivity, symbol, targetLine);
        return true;
      }));
    }


    menu.add("Renumber").setOnMenuItemClickListener(item -> {
      RenumberFlow.start(mainActivity, symbol, getCodeLineView(selection.getStartIndex()).lineNumber, getCodeLineView(selection.getEndIndex() - 1).lineNumber);
      return true;
    });
    menu.add("Delete").setOnMenuItemClickListener(item -> {
      selection.startDeleteFlow();
      return true;
    });

    popup.show();
  }


  class Selection {
    final int firstSelectedIndex;
    int lastSelectedIndex;

    Selection(int firstSelectedIndex) {
      this.firstSelectedIndex = lastSelectedIndex = firstSelectedIndex;
    }

    void setLastSelectedIndex(int lastSelectedIndex) {
      this.lastSelectedIndex = lastSelectedIndex;
    }

    public int getStartIndex() {
      return Math.min(firstSelectedIndex, lastSelectedIndex);
    }

    /**
     * Exclusive. Note that line number ranges are inclusive.
     */
    public int getEndIndex() {
      return Math.max(firstSelectedIndex, lastSelectedIndex) + 1;
    }


    void startDeleteFlow() {
      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
      alertBuilder.setTitle("Confirm Delete");
      if (firstSelectedIndex == lastSelectedIndex) {
        alertBuilder.setMessage("Delete line " + getCodeLineView(firstSelectedIndex).lineNumber + "'?");
      } else {
        alertBuilder.setMessage("Delete lines " + getCodeLineView(getStartIndex()).lineNumber + " - " +
            getCodeLineView(getEndIndex() - 1).lineNumber + "?");
      }
      alertBuilder.setNegativeButton("Cancel", null);
      alertBuilder.setPositiveButton("Delete", (a, b) -> {
        for (int i = getEndIndex() - 1; i >= getStartIndex(); i--) {
          functionImplementation.deleteLine(getCodeLineView(i).lineNumber);
          getContentView().removeViewAt(i);
        }
      });
      alertBuilder.show();
    }
  }


  class TouchListener implements OnTouchListener {
    boolean dragMode = false;

    GestureDetector gestureDetector = new GestureDetector(mainActivity, new GestureDetector.SimpleOnGestureListener() {
      @Override
      public void onLongPress(MotionEvent e) {
        dragMode = true;
        getContentView().requestDisallowInterceptTouchEvent(true);
        startSelection(e);
      }
    });

    @Override
    public boolean onTouch(View view, MotionEvent event) {
      gestureDetector.setIsLongpressEnabled(true);
      boolean result = gestureDetector.onTouchEvent(event);

      if (!dragMode) {
        return result;
      }

      if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
        dragMode = false;
        endSelection();
      } else {
        moveSelection(event);
      }

      return true;
    }
  }

}
