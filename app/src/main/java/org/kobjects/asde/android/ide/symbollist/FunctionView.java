package org.kobjects.asde.android.ide.symbollist;

import android.graphics.Color;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.FunctionImplementation;
import org.kobjects.asde.lang.StaticSymbol;
import org.kobjects.asde.lang.type.CodeLine;
import org.kobjects.asde.lang.type.Types;

import java.util.ArrayList;
import java.util.Map;

public class FunctionView extends SymbolView {
  public FunctionImplementation functionImplementation;

  int selectionStartIndex;
  int selectionEndIndex;

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


  CodeLineView findLine(int lineNumber) {
    LinearLayout codeView = getContentView();
    for (int i = 0; i < codeView.getChildCount(); i++) {
      CodeLineView codeLineView = (CodeLineView) codeView.getChildAt(i);
      if (codeLineView.lineNumber == lineNumber) {
        return codeLineView;
      }
    }
    return null;
  }

  private int findLine(MotionEvent event) {
    LinearLayout codeView = getContentView();
    int rawX = (int) event.getRawX();
    int rawY = (int) event.getRawY();
    System.out.println("ev: " + event + " rx: " + rawX + " ry: " + rawY);
    int[] location = new int[2];
    for (int i = 0; i < codeView.getChildCount(); i++) {
      CodeLineView view = (CodeLineView) codeView.getChildAt(i);
      view.getLocationOnScreen(location);

      if (rawX > location[0] && rawX < location[0] + view.getWidth()
              && rawY > location[1]  && rawY < location[1] + view.getHeight()) {
        return i;
      }
    }
    return -1;
  }


  private void startSelection(MotionEvent e) {
    int i = findLine(e);
    if (i != -1) {
      selectionStartIndex = i;
      selectionEndIndex = i;
      moveSelection(e);
    }
  }

  private void moveSelection(MotionEvent e) {
    int index = findLine(e);
    if (index != -1) {
      LinearLayout contentView = getContentView();
        for (int i = Math.min(selectionStartIndex, selectionEndIndex); i <= Math.max(selectionStartIndex, selectionEndIndex); i++) {
          contentView.getChildAt(i).setBackgroundColor(0);
        }
        selectionEndIndex = index;
      for (int i = Math.min(selectionStartIndex, selectionEndIndex); i <= Math.max(selectionStartIndex, selectionEndIndex); i++) {
        contentView.getChildAt(i).setBackgroundColor(Colors.ORANGE);
      }
      

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
        getContentView().setBackgroundColor(0);
      } else {
        moveSelection(event);
      }

      return true;
    }
  }

}
