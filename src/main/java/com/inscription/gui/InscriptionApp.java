package com.inscription.gui;

import com.inscription.cli.RunGame;
import com.inscription.deck.StarterDecks;
import com.inscription.pelt.PeltKind;
import com.inscription.player.Player;
import com.inscription.run.PathRunner;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX entry point.
 * <p>
 * <b>UNVERIFIED</b> - see JavaFXGameUI's class-level note; this has not
 * been compiled in this sandbox, only reasoned through carefully.
 * <p>
 * Layout: a persistent board grid pinned to the top (updated in place by
 * JavaFXGameUI.updateBoard(), never appended to), a scrolling log in the
 * middle for show() output (auto-scrolls to the newest message as it
 * arrives), and a row of buttons at the bottom for askChoice(). Still not
 * real card art or a polished visual style - the goal here was fixing the
 * two concrete complaints (the board printing itself over and over instead
 * of updating in place, and having to manually scroll to see new
 * messages) before spending more effort on visual polish.
 * <p>
 * Reuses RunGame's own buildWoodlandsPath() / buildLocation2Path() /
 * buildLocation3Path() (made public specifically for this) rather than
 * duplicating any deck content here - the only thing genuinely new in this
 * class is how the player's choices get collected.
 */
public class InscriptionApp extends Application {

    @Override
    public void start(Stage stage) {
        GridPane boardArea = new GridPane();
        boardArea.setPadding(new Insets(8));
        boardArea.setHgap(8);
        boardArea.setVgap(4);

        Label bonesLabel = new Label("Bones: 0");
        bonesLabel.setStyle("-fx-font-weight: bold; -fx-padding: 8 8 0 8;");

        VBox topArea = new VBox(bonesLabel, boardArea);

        VBox outputArea = new VBox(4);
        outputArea.setPadding(new Insets(8));
        ScrollPane scrollPane = new ScrollPane(outputArea);
        scrollPane.setFitToWidth(true);

        // Auto-scroll to the newest message whenever one is added - fires whenever
        // the log's own height changes, which happens exactly when a child is added.
        outputArea.heightProperty().addListener((observable, oldValue, newValue) -> scrollPane.setVvalue(1.0));

        VBox choiceArea = new VBox(4);
        choiceArea.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setTop(topArea);
        root.setCenter(scrollPane);
        root.setBottom(choiceArea);

        stage.setScene(new Scene(root, 700, 550));
        stage.setTitle("Inscription");
        stage.show();

        JavaFXGameUI ui = new JavaFXGameUI(outputArea, choiceArea, boardArea, bonesLabel);

        // Game logic runs on its own background thread - it's written as one long
        // blocking sequence (see JavaFXGameUI's note on why), which would freeze the
        // JavaFX Application Thread solid - and the whole window with it - if run here directly.
        Thread gameThread = new Thread(() -> runGame(ui));
        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void runGame(JavaFXGameUI ui) {
        ui.show("==============================");
        ui.show("          INSCRIPTION");
        ui.show("==============================");
        int choice = ui.askChoice("Choose an option:", java.util.List.of("Start", "Quit"));
        if (choice == 1) {
            ui.show("Goodbye!");
            javafx.application.Platform.exit();
            return;
        }

        Player player = new Player("You", StarterDecks.beginnerAnimalDeck(), StarterDecks.beginnerSquirrelDeck());
        player.gainPelt(PeltKind.RABBIT);
        player.gainPelt(PeltKind.RABBIT);

        PathRunner runner = new PathRunner(player, ui);
        ui.show("=== Inscription: a run begins ===");
        if (!runner.run(RunGame.buildWoodlandsPath())) {
            return;
        }
        ui.show("Woodlands complete! Entering Location 2");
        if (!runner.run(RunGame.buildLocation2Path())) {
            return;
        }
        ui.show("Location 2 complete! Entering Location 3");
        runner.run(RunGame.buildLocation3Path());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
