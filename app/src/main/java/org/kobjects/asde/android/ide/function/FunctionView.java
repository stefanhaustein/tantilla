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

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.property.DeleteFlow;
import org.kobjects.asde.android.ide.property.RenameFlow;
import org.kobjects.asde.android.ide.property.PropertyView;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.trait.AdapterType;
import org.kobjects.asde.lang.function.Callable;
import org.kobjects.asde.lang.function.FunctionType;
import org.kobjects.asde.lang.function.UserFunction;
import org.kobjects.asde.lang.statement.Statement;
import org.kobjects.asde.lang.type.MetaType;
import org.kobjects.asde.lang.type.Types;

import java.util.ArrayList;

public class FunctionView extends PropertyView {
  public Callable userFunction;

  private Selection selection;

  public FunctionView(final MainActivity mainActivity, Property symbol) {
    super(mainActivity, symbol);
    this.userFunction = (Callable) symbol.getStaticValue();

    boolean isMain = userFunction == mainActivity.program.getMain();
    boolean isMethod = userFunction.getType().getParameterCount() > 0 && userFunction.getType().getParameter(0).getName().equals("self");
    boolean isAdapterMethod = symbol.getOwner().getType() instanceof MetaType
        && ((MetaType) symbol.getOwner().getType()).getWrapped() instanceof AdapterType;

    titleView.setTypeIndicator(
          "def",
          isMethod ? Colors.LIGHT_PURPLE_RED : isMain ? Colors.PRIMARY_FILTER : Colors.YELLOW,
          true);

    if (!isMain && !isAdapterMethod) {
      titleView.setMoreClickListener(clicked -> {
        PopupMenu popup = new PopupMenu(mainActivity, clicked);
        Menu menu = popup.getMenu();
        if (!isMain) {
          menu.add("Rename").setOnMenuItemClickListener(item -> {
            RenameFlow.start(mainActivity, symbol);
            return true;
          });
          menu.add("Change Signature").setOnMenuItemClickListener(item -> {
            FunctionSignatureFlow.changeSignature(mainActivity, symbol);
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
    }
    refreshSignature();
  }

  int syncedTo;

  void refreshSignature() {
    FunctionType type = userFunction.getType();
    titleView.setTitle(property.getName() + "(" + (type.getParameterCount() == 0 ? ")" : ""));

    boolean isVoid = userFunction.getType().getReturnType() == Types.VOID;
    ArrayList<String> subtitles = new ArrayList<>();
    for (int i = 0; i < type.getParameterCount(); i++) {
      subtitles.add(" " + type.getParameter(i).getName() + ": " + type.getParameter(i).getExplicitType() + (i == type.getParameterCount() - 1 ? ")" : ","));
    }
    if (!isVoid) {
      subtitles.add("-> " + userFunction.getType().getReturnType());
    }
    titleView.setSubtitles(subtitles);
  }

  public void syncContent() {

    // TODO: Ideally, this would be triggered by the function signature flow, which should get
    //   a reference to the FunctionView....
    refreshSignature();

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

    if (userFunction instanceof UserFunction) {
      for (Statement statement : ((UserFunction) userFunction).allLines()) {
        if (index >= syncedTo) {
          updated++;
          CodeLineView codeLineView;
          if (index < codeView.getChildCount()) {
            codeLineView = (CodeLineView) codeView.getChildAt(index);
          } else {
            codeLineView = new CodeLineView(mainActivity, index % 2 == 1);
            codeView.addView(codeLineView);
          }
          codeLineView.setCodeLine(index + 1, statement, property.getErrors());
        }
        index++;
        if (updated > 8) {
          syncedTo += updated;
          post(this::syncContent);
          return;
        }
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
      /*
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

       */
    }

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
          ((UserFunction) userFunction).deleteLine(getCodeLineView(i).lineNumber);
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
