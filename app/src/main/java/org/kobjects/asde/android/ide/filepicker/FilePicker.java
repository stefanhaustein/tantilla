package org.kobjects.asde.android.ide.filepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class FilePicker {

  public enum Option {
    DELETE, CREATE_FILE, CREATE_FOLDER, CONFIRM_OVERWRITE, SINGLE_CLICK
  }

  private static final String NEW_FILE = "New File";
  private static final String NEW_FOLDER = "New Folder";

  private final Context context;
  private final Consumer<Node> callback;
  private final List<Node> path = new ArrayList<>();

  private String title = "Open File";

  private EnumSet<Option> options = EnumSet.noneOf(Option.class);

  public FilePicker(Context context, Consumer<Node> callback) {
    this.context = context;
    this.callback = callback;
    path.add(new FileNode(context.getFilesDir()));
  }

  public FilePicker setTitle(String title) {
    this.title = title;
    return this;
  }

  public FilePicker setOptions(Option... options) {
    this.options = options.length == 0 ? EnumSet.noneOf(Option.class) : EnumSet.copyOf(Arrays.asList(options));
    return this;
  }

  public FilePicker setRootNode(Node node) {
    path.clear();
    path.add(node);
    return this;
  }


  public void show() {
    Node currentNode = path.get(path.size() - 1);
    List<Node> chidren = currentNode.getChildren();
    boolean isRoot = path.size() == 1;

    AlertDialog[] alert = new AlertDialog[1];
    String[] selectedAction = new String[1];
    Node[] selectedNode = new Node[1];

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(title);

    ArrayList<String> names = new ArrayList<>();
    for (Node child : chidren) {
      names.add(child.getName() + (child.isLeaf() ? "" : "/"));
    }

    // Needed first for references in actions etc.
    TextInputLayout textInputLayout = new TextInputLayout(context);
    textInputLayout.addView(new EditText(context));
    textInputLayout.getEditText().setEnabled(false);
    textInputLayout.setErrorEnabled(true);
    TextValidator.TextInputLayoutValidator validator = new TextValidator() {
        @Override
        public String validate(String text) {
          String error = null;
          if (textInputLayout.getEditText().isEnabled()) {
            if (text.isEmpty()) {
              error = "File name can't be empty.";
            } else if (text.indexOf('/') != -1) {
              error = "File name must not contain '/'.";
            } else if (names.contains(text) || names.contains(text + "/")) {
              error = "File existst!";
            }
          }
          if (alert[0] != null) {
            alert[0].getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(error == null);
          }
          return error;
        }
      }.attach(textInputLayout);


    Runnable itemAction = () -> {
      String action = selectedAction[0];
      if (NEW_FOLDER.equals(action)) {
        Node newNode = currentNode.createChild(false, textInputLayout.getEditText().getText().toString());
        path.add(newNode);
        show();
      } else if (NEW_FILE.equals(action)) {
        Node newNode = currentNode.createChild(true, textInputLayout.getEditText().getText().toString());
        callback.accept(newNode);
      } else {
        Node selected = selectedNode[0];
        if (selected.isLeaf()) {
          if (selected.isWritable() && options.contains(Option.CONFIRM_OVERWRITE)) {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
            alertBuilder.setTitle("Confirm Overwrite");
            alertBuilder.setMessage("Replace existing file '" + selected.getName() + "'");
            alertBuilder.setNegativeButton("Cancel", (i, j) -> show());
            alertBuilder.setPositiveButton("Replace", (i, j) -> callback.accept(selected));
            alertBuilder.show();
          } else {
            callback.accept(selected);
          }
        } else {
          path.add(selected);
          show();
        }
      }
    };

    LinearLayout container = new LinearLayout(context);
    container.setOrientation(LinearLayout.VERTICAL);
    RadioGroup radioGroup = new RadioGroup(context);

    Spinner pathSpinner = new Spinner(context);
    ArrayList<String> pathNames = new ArrayList<>();
    for (Node node : path) {
      pathNames.add(node.getName());
    }
    ArrayAdapter<String> adapter =new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, pathNames);
    pathSpinner.setAdapter(adapter);
    pathSpinner.setEnabled(!isRoot);
    pathSpinner.setSelection(path.size() - 1);
    pathSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i >= 0 && i < path.size() - 1) {
          while (path.size() > i + 1) {
            path.remove(path.size() - 1);
          }
          alert[0].dismiss();
          show();
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {

      }
    });
    container.addView(pathSpinner);

    ListView listView = new ListView(context);
    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    ArrayAdapter arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names);
    listView.setAdapter(arrayAdapter);
    listView.setSelector(new ColorDrawable(0x44ffffff));
    listView.setHeaderDividersEnabled(true);
    listView.setFooterDividersEnabled(true);

    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
        Node node = chidren.get(index);
        if (selectedNode[0] == node || options.contains(Option.SINGLE_CLICK)) {
          selectedNode[0] = node;
          alert[0].dismiss();
          itemAction.run();
        } else {
          radioGroup.clearCheck();
          textInputLayout.getEditText().setText(node.getName());
          textInputLayout.getEditText().setEnabled(false);
          alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setText(node.isLeaf() && node.isWritable() && options.contains(Option.CONFIRM_OVERWRITE) ? "Replace" : "Open");
          alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
          alert[0].getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
          selectedNode[0] = node;
          selectedAction[0] = null;
          if (validator != null) {
            validator.update();
          }
        }
      }
    });

    ArrayList<String> createOptions = new ArrayList<>();
    if (currentNode.isWritable()) {
      if (options.contains(Option.CREATE_FILE)) {
        createOptions.add(NEW_FILE);
      }
      if (options.contains(Option.CREATE_FOLDER)) {
        createOptions.add(NEW_FOLDER);
      }
    }

    DisplayMetrics metrics = new DisplayMetrics();
    int height = context.getResources().getDisplayMetrics().heightPixels / (createOptions.isEmpty() ? 3 : 4);


    container.addView(listView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
    /*

    for (Node node : currentNode.getChildren()) {
      RadioButton radioButton = new RadioButton(context);
      radioButton.setText(node.isLeaf() ? node.getName() : (node.getName() + "/"));
      radioGroup.addView(radioButton);
      radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
          if (checked) {
            lastChange = System.currentTimeMillis();
            textInputLayout.getEditText().setText(node.getName());
            textInputLayout.getEditText().setEnabled(false);
            alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setText(node.isLeaf() && options.contains(Option.CONFIRM_OVERWRITE) ? "Replace" : "Open");
            alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
            alert[0].getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
            selectedNode[0] = node;
            selectedAction[0] = null;
            if (validator != null) {
              validator.update();
            }
          }
        }
      });
      radioButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (System.currentTimeMillis() - lastChange > 200 && ((RadioButton) view).isChecked()) {

          }
        }
      });
    }
    */

    if (!createOptions.isEmpty()) {
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);
        radioGroup.addView(new TextView(context));
        for (String name : createOptions) {
          RadioButton radioButton = new RadioButton(context);
          radioButton.setText(name);
          radioGroup.addView(radioButton);
          radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
              if (checked) {
                listView.setAdapter(arrayAdapter);
                textInputLayout.getEditText().setEnabled(true);
                alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setText("Create");
                alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                alert[0].getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                selectedNode[0] = null;
                selectedAction[0] = name;
                validator.update();
              }
            }
          });
        }
    }



    container.addView(radioGroup);

    if (!createOptions.isEmpty()) {
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

    if (currentNode.isWritable() && options.contains(Option.DELETE)) {
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
    }


    if (!options.contains(Option.SINGLE_CLICK)) {
      builder.setPositiveButton("Open", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          itemAction.run();
        }
      });
    }

    alert[0] = builder.show();
    alert[0].getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
    alert[0].getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
  }
}
