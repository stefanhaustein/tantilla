package org.kobjects.asde.android.ide.symbollist;

import android.app.AlertDialog;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;

import java.util.ArrayList;
import java.util.Map;

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
        isMain ? Colors.PRIMARY_FILTER : Colors.PURPLE);

    if (!isMain) {
      titleView.setMoreClickListener(clicked -> {
        PopupMenu popupMenu = new PopupMenu(mainActivity, clicked);
        popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
          RenameFlow.start(mainActivity, symbol);
          return true;
        });
        popupMenu.getMenu().add("Change Signature").setOnMenuItemClickListener(item -> {
          FunctionSignatureFlow.changeSignature(mainActivity, symbol, functionImplementation);
          return true;
        });
        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
          DeleteFlow.start(mainActivity, symbol);
          return true;
        });
        popupMenu.show();
      });
    }

    ArrayList<String> subtitles = new ArrayList<>();
    for (int i = 0; i < functionImplementation.getType().getParameterCount(); i++) {
      subtitles.add(" " + functionImplementation.parameterNames[i] + ": " + functionImplementation.getType().getParameterType(i));
    }
    if (!isVoid) {
      subtitles.add("-> " + functionImplementation.getType().getReturnType());
    }

    titleView.setSubtitles(subtitles);

  }


  public void syncContent() {
    refresh();

    LinearLayout codeView = getContentView();

    if (!expanded) {
      // Only remove if we own the view.
      if (codeView == contentView){
        codeView.removeAllViews();
      }
      return;
    }
    int index = 0;

    if (codeView.getChildCount() > 0 && !(codeView.getChildAt(0) instanceof CodeLineView)) {
      codeView.removeAllViews();
    }

    for (Map.Entry<Integer, CodeLine> entry : functionImplementation.entrySet()) {
      CodeLineView codeLineView;
      if (index < codeView.getChildCount()) {
        codeLineView = (CodeLineView) codeView.getChildAt(index);
      } else {
        codeLineView = new CodeLineView(mainActivity, index % 2 == 1);
        codeView.addView(codeLineView);
      }
      codeLineView.setLineNumber(entry.getKey());
      codeLineView.setCodeLine(entry.getValue(), symbol.getErrors());
      codeLineView.setClickable(true);  // needed for long press to work :(
      //codeLineView.setFocusable(true);
      codeLineView.setOnTouchListener(new ChildTouchListener());

//       codeLineView.setOnLongClickListener(lineClickListener);
      index++;
    }
    while (index < codeView.getChildCount()) {
      codeView.removeViewAt(codeView.getChildCount() - 1);
    }

  }


  CodeLineView findLineIndex(int lineNumber) {
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
              && rawY > location[1]  && rawY < location[1] + view.getHeight()) {
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
    CodeLineView lastSelected = (CodeLineView) contentView.getChildAt(selection.lastSelectedIndex);
    PopupMenu popup = new PopupMenu(mainActivity, lastSelected);

    popup.setOnDismissListener((a) -> {
      for (int i = selection.getStartIndex(); i <= selection.getEndIndex(); i++) {
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

    menu.add("Copy");
    menu.add("Renumber");
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

    public int getEndIndex() {
      return Math.max(firstSelectedIndex, lastSelectedIndex) + 1;
    }


    void startDeleteFlow() {
      AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainActivity);
      alertBuilder.setTitle("Confirm Delete");
      if (firstSelectedIndex == lastSelectedIndex) {
        alertBuilder.setMessage("Delete line " + getCodeLineView(firstSelectedIndex).lineNumber  + "'?");
      } else {
        alertBuilder.setMessage("Delete lines " + getCodeLineView(getStartIndex()).lineNumber + " - " +
            getCodeLineView(getEndIndex() - 1).lineNumber + "?");
      }
      alertBuilder.setNegativeButton("Cancel", null);
      alertBuilder.setPositiveButton("Delete", (a,b) -> {
        for (int i = getEndIndex() - 1; i >= getStartIndex(); i--) {
          functionImplementation.deleteLine(getCodeLineView(i).lineNumber);
          getContentView().removeViewAt(i);
        }
      });
      alertBuilder.show();
    }
  }


  class ChildTouchListener implements OnTouchListener {
    boolean dragMode = false;
    View view;

    GestureDetector gestureDetector = new GestureDetector(mainActivity, new GestureDetector.SimpleOnGestureListener() {
      @Override
      public void onLongPress(MotionEvent e) {
        dragMode = true;
        view.getParent().requestDisallowInterceptTouchEvent(true);
        startSelection(e);
      }
    });

    @Override
    public boolean onTouch(View view, MotionEvent event) {
      this.view = view;
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
