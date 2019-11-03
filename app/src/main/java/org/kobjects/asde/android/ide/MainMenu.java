package org.kobjects.asde.android.ide;

import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.classifier.CreateClassFlow;
import org.kobjects.asde.android.ide.filepicker.AssetNode;
import org.kobjects.asde.android.ide.filepicker.FilePicker;
import org.kobjects.asde.android.ide.filepicker.FileNode;
import org.kobjects.asde.android.ide.filepicker.Node;
import org.kobjects.asde.android.ide.filepicker.SimpleLeaf;
import org.kobjects.asde.android.ide.filepicker.SimpleNode;
import org.kobjects.asde.android.ide.function.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;

import java.io.File;
import java.util.List;

public class MainMenu {

  private static final Node GENERAL_STORAGE_NODE = new SimpleLeaf("Shared and Cloud Storage…", null);
  private static final Node IMPORT_NODE = new SimpleLeaf("Import…", null);

  private static String[] REFURBISHED = {
      "poker.bas", "Poker"
  };

  private static String[] VINTAGE_BASIC = {
      "3dplot.bas", "3D-Plot curves",
      "23matches.bas", "23 Matches",

      "animal.bas", "Animal Guessing",
      "awari.bas", "Awari Game",
      "amazing.bas", "A Maze Generator",

      "bagels.bas", "Bagles (Number Guessing)",
      "banner.bas", "Banner Printer",
      "basketball.bas", "Basketball Strategy",
      "batnum.bas", "Battle of Numbers",
      "battle.bas", "Battle Ships",
      "bombardment.bas", "Bombardment",
      "bombsaway.bas", "Bombs Away (WWII Bombing Sim)",
      "blackjack.bas", "Blackjack (Las Vegas Rules)",
      "bowling.bas", "Bowling",
      "boxing.bas", "3-round boxing match",
      "bounce.bas", "Bouncing ball plot",
      "bunny.bas", "Bunny: Computer drawing",
      "buzzword.bas", "Buzzword Generator",
      "bug.bas", "Bug: Roll dice vs. computer",
      "bullseye.bas", "Bullseye: Throw darts",
      "bullfight.bas", "Bullfight matador",

      "checkers.bas", "Game of checkers",
      "civilwar.bas", "Civil War",
      "combat.bas", "Combat: small war vs. computer",
      "calendar.bas", "Calendar for any year",
      "change.bas", "Change: Computer cashier",
      "chemist.bas", "Chemist: Dilute kryptocyanic acid",
      "chief.bas", "Chief: Silly arithmetic drill",
      "chomp.bas", "Chomp: Avoid poison (multiplayer)",
      "craps.bas", "Craps (dice), Las Vegas style",
      "cube.bas", "Cube: Negotiate a 3-D cube",

      "depthcharge.bas", "Depth Charge: destroy a submarine",
      "dice.bas", "Dice: Summarizes dice rolls",
      "digits.bas", "Digits: Computer guesses digits",
      "diamond.bas", "Diamond: Prints diamond patterns",

      "evenwins.bas", "Even Wins Game",

      "ftball.bas", "American football vs. computer",
      "football.bas", "American football 2 players",
      "furtrader.bas", "Trade furs",
      "flipflop.bas", "Solitaire logic game",

      "gameofevenwins.bas", "Even Wins; computer improves",
      "guess.bas", "Guess a number with clues",
      "gomoko.bas", "Gomoko: Ancient strategy board game",
      "golf.bas", "Golf game",
      "gunner.bas", "Gunner: Fire a cannon",

      "hangman.bas", "Hangman word guessing game",
      "hammurabi.bas", "Hammurabi: Govern an ancient city-state",
      "hello.bas", "Hello: computer psychiatrist",
      "hockey.bas", "Hockey: 2 player Ice hockey",
      "horserace.bas", "Horserace: Off-track betting",
      "hexapawn.bas", "Hexapawn game",
      "hi-lo.bas", "Hi-Lo: Hit the mystery jackpot",
      "highiq.bas", "High IQ: Remove all the pegs",
      "hurkle.bas", "Hurkle: Find the Hurkle in a 10x10 grid",

      "kinema.bas", "Kinema: Drill in simple kinematics",
      "king.bas", "King: Govern an island kingdom",

      "letter.bas", "Letter: Guess a letter with clues",
      "lunar.bas", "Lunar: Land an Apollo capsule on the moon",
      "lem.bas", "LEM: Very comprehensive lunar landing",
      "life.bas", "Life: John Conway's Game of Life",
      "lifefortwo.bas", "LifeFor2: 2+ players",
      "litquiz.bas", "LitQuiz: Children's literature quiz",
      "love.bas", "Love: Robert Indiana's artwork, your message",

      "mathdice.bas", "Math Dice: Children's math drill",
      "mugwump.bas", "Mugwump: Locate 4 mugwumps hiding on a 10x10 grid",
      "mastermind.bas", "Master Mind color guessing",

      "nicomachus.bas", "Nicomachus: Computer guesses your number ",
      "name.bas", "Name: An ice-breaker with the computer",
      "nim.bas", "NIM: Chinese game of Nim",
      "number.bas", "Number: Silly number matching game",

      "orbit.bas", "Orbit: Destroy an orbiting germ-laden enemy spaceship",
      "onecheck.bas", "One Check: Remove checkers",

      "poker.bas", "Poker game",
      "pizza.bas", "Pizza: Deliver pizzas successfully",
      "poetry.bas", "Poetry: Computer composes random poetry",

      "queen.bas", "Queen: Single queen vs. computer",
      "qubit.bas", "Quobit: TicTacToe in 3D",

      "reverse.bas", "Order a series of numbers by reversing",
      "rockscissors.bas", "Rock, Scissors, Paper",
      "roulette.bas", "Roulette (European)",
      "russianroulette.bas", "Russian Roulette",
      "rocket.bas", "Rocket: Lunar landing from 500 feet (with plot)",

      "salvo.bas", "Salvo: Destroy an enemy fleet of ships",
      "superstartrek.bas", "Super Star Trek Game",
      "superstartrekins.bas", "Super Star Trek Instructions",
      "sinewave.bas", "Sine Wave Drawing",
      "slalom.bas", "Slalom Simulation",
      "slots.bas", "Slot machine (one-armed bandit)",
      "splat.bas", "Splat: Open a parachute at the last possible moment",
      "stars.bas", "Stars: Guess a mystery number—stars give you clues",
      "stockmarket.bas", "Stock market simulation",
      "synonym.bas", "Word synonym drill",

      "target.bas", "Destroy a target in 3-D space—very tricky",
      "tictactoe1.bas", "TicTacToe (Simple version)",
      "tictactoe2.bas", "TicTacToe (With board)",
      "tower.bas", "Towers of Hanoi puzzle",
      "train.bas", "Time-speed-distance quiz",
      "trap.bas", "Trap a mystery number—computer gives you clues",

      "war.bas", "Card game of war",
      "weekday.bas", "Facts about your birthday",
      "word.bas", "Word guessing game"
  };

  public static Node getExamplesNode(MainActivity mainActivity) {
    return new AssetNode(mainActivity.getAssets(), "Examples", "examples",
        new SimpleNode("Refurbished classics",
            addRemoteExamples(
                "https://raw.githubusercontent.com/stefanhaustein/basic-classics/master/src/main/assets/classics/",
                REFURBISHED)),
        new SimpleNode("vintage-basic.net",
            addRemoteExamples(
                "http://vintage-basic.net/bcg/",
                VINTAGE_BASIC)));
  }


  public static SimpleNode getRootNode(MainActivity mainActivity, boolean forSave) {
    Node internalStorgae = new FileNode("Application Internal Storage", new File(mainActivity.getProgramStoragePath().getAbsolutePath()));
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


  public static void show(MainActivity mainActivity, View menuButton) {
    PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
    Menu mainMenu = popupMenu.getMenu();

    ProgramReference programReference = mainActivity.program.reference;

    Menu projectMenu = mainMenu.addSubMenu("Project");

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
            .setRootNode(getRootNode(mainActivity, false))
            .setOptions()
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
          .show();
      return true;
    });


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

    projectMenu.add("Home shortcut…").setOnMenuItemClickListener(item -> {
      mainActivity.addShortcut();
      return true;
    }).setEnabled(!mainActivity.program.reference.name.isEmpty());

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
    displayMenu.add("Debug graphics window").setCheckable(true).setChecked(mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = !mainActivity.windowMode;
      mainActivity.arrangeUi();
      return true;
    });;


    Menu addMenu = mainMenu.addSubMenu("Edit");
    addMenu.add("Add Class…").setOnMenuItemClickListener(item -> {
      CreateClassFlow.start(mainActivity);
      return true;
    });
    addMenu.add("Add Function…").setOnMenuItemClickListener(item -> {
      FunctionSignatureFlow.createFunction(mainActivity);
      return true;
    });


    if (mainActivity.sharedCodeViewAvailable()) {
      TextOutputView.populateMenu(mainActivity, mainMenu.addSubMenu("Output"));
    }
    popupMenu.show();
  }

}
