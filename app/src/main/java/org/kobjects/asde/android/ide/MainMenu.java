package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.kobjects.asde.android.ide.classifier.CreateClassFlow;
import org.kobjects.asde.android.ide.function.FunctionSignatureFlow;
import org.kobjects.asde.lang.io.ProgramReference;

import java.io.IOException;

public class MainMenu {

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


  private static void addExamples(MainActivity mainActivity, Menu menu, String basePath) throws IOException {
    for (final String fileName : mainActivity.getAssets().list(basePath)) {
      String filePath = basePath + "/" + fileName;
      if (mainActivity.getAssets().list(filePath).length > 0) {
        addExamples(mainActivity, menu.addSubMenu(fileName), filePath);
      } else {
        menu.add(fileName).setOnMenuItemClickListener(item -> {
              mainActivity.load(new ProgramReference(fileName, "file:///android_asset/" + filePath, false), true, false);
              return true;
            }
        );
      }
    }
  }

  private static void addRemoteExamples(MainActivity mainActivity, Menu menu, String baseUrl, String[] list) {
    for (int i = 0; i < list.length; i += 2) {
      String fileName = list[i];
      menu.add(list[i + 1]).setOnMenuItemClickListener(item -> {
        mainActivity.load(new ProgramReference(fileName.substring(0, fileName.indexOf(".")), baseUrl + fileName, false), true, false);
        return true;
      });
    }

  }


  public static void show(MainActivity mainActivity, View menuButton) {
    PopupMenu popupMenu = new PopupMenu(mainActivity, menuButton);
    Menu mainMenu = popupMenu.getMenu();

    mainMenu.add("Erase all and restart").setOnMenuItemClickListener(item -> {
      mainActivity.eraseProgram();
      try {
        mainActivity.program.save(mainActivity.program.reference);
        mainActivity.restart();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return true;
    });

    Menu addMenu = mainMenu.addSubMenu("Add");
    addMenu.add("Class").setOnMenuItemClickListener(item -> {
      CreateClassFlow.start(mainActivity);
      return true;
    });
    addMenu.add("Function").setOnMenuItemClickListener(item -> {
      FunctionSignatureFlow.createFunction(mainActivity);
      return true;
    });

    Menu loadMenu = mainMenu.addSubMenu("Load");


    Menu examplesMenu = loadMenu.addSubMenu("Examples");
    try {
      addExamples(mainActivity, examplesMenu, "examples");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    addRemoteExamples(
        mainActivity,
        examplesMenu.addSubMenu("Refurbished Classics"),
        "https://raw.githubusercontent.com/stefanhaustein/basic-classics/master/src/main/assets/classics/",
        REFURBISHED);

    addRemoteExamples(
          mainActivity,
          examplesMenu.addSubMenu("vintage-basic.net"),
          "http://vintage-basic.net/bcg/",
          VINTAGE_BASIC);


    loadMenu.add("Load local file").setOnMenuItemClickListener(item -> {
      DialogProperties properties = new DialogProperties();
      properties.root = mainActivity.getProgramStoragePath();
      properties.error_dir = mainActivity.getProgramStoragePath();
      properties.offset = mainActivity.getProgramStoragePath();
      properties.selection_mode = DialogConfigs.SINGLE_MODE;
      properties.selection_type = DialogConfigs.FILE_SELECT;

      // TODO: new String[] {".bas", ".asde", ""};
      properties.extensions = null;

      FilePickerDialog dialog = new FilePickerDialog(mainActivity, properties);
      dialog.setTitle("Select Program File");
      dialog.show();
      dialog.setDialogSelectionListener(files -> {
        mainActivity.load(mainActivity.console.nameToReference(files[0]), true, false);
      });
      return true;
    });


    loadMenu.add("Load from cloud storage").setOnMenuItemClickListener(item -> {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("text/plain");
      mainActivity.startActivityForResult(intent, MainActivity.OPEN_EXTERNALLY_REQUEST_CODE);
      return true;
    });

    loadMenu.add("Import").setOnMenuItemClickListener(item -> {
      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("text/plain");
      mainActivity.startActivityForResult(intent, MainActivity.LOAD_EXTERNALLY_REQUEST_CODE);
      return true;
    });


    Menu saveMenu = mainMenu.addSubMenu("Save");

    saveMenu.add("Save locally as...").setOnMenuItemClickListener(item -> {
      AlertDialog.Builder dialog = new AlertDialog.Builder(mainActivity);
      EditText fileNameInput = new EditText(mainActivity);
      fileNameInput.setText(mainActivity.preferences.getProgramReference().name);
      dialog.setTitle("Save as...");
      dialog.setMessage("File name");
      dialog.setView(fileNameInput);
      dialog.setPositiveButton("Save", (dlg, btn) -> {
        String name = fileNameInput.getText().toString();
        if (!name.isEmpty()) {
          try {
            mainActivity.program.save(mainActivity.console.nameToReference(fileNameInput.getText().toString()));
          } catch (Exception e) {
            mainActivity.console.showError("Error saving file " + fileNameInput.getText().toString(), e);
          }
        }
      });
      dialog.show();
      return true;
    });

    saveMenu.add("Save externally as...").setOnMenuItemClickListener(item -> {
      Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
      intent.setType("text/plain");

      mainActivity.startActivityForResult(intent, MainActivity.SAVE_EXTERNALLY_REQUEST_CODE);
      return true;
    });

    saveMenu.add("Add shortcut").setOnMenuItemClickListener(item -> {
      mainActivity.addShortcut();
      return true;
    });

    if (mainActivity.sharedCodeViewAvailable()) {
      OutputView.populateMenu(mainActivity, mainMenu.addSubMenu("Output"));
    }
    popupMenu.show();

  }

}
