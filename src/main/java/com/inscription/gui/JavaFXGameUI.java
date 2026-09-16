package com.inscription.gui;

import com.inscription.ui.BoardSnapshot;
import com.inscription.ui.GameUI;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.SynchronousQueue;

/**
 * JavaFX implementation of GameUI.
 * <p>
 * <b>UNVERIFIED.</b> Written without the ability to compile or run JavaFX
 * code in the sandbox this project was built in - there's no network
 * access here to download JavaFX from Maven Central at all (confirmed
 * directly: a request to repo1.maven.org returns "host_not_allowed").
 * Everything else in this project was backed by a passing regression suite
 * you could trust completely; this class has not been compiled even once.
 * Please build and exercise it for real on your own machine, and expect to
 * need to fix compile errors here - the shape should be right, but I
 * cannot promise the exact API calls are.
 * <p>
 * The problem this solves: game logic (Battle, run events, etc.) is
 * written as one long blocking sequence - call askChoice(), and the very
 * next line already has the answer. JavaFX doesn't work that way: a
 * button's click handler fires later, on the JavaFX Application Thread,
 * well after the method that created the button has already returned.
 * <p>
 * The fix: game logic runs on its own background thread (started by
 * InscriptionApp, not by this class). show() is fire-and-forget, so
 * Platform.runLater() schedules the UI update and returns immediately - no
 * blocking needed. askChoice() is different: it blocks the calling
 * (background, game-logic) thread on a SynchronousQueue until a button
 * click - running separately, on the JavaFX Application Thread - supplies
 * an answer and unblocks it. The JavaFX thread itself is never blocked;
 * only the game-logic thread waits, which is exactly the thread that's
 * supposed to be waiting for the player's input anyway.
 * <p>
 * updateBoard() refreshes a persistent GridPane in place, rather than the
 * board appearing as another wall of text appended to the scrolling log
 * every single turn - this is the actual fix for the GUI "looking like a
 * CLI with buttons," since the board now stays put on screen and just
 * updates its contents.
 */
public class JavaFXGameUI implements GameUI {

    private final VBox outputArea;
    private final VBox choiceArea;
    private final GridPane boardArea;
    private final Label bonesLabel;

    public JavaFXGameUI(VBox outputArea, VBox choiceArea, GridPane boardArea, Label bonesLabel) {
        this.outputArea = outputArea;
        this.choiceArea = choiceArea;
        this.boardArea = boardArea;
        this.bonesLabel = bonesLabel;
    }

    private static final String HEADER_MARKER = "##########";

    @Override
    public void show(String message) {
        Platform.runLater(() -> {
            if (message.startsWith(HEADER_MARKER) && message.endsWith(HEADER_MARKER)) {
                String title = message.substring(HEADER_MARKER.length(), message.length() - HEADER_MARKER.length()).trim();
                outputArea.getChildren().add(new Separator());
                Label header = new Label(title);
                header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                header.setPadding(new Insets(8, 0, 4, 0));
                outputArea.getChildren().add(header);
            } else {
                outputArea.getChildren().add(new Label(message));
            }
        });
    }

    @Override
    public int askChoice(String prompt, List<String> options) {
        SynchronousQueue<Integer> answer = new SynchronousQueue<>();
        Platform.runLater(() -> {
            choiceArea.getChildren().clear();
            choiceArea.getChildren().add(new Label(prompt));
            for (int i = 0; i < options.size(); i++) {
                int index = i;
                Button button = new Button(options.get(i));
                button.setOnAction(event -> {
                    choiceArea.getChildren().clear();
                    offer(answer, index);
                });
                choiceArea.getChildren().add(button);
            }
        });
        return take(answer);
    }

    @Override
    public void waitForContinue(String prompt) {
        // Reuses askChoice with a single option, matching a plain "Continue" button
        // visually (one button in the list) without duplicating the blocking logic.
        askChoice(prompt, List.of("Continue"));
    }

    @Override
    public void updateBoard(BoardSnapshot snapshot) {
        Platform.runLater(() -> {
            boardArea.getChildren().clear();

            boardArea.add(laneHeader("Opponent (next up)"), 0, 0);
            boardArea.add(laneHeader("Opponent (attacking)"), 0, 1);
            boardArea.add(laneHeader("You"), 0, 2);

            for (int lane = 0; lane < snapshot.getLaneCount(); lane++) {
                boardArea.add(laneCell(lane < snapshot.getOpponentReserveLanes().size()
                    ? snapshot.getOpponentReserveLanes().get(lane) : "(n/a)"), lane + 1, 0);
                boardArea.add(laneCell(snapshot.getOpponentAttackingLanes().get(lane)), lane + 1, 1);
                boardArea.add(laneCell(snapshot.getPlayerLanes().get(lane)), lane + 1, 2);
            }
        });
    }

    @Override
    public void clearBoard() {
        Platform.runLater(() -> boardArea.getChildren().clear());
    }

    @Override
    public void updateBones(int amount) {
        Platform.runLater(() -> bonesLabel.setText("Bones: " + amount));
    }

    private Label laneHeader(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private Label laneCell(String text) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(140);
        label.setPadding(new Insets(6));
        label.setStyle("-fx-border-color: gray; -fx-border-radius: 4; -fx-background-radius: 4;"
            + (text.equals("(empty)") || text.equals("(n/a)") ? " -fx-background-color: #f0f0f0;" : " -fx-background-color: #ffffff;"));
        return label;
    }

    private void offer(SynchronousQueue<Integer> queue, int value) {
        try {
            queue.put(value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int take(SynchronousQueue<Integer> queue) {
        try {
            return queue.take(); // blocks the calling (background, game-logic) thread - never the JavaFX thread
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for the player's choice", e);
        }
    }
}
