package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;

import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.kobjects.asde.android.ide.editor.CreateClassFlow;
import org.kobjects.asde.android.ide.editor.FunctionSignatureFlow;
import org.kobjects.asde.lang.io.Console;
import org.kobjects.asde.lang.io.ProgramReference;

import java.io.IOException;

public class MainMenu {


  private static String[] CLASSICS = {
      "aceyducey.bas", 	"AceyDucey (Card Game)",
      "amazing.bas",	"A Maze Generator",
      "animal.bas",	"Animal Guessing",
      "awari.bas", "Awari Game",
      "bagels.bas",	"Bagles (Number Guessing)",
      "banner.bas",	"Banner Printer",
      "basketball.bas",	"Basketball Strategy Game",
      "batnum.bas", "Battle of Numbers",
      "battle.bas",	"Battle (Ship Location Game)",
      "blackjack.bas",	"Blackjack (Las Vegas Rules)",
      "bombardment.bas", "Bombardment",
      "bombsaway.bas", "Bombs Away (WWII Bombing Sim)",
      "bounce.bas", "Bouncing ball plot",
      "bowling.bas", "Bowling game",
      "boxing.bas",	"3-round boxing match",
      "bug.bas", "Roll dice vs. the computer to draw a bug",
      "bullfight.bas",	"Bullfight: You're the matador in a championship bullfight",
      "bullseye.bas", "Bullseye: Throw darts",
      "bunny.bas", "Bunny: Computer drawing of the Playboy bunny",
      "buzzword.bas", "Buzzword: Compose your speeches with the latest buzzwords",
      "calendar.bas", "Calendar for any year",
      "change.bas", "Change: Computer imitates a cashier",
      "checkers.bas", "Game of checkers",
      /*
  Chemist	chemist.bas	Dilute kryptocyanic acid to make it harmless
  Chief	chief.bas	Silly arithmetic drill
  Chomp	chomp.bas	Eat a cookie avoiding the poison piece (2 or more players)
  Civil War	civilwar.bas	Fight the Civil War
  Combat	combat.bas	Fight a small-scale war with the computer
  Craps	craps.bas	Play craps (dice), Las Vegas style
  Cube	cube.bas	Negotiate a 3-D cube avoiding hidden landmines
  Depth Charge	depthcharge.bas	Launch depth charges to destroy a submarine
  Diamond	diamond.bas	Prints 1-page diamond patterns
  Dice	dice.bas	Summarizes dice rolls
  Digits	digits.bas	Computer tries to guess digits you select at random
  Even Wins	evenwins.bas	Take objects from a pile—try to end with an even number
  Game of Even Wins	gameofevenwins.bas	Same as Even Wins—computer improves its play
  Flip Flop	flipflop.bas	Solitaire logic game—change a row of Xs to Os
  Ftball	ftball.bas	American football—you vs. the computer
  Football	football.bas	American football for two players
  Fur Trader	furtrader.bas	Trade furs with the white man
  Golf	golf.bas	Golf game—choose your clubs and swing
  Gomoko	gomoko.bas	Ancient board game of logic and strategy
  Guess	guess.bas	Guess a mystery number—computer gives you clues
  Gunner	gunner.bas	Fire a cannon at a stationary target
  Hammurabi	hammurabi.bas	Govern the ancient city-state of Sumeria
  Hangman	hangman.bas	Hangman word guessing game
  Hello	hello.bas	Computer becomes your friendly psychiatrist
  Hexapawn	hexapawn.bas	Hexapawn game
  Hi-Lo	hi-lo.bas	Try to hit the mystery jackpot
  High I-Q	highiq.bas	Try to remove all the pegs from a board
  Hockey	hockey.bas	Ice hockey, two players
  Horserace	horserace.bas	Off-track betting on a horse race
  Hurkle	hurkle.bas	Find the Hurkle hiding on a 10x10 grid
  Kinema	kinema.bas	Drill in simple kinematics
  King	king.bas	Govern a modern island kingdom wisely
  Letter	letter.bas	Guess a mystery letter—computer gives you clues
  Life	life.bas	John Conway's Game of Life
  Life For Two	lifefortwo.bas	Competitive game of Life (two or more players)
  Literature Quiz	litquiz.bas	Children's literature quiz
  Love	love.bas	Robert Indiana's artwork, your message
  Lunar	lunar.bas	Land an Apollo capsule on the moon
  LEM	lem.bas	Very comprehensive lunar landing
  Rocket	rocket.bas	Lunar landing from 500 feet (with plot)
  Master Mind	mastermind.bas	Guess the colors of pegs—then the computer guesses yours
  Math Dice	mathdice.bas	Children's arithmetic drill using pictures of dice
  Mugwump	mugwump.bas	Locate 4 mugwumps hiding on a 10x10 grid
  Name	name.bas	An ice-breaker with the computer
  Nicomachus	nicomachus.bas	Computer guesses number you think of
  Nim	nim.bas	Chinese game of Nim
  Number	number.bas	Silly number matching game
  One Check	onecheck.bas	Challenging game to remove checkers from a board
  Orbit	orbit.bas	Destroy an orbiting germ-laden enemy spaceship
  Pizza	pizza.bas	Deliver pizzas successfully
  Poetry	poetry.bas	Computer composes random poetry
  Poker	poker.bas	Poker game
  Queen	queen.bas	Move a single chess queen vs. the computer
  Reverse	reverse.bas	Order a series of numbers by reversing
  Rock, Scissors, Paper	rockscissors.bas	Game of rock, scissors, paper
  Roulette	roulette.bas	European roulette table
  Russian Roulette	russianroulette.bas	Russian roulette
  Salvo	salvo.bas	Destroy an enemy fleet of ships
  Sine Wave	sinewave.bas	Draw a sine wave on screen
  Slalom	slalom.bas	Simulates a slalom run
  Slots	slots.bas	Slot machine (one-armed bandit)
  Splat	splat.bas	Open a parachute at the last possible moment
  Stars	stars.bas	Guess a mystery number—stars give you clues
  Stock Market	stockmarket.bas	Stock market simulation
  Super Star Trek	superstartrek.bas	Comprehensive game of Star Trek
  Super Star Trek: Instructions	superstartrekins.bas	Instructions for Super Star Trek
  Synonym	synonym.bas	Word synonym drill
  Target	target.bas	Destroy a target in 3-D space—very tricky
3-D Plot	3dplot.bas	Plot families of curves—looks 3-dimensional
3-D Tic-Tac-Toe	qubit.bas	Game of tic-tac-toe in a 4x4x4 cube
  Tic-Tac-Toe 1	tictactoe1.bas	Simple version
  Tic-Tac-Toe 2	tictactoe2.bas	This version prints out the board
  Tower	tower.bas	Towers of Hanoi puzzle
  Train	train.bas	Time-speed-distance quiz
  Trap	trap.bas	Trap a mystery number—computer gives you clues
23 Matches	23matches.bas	Game of 23 matches—try not to take the last one
  War	war.bas	Card game of war
  Weekday	weekday.bas	Facts about your birthday
  Word	word.bas	Word guessing game*/
  };




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

    Menu classicsMenu = loadMenu.addSubMenu("vintage-basic.net");
    for (int i = 0; i < CLASSICS.length; i+=2) {
      final String fileName = CLASSICS[i];
      classicsMenu.add(CLASSICS[i+1]).setOnMenuItemClickListener(item -> {
        mainActivity.load(new ProgramReference(fileName, "http://vintage-basic.net/bcg/" + fileName, false), true, false);
        return true;
      });
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


    SubMenu displayMenu = mainMenu.addSubMenu("Display");
    displayMenu.add("Clear").setOnMenuItemClickListener(item -> {
      mainActivity.clearScreen(Console.ClearScreenType.CLS_STATEMENT);
      return true;
    });
    displayMenu.add(1, 0, 0, "Overlay mode").setChecked(!mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = false;
      mainActivity.arrangeUi();
      return true;
    });
    displayMenu.add(1, 0, 0, "Window mode").setChecked(mainActivity.windowMode).setOnMenuItemClickListener(item -> {
      mainActivity.windowMode = true;
      mainActivity.arrangeUi();
      return true;
    });
    displayMenu.setGroupCheckable(1, true, true);
    popupMenu.show();

  }
}
