package org.kobjects.asde.android.ide.property;

import android.widget.LinearLayout;
import android.widget.TextView;

import org.kobjects.asde.android.ide.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.errors.Errors;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PropertyView extends LinearLayout {
  protected final MainActivity mainActivity;

  public PropertyTitleView titleView;
  List<ExpandListener> expandListeners = new ArrayList<>();
  public boolean expanded;
  public Property property;
  private TextView errorView;

  protected ExpandableList contentView;


  protected PropertyView(MainActivity mainActivity, Property symbol) {
    super(mainActivity);
    this.mainActivity = mainActivity;
    this.property = symbol;
    setOrientation(VERTICAL);

    titleView = new PropertyTitleView(mainActivity, symbol.getName());
    addView(titleView);
    titleView.setOnClickListener(clicked -> {
      setExpanded(!expanded, true);

      if (!expanded && mainActivity.sharedCodeViewAvailable()) {
        mainActivity.textOutputView.syncContent();
      }

    });
    refresh();
  }

  public void addExpandListener(ExpandListener expandListener) {
    expandListeners.add(expandListener);
  }

  public abstract void syncContent();

  public void refresh() {
    System.out.println("Refreshing: " + property);
    Map<Node, Exception> errors = property.getErrors();
    titleView.setBackgroundColor(errors.size() > 0 ? Colors.RED : expanded ? Colors.PRIMARY_LIGHT_FILTER : 0);
    if (property.getErrors().size() > 0) {
      System.out.println(property.getErrors());
    }
    Exception exception = property.getErrors().get(Node.NO_NODE);
    if (exception != null) {
      if (errorView == null) {
        errorView = new TextView(mainActivity);
        errorView.setBackgroundColor(Colors.RED);
        addView(errorView, 1);
      }
      errorView.setText(exception.getMessage());
      errorView.setOnClickListener((a) -> {
        Errors.show(mainActivity, exception);
      });

    } else {
      if (errorView != null) {
        removeView(errorView);
        errorView = null;
      }
    }
  }

  public void setExpanded(final boolean expand, boolean animated) {
    if (expanded == expand) {
      return;
    }
    if (animated && contentView == getContentView()) {
      contentView.animateNextChanges();
    }
    expanded = expand;
    for (ExpandListener expandListener : expandListeners) {
      expandListener.notifyExpanding(this, animated);
    }
    syncContent();
  }

  public LinearLayout getContentView() {
    if (mainActivity.sharedCodeViewAvailable()) {
      return mainActivity.obtainSharedCodeView(this);
    }

    if (contentView == null) {
      contentView = new ExpandableList(mainActivity);
      addView(contentView);
    }

    return contentView;
  }


}