package org.kobjects.asde.android.ide;

import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.classifier.CreateClassifierFlow;
import org.kobjects.asde.android.ide.filepicker.AssetNode;
import org.kobjects.asde.android.ide.filepicker.FilePicker;
import org.kobjects.asde.android.ide.filepicker.FileNode;
import org.kobjects.asde.android.ide.filepicker.Node;
import org.kobjects.asde.android.ide.filepicker.SimpleLeaf;
import org.kobjects.asde.android.ide.filepicker.SimpleNode;
import org.kobjects.asde.android.ide.function.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.help.HelpDialog;
import org.kobjects.asde.android.ide.field.FieldFlow;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;

import java.io.File;

public class MainMenu {

  private static final Node GENERAL_STORAGE_NODE = new SimpleLeaf("Shared and Cloud Storage…", null);
  private static final Node IMPORT_NODE = new SimpleLeaf("Import…", null);

  public static Node getExamplesNode(MainActivity mainActivity) {
    return new AssetNode(mainActivity.getAssets(), "Examples", "examples");
  }


  public static SimpleNode getRootNode(MainActivity mainActivity, boolean forSave) {
    Node internalStorgae = new FileNode("Application Storage", new File(mainActivity.getProgramStoragePath().getAbsolutePath()));
    return forSave
        ? new SimpleNode("Storage Selection", internalStorgae, GENERAL_STORAGE_NODE)
        : new SimpleNode("Storage Selection", internalStorgae, GENERAL_STORAGE_NODE, getExamplesNode(mainActivity), IMPORT_NODE);
  }


  private static SimpleLeaf[] addRemoteExamples(String baseUrl, String[] list) {
    SimpleLeaf[] result = new SimpleLeaf[list.length / 2];
    int index = 0;
    for (int i = 0; i < list.length; i += 2) {
      String fileName = list[i];
      result[index++] = new SimpleLeaf(list[i + 1], baseUrl + fileName);
    }
    return result;
  }

  public static void confirmLosingUnsavedChanges(MainActivity mainActivity, String actionName, Runnable conditionalAction) {
    if (mainActivity.isUnsaved()) {
      new InputFlowBuilder(mainActivity, actionName).setConfirmationCheckbox("Confirm losing unsaved changes.").start(result -> conditionalAction.run());
    } else {
      conditionalAction.run();
    }
  }


  public static void showProjectMenu(MainActivity mainActivity, View menuButton) {
    PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
    Menu projectMenu = popupMenu.getMenu();
    fillProjectMenu(mainActivity, projectMenu);

    popupMenu.show();

  }


  public static void fillProjectMenu(MainActivity mainActivity, Menu projectMenu) {
    ProgramReference programReference = mainActivity.program.reference;

    projectMenu.add("New" + (mainActivity.isUnsaved() ? "…" : "")).setOnMenuItemClickListener(item -> {
      confirmLosingUnsavedChanges(mainActivity, "New Project", () -> {
        mainActivity.eraseProgram();
        try {
          mainActivity.program.save(mainActivity.program.reference);
          mainActivity.restart();
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      return true;
    });


    projectMenu.add("Open…").setOnMenuItemClickListener(item -> {
      confirmLosingUnsavedChanges(mainActivity, "Open File", () -> {
        new FilePicker(mainActivity, node -> {
          if (node == GENERAL_STORAGE_NODE) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            mainActivity.startActivityForResult(intent, MainActivity.OPEN_EXTERNALLY_REQUEST_CODE);
          } else  if (node == IMPORT_NODE) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            mainActivity.startActivityForResult(intent, MainActivity.LOAD_EXTERNALLY_REQUEST_CODE);
          } else {
            mainActivity.load(new ProgramReference(node.getName(), node.getUrl(), node.isWritable()), true, false);
          }
        }).setTitle("Open")
            .setRootNode(getRootNode(mainActivity, true))
            .setOptions(FilePicker.Option.SINGLE_CLICK)
            .show();
      });
      return true;
    });

    /*
    projectMenu.add("Import…").setOnMenuItemClickListener(item -> {
      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("text/plain");
      mainActivity.startActivityForResult(intent, MainActivity.LOAD_EXTERNALLY_REQUEST_CODE);
      return true;
    });
    */

    /*
    List<ProgramReference> recentList = mainActivity.preferences.getRecents();
    if (recentList.size() >= 2) {
      Menu openRecentMenu = projectMenu.addSubMenu("Open Recent");
      for (ProgramReference reference : mainActivity.preferences.getRecents()) {
        if (!reference.equals(programReference)) {
          openRecentMenu.add(reference.name).setOnMenuItemClickListener(item -> {
            mainActivity.load(reference, true, false);
            return true;
          });
        }
      }
    }
     */

    /*
    projectMenu
        .add("Legacy Mode")
        .setCheckable(true)
        .setChecked(mainActivity.program.isLegacyMode())
        .setOnMenuItemClickListener(event -> {
          mainActivity.program.setLegacyMode(!mainActivity.program.isLegacyMode());
          return true;
        }
    );
    */

    projectMenu.add("Save as…").setOnMenuItemClickListener(item -> {
      new FilePicker(mainActivity, node -> {
        if (node == GENERAL_STORAGE_NODE) {
          Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
          intent.setType("text/plain");
          mainActivity.startActivityForResult(intent, MainActivity.SAVE_EXTERNALLY_REQUEST_CODE);
        } else {
          try {
            ProgramReference reference = new ProgramReference(node.getName(), node.getUrl(), true);
            mainActivity.program.save(reference);
          } catch (Exception e) {
            mainActivity.console.showError("Error saving file " + node.getName(), e);
          }
        }
      }).setTitle("Save as")
          .setRootNode(getRootNode(mainActivity, true))
          .setOptions(FilePicker.Option.CONFIRM_OVERWRITE, FilePicker.Option.CREATE_FILE, FilePicker.Option.CREATE_FOLDER, FilePicker.Option.DELETE)
          .show();
      return true;
    });

/*
    projectMenu.add("Delete…")
        .setEnabled(programReference.urlWritable && programReference.url.startsWith("file://") && !programReference.name.isEmpty())
        .setOnMenuItemClickListener(item -> {
          File file = new File(programReference.url.substring(6));
          new InputFlowBuilder(mainActivity, "Delete " + programReference.name)
              .setConfirmationCheckbox("Delete " + file.getAbsolutePath())
              .start(result -> {
                mainActivity.eraseProgram();
                try {
                  mainActivity.program.save(mainActivity.program.reference);
                  file.delete();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              });
          return true;
        });
 */

    projectMenu.add("Home shortcut…").setOnMenuItemClickListener(item -> {
      mainActivity.addShortcut();
      return true;
    }).setEnabled(!mainActivity.program.reference.name.isEmpty());

  }


  public static void show(MainActivity mainActivity, View menuButton) {

    PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
    Menu mainMenu = popupMenu.getMenu();

    mainMenu.add("Help").setOnMenuItemClickListener(item -> {
      HelpDialog.showHelp(mainActivity);
      return true;
    });

    mainMenu.add("Examples…").setOnMenuItemClickListener(item -> {
      confirmLosingUnsavedChanges(mainActivity, "Open Example", () -> {
        new FilePicker(mainActivity, node -> {
          mainActivity.load(new ProgramReference(node.getName(), node.getUrl(), node.isWritable()), true, false);
        }).setTitle("Open")
            .setRootNode(getExamplesNode(mainActivity))
            .setOptions(FilePicker.Option.SINGLE_CLICK)
            .show();
      });
      return true;
    });

    Menu projectMenu = mainMenu.addSubMenu("Project");
    fillProjectMenu(mainActivity, projectMenu);

    /*
    mainMenu.add("Examples…").setOnMenuItemClickListener(item -> {
      confirmLosingUnsavedChanges(mainActivity, "Open Example", () -> {
        new FilePicker(mainActivity, node ->
            mainActivity.load(new ProgramReference(node.getName(), node.getUrl(), node.isWritable()), true, false))
            .setTitle("Open Example")
            .setRootNode(getExamplesNode(mainActivity))
            .setOptions()
            .show();
      });
      return true;
    });
     */



    Menu displayMenu = mainMenu.addSubMenu("Display");
    displayMenu.add("Clear").setOnMenuItemClickListener(item -> {
      mainActivity.console.clearScreen(Console.ClearScreenType.CLS_STATEMENT);
      return true;
    });
    displayMenu.add("Overlay graphics mode").setCheckable(true).setChecked(mainActivity.preferences.getOverlayGraphics()).setOnMenuItemClickListener(item -> {
      mainActivity.preferences.setOverlayGraphics(!mainActivity.preferences.getOverlayGraphics());
      mainActivity.arrangeUi();
      return true;
    });;

    Menu addMenu = mainMenu.addSubMenu("Add");

    addMenu.add("Add Constant…").setOnMenuItemClickListener(item -> {
      FieldFlow.createStaticProperty(mainActivity, mainActivity.program.mainModule, false);
      return true;
    });

    addMenu.add("Add Variable…").setOnMenuItemClickListener(item -> {
      FieldFlow.createStaticProperty(mainActivity, mainActivity.program.mainModule, true);
      return true;
    });

    addMenu.add("Add Class…").setOnMenuItemClickListener(item -> {
      CreateClassifierFlow.start(mainActivity, CreateClassifierFlow.Kind.CLASS);
      return true;
    });
    addMenu.add("Add Trait…").setOnMenuItemClickListener(item -> {
      CreateClassifierFlow.start(mainActivity, CreateClassifierFlow.Kind.TRAIT);
      return true;
    });
    addMenu.add("Add Function…").setOnMenuItemClickListener(item -> {
      FunctionSignatureFlow.createFunction(mainActivity);
      return true;
    });

    popupMenu.show();
  }

}
