package org.kobjects.asde.android.ide.filepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.Consumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileDialog {

  static final String UP = "..";
  static final String NEW_FILE = "New File...";
  static final String NEW_FOLDER = "New Folder...";

  final Context context;
  final Consumer<Node> callback;
  final ArrayList<Node> path = new ArrayList<>();

  public FileDialog(Context context, String baseDir, Consumer<Node> callback) {
    this(context, new FileNode(new File(baseDir)), callback);
  }

  public FileDialog(Context context, Node rootNode, Consumer<Node> callback) {
    this.context = context;
    this.callback = callback;
    path.add(rootNode);
  }


  public void show() {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    boolean isRoot = path.size() == 1;
    Node currentNode = path.get(path.size() - 1);

    builder.setTitle("Open");

    List<Node> children = new ArrayList<>();
    List<String> names = new ArrayList<>();

    children.addAll(currentNode.getChildren());
    for (Node child : children) {
      names.add(child.getName() + (child.isLeaf() ? "" : "/"));
    }
    if (!isRoot) {
      names.add(0, UP);
      children.add(0, null);
    }

    AlertDialog[] alert = new AlertDialog[1];

    TextInputLayout textInputLayout = new TextInputLayout(context);
    textInputLayout.setEnabled(false);
    textInputLayout.addView(new EditText(context));
    TextValidator.TextInputLayoutValidator validator = new TextValidator() {
        @Override
        public String validate(String text) {
          String error = textInputLayout.isEnabled()
              ? text.isEmpty() ? "File name can't be empty" : (names.contains(text) || names.contains(text + "/")) ? "File exists" : null : null;
          if (alert[0] != null) {
            alert[0].getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(error == null);
          }
          return error;
        }
      }.attach(textInputLayout);


    if (currentNode.isWriteable()) {
      names.add(NEW_FILE);
      names.add(NEW_FOLDER);
    }


    String[] selectedName = new String[1];
    Node[] selectedNode = new Node[1];

    RadioGroup radioGroup = new RadioGroup(context);
    for (int i = 0; i < names.size(); i++) {
      final int index = i;
      String name = names.get(i);
      RadioButton radioButton = new RadioButton(context);
      radioButton.setText(name);
      radioGroup.addView(radioButton);
      radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
          if (!b) {
            return;
          }
          if (name.equals(NEW_FOLDER) || name.equals(NEW_FILE)) {
            textInputLayout.setEnabled(true);
            alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setText("Create");
          } else {
            if (!name.equals(UP)) {
              textInputLayout.getEditText().setText(name);
            }
            textInputLayout.setEnabled(false);
            alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setText("Open");
          }
          if (validator != null) {
            validator.update();
          }

          alert[0].getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(
              !name.equals(NEW_FILE) && !name.equals(NEW_FOLDER) && !name.equals(UP));

          selectedName[0] = name;
          selectedNode[0] = index < children.size() ? children.get(index) : null;
        }
      });
    }

    LinearLayout container = new LinearLayout(context);
    container.setOrientation(LinearLayout.VERTICAL);

    if (!isRoot) {
      TextView pathView = new TextView(context);
      pathView.setText(currentNode.getName() + "/");
      container.addView(pathView);
    }

    container.addView(radioGroup);
    if (currentNode.isWriteable()) {
      container.addView(textInputLayout);
    }

    ScrollView scrollView = new ScrollView(context);
    ScrollView.LayoutParams scrollViewLayoutParams = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    scrollViewLayoutParams.topMargin = 24;
    scrollViewLayoutParams.leftMargin = 56;
    scrollViewLayoutParams.rightMargin = 56;
    scrollView.addView(container, scrollViewLayoutParams);

    builder.setView(scrollView);


    builder.setNegativeButton("Cancel", null);

    builder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        Node toDelete = selectedNode[0];

        if (!toDelete.isLeaf() && !toDelete.getChildren().isEmpty()) {
          new InputFlowBuilder(context, "Error")
              .setMessage("Folder is not empty")
              .start(null);
        } else {
          new InputFlowBuilder(context, "Confirm Delete")
              .setConfirmationCheckbox("Delete "
                  + (toDelete.isLeaf() ? "file '" : "folder '") + toDelete.getName() + "'.")
              .setPositiveLabel("Delete")
              .start(new InputFlowBuilder.ResultHandler() {
                @Override
                public void handleResult(String... result) {
                  toDelete.delete();
                  show();
                }
              });
        }
      }
    });


    builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        String name = selectedName[0];
        if (name.equals(UP)) {
          path.remove(path.size() - 1);
          show();
        } else if (name.equals(NEW_FOLDER)) {

        } else if (name.equals(NEW_FILE)) {
          //     callback.accept(new File(currentPath, textInputLayout.getEditText().getText().toString()).getAbsolutePath());
        } else {
          Node selected = selectedNode[0];

          if (selected.isLeaf()) {
            callback.accept(selected);
          } else {
            path.add(selected);
            show();
          }
        }
      }
    });

    alert[0] = builder.show();
    alert[0].getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);


  }

}
