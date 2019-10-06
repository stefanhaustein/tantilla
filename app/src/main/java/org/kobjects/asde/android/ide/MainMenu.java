package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.kobjects.asde.android.ide.editor.CreateClassFlow;
import org.kobjects.asde.android.ide.editor.FunctionSignatureFlow;
import org.kobjects.asde.lang.io.ProgramReference;

import java.io.IOException;

public class MainMenu {

  private static String[][] CLASSICS = {
      {"Classics"}, {
        "aceyducey.bas", "AceyDucey (Card Game)",
        "aceyducey.bas", "AceyDucey (Card Game)",
        "blackjack.bas",	"Blackjack (Las Vegas Rules)",
        "hangman.bas",	"Hangman word guessing game",
        "checkers.bas", "Game of checkers",
      "poker.bas", "Poker game",
      "superstartrek.bas", "Super Star Trek Game",
      "superstartrekins.bas", "Super Star Trek Instructions",
      },
      {"Sport Games"}, {
        "basketball.bas", "Basketball Strategy",
        "bowling.bas", "Bowling",
        "boxing.bas",	"3-round boxing match",
        "bullfight.bas",	"Bullfight: You're the matador in a championship bullfight",
        "bullseye.bas", "Bullseye: Throw darts",
        "ftball.bas",	"American football—you vs. the computer",
        "football.bas",	"American football for two players",
        "golf.bas",	"Golf game—choose your clubs and swing",
      },
      {"War Games"}, {
        "battle.bas",	"Battle (Ship Location Game)",
        "bombardment.bas", "Bombardment",
        "bombsaway.bas", "Bombs Away (WWII Bombing Sim)",
        "civilwar.bas",	"Civil War",
        "combat.bas",	"Combat: small war with the computer",
      "depthcharge.bas",	"Depth Charge: destroy a submarine",
      "gunner.bas",	"Fire a cannon at a stationary target",
        "orbit.bas", "Orbit: Destroy an orbiting germ-laden enemy spaceship",
      "war.bas", "Card game of war",
      },
      {"Misc"}, {
        "amazing.bas",	"A Maze Generator",
        "animal.bas",	"Animal Guessing",
        "awari.bas", "Awari Game",
        "bagels.bas",	"Bagles (Number Guessing)",
        "banner.bas",	"Banner Printer",
        "batnum.bas", "Battle of Numbers",
        "bounce.bas", "Bouncing ball plot",
        "bug.bas", "Roll dice vs. the computer to draw a bug",
        "bunny.bas", "Bunny: Computer drawing of the Playboy bunny",
        "buzzword.bas", "Buzzword: Compose your speeches with the latest buzzwords",
        "calendar.bas", "Calendar for any year",
        "change.bas", "Change: Computer imitates a cashier",
        "chemist.bas",	"Chemist: Dilute kryptocyanic acid",
        "chief.bas",	"Chief: Silly arithmetic drill",
        "hello.bas",	"Computer becomes your friendly psychiatrist",
        "chomp.bas", "Chomp: Avoid poison (multiplayer)",
        "craps.bas", "Craps (dice), Las Vegas style",
  "cube.bas",	"Cube: Negotiate a 3-D cube avoiding hidden landmines",
  "diamond.bas",	"Diamond: Prints diamond patterns",
  "dice.bas",	"Dice: Summarizes dice rolls",
  "digits.bas",	"Digits: Computer guesses digits",
  "evenwins.bas", "Even Wins Game",
  "gameofevenwins.bas", "Even Wins; computer improves",
  "flipflop.bas",	"Solitaire logic game",
  "furtrader.bas",	"Trade furs with the white man",
  "gomoko.bas",	"Gomoko: Ancient strategy board game",
  "guess.bas",	"Guess a mystery number—computer gives you clues",
  "hammurabi.bas",	"Hammurabi: Govern an ancient city-state",
  "hexapawn.bas", "Hexapawn game",
  "hi-lo.bas",	"Hi-Lo: Hit the mystery jackpot",
  "highiq.bas", "High IQ: Remove all the pegs",
  "hockey.bas", "Hockey: Two player Ice hockey",
  "horserace.bas", "Horserace: Off-track betting",
  "hurkle.bas", "Hurkle Find the Hurkle hiding on a 10x10 grid",
  "kinema.bas", "Kinema: Drill in simple kinematics",
  "king.bas", "King: Govern a modern island kingdom wisely",

  "letter.bas", "Letter: Guess a letter with clues",
  "life.bas",	"Life: John Conway's Game of Life",
  "lifefortwo.bas", "LifeFor2: 2+ players",
  	"litquiz.bas", "LitQuiz: Children's literature quiz",
  "love.bas", "Love: Robert Indiana's artwork, your message",
  "lunar.bas", "Lunar: Land an Apollo capsule on the moon",
  "lem.bas", "LEM: Very comprehensive lunar landing",
  "rocket.bas", "Rocket: Lunar landing from 500 feet (with plot)",
  "mastermind.bas", "Master Mind: Guess the colors of pegs—then the computer guesses yours",
  "mathdice.bas", "Math Dice: Children's arithmetic drill using pictures of dice",
  "mugwump.bas", "Mugwump Locate 4 mugwumps hiding on a 10x10 grid",
  "name.bas", "Name: An ice-breaker with the computer",
  "nicomachus.bas", "Nicomachus: Computer guesses number you think of",
  "nim.bas", "NIM: Chinese game of Nim",
  "number.bas",	"Number: Silly number matching game",
  "onecheck.bas", "One Check: Challenging game to remove checkers from a board",
  "pizza.bas", "Pizza: Deliver pizzas successfully",
  "poetry.bas", "Poetry: Computer composes random poetry",
  "queen.bas", "Queen: Move a single chess queen vs. the computer",
  "reverse.bas", "Order a series of numbers by reversing",
  "rockscissors.bas", "Rock, Scissors, Paper",
  "roulette.bas", "Roulette (European)",
  "russianroulette.bas", "Russian Roulette",
  "salvo.bas", "Salvo: Destroy an enemy fleet of ships",
  "sinewave.bas", "Sine Wave Drawing",
  "slalom.bas", "Slalom Simulation",
  "slots.bas", "Slot machine (one-armed bandit)",
  "splat.bas", "Splat: Open a parachute at the last possible moment",
  "stars.bas", "Stars: Guess a mystery number—stars give you clues",
  "stockmarket.bas", "Stock market simulation",
  "synonym.bas", "Word synonym drill",
  "target.bas",	"Destroy a target in 3-D space—very tricky",
  "3dplot.bas", "Plot families of curves—looks 3-dimensional",
  "qubit.bas", "Quobit: TicTacToe in 3D",
  "tictactoe1.bas",	"TicTacToe (Simple version)",
  "tictactoe2.bas",	"TicTacToe (With board)",
  "tower.bas", "Towers of Hanoi puzzle",
  "train.bas", "Time-speed-distance quiz",
  "trap.bas", "Trap a mystery number—computer gives you clues",
  "23matches.bas", "23 Matches: don't take the last one",
  "weekday.bas", "Facts about your birthday",
  "word.bas", "Word guessing game"
  }};




  private static void addExamples(MainActivity mainActivity, Menu menu, String basePath) throws IOException {
    for (final String fileName : mainActivity.getAssets().list(basePath)) {
      String filePath = basePath + "/" + fileName;
      if (mainActivity.getAssets().list(filePath).length > 0) {
        addExamples(mainActivity, menu.addSubMenu(fileName), filePath);
      } else{
        menu.add(fileName).setOnMenuItemClickListener(item -> {
              mainActivity.load(new ProgramReference(fileName, "file:///android_asset/" + filePath, false), true, false);
              return true;
            }
        );
      }
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
    }catch (IOException e) {
      throw new RuntimeException(e);
    }

    Menu classicsMenu = examplesMenu.addSubMenu("vintage-basic.net");
    for (int i = 0; i < CLASSICS.length; i+=2) {
      final String categoryName = CLASSICS[i][0];
      Menu categoryMenu = classicsMenu.addSubMenu(CLASSICS[i][0]);
      String[] array = CLASSICS[i + 1];
      for (int j = 0; j < array.length; j += 2) {
        String fileName = array[j];
        categoryMenu.add(array[j + 1]).setOnMenuItemClickListener(item -> {
          mainActivity.load(new ProgramReference(fileName, "http://vintage-basic.net/bcg/" + fileName, false), true, false);
          return true;
        });
      }
    }

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
        mainActivity.load(mainActivity.nameToReference(files[0]), true, false);
      });
      return true;
    });


    loadMenu.add("Load external file").setOnMenuItemClickListener(item -> {
      Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
      intent.addCategory(Intent.CATEGORY_OPENABLE);
      intent.setType("text/plain");
      mainActivity.startActivityForResult(intent, MainActivity.OPEN_EXTERNALLY_REQUEST_CODE);
      return true;
    });

    loadMenu.add("Import external file").setOnMenuItemClickListener(item -> {
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
            mainActivity.program.save(mainActivity.nameToReference(fileNameInput.getText().toString()));
          } catch (Exception e) {
            mainActivity.showError("Error saving file " + fileNameInput.getText().toString(), e);
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
