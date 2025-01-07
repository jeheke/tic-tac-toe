package com.example.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.net.URL;
import java.util.Arrays;

public class botLogic implements Initializable {
    @FXML
    private Button button00;
    @FXML
    private Button button01;
    @FXML
    private Button button02;
    @FXML
    private Button button10;
    @FXML
    private Button button11;
    @FXML
    private Button button12;
    @FXML
    private Button button20;
    @FXML
    private Button button21;
    @FXML
    private Button button22;
    @FXML
    private Text winnerText;

    private int playerTurn = 0;
    ArrayList<Button> buttons;
    private String difficulty;
    private boolean gameOver = false;

    @Override
    public void initialize(URL url, ResourceBundle resource) {
        buttons = new ArrayList<>(Arrays.asList(button00, button01, button02, button10, button11, button12, button20, button21, button22));
        buttons.forEach(button -> {
            setupButton(button);
            button.setFocusTraversable(false);


        });
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    @FXML
    void restart(ActionEvent event) {
        // Resetowanie stanu gry
        buttons.forEach(this::resetButton);
        winnerText.setText("Kółko i krzyżyk");


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-pvp.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();


            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resetButton(Button button) {
        button.setDisable(false);
        button.setText("");
    }

    private void setupButton(Button button) {
        button.setOnMouseClicked(event -> {
            setSymbol(button, "X");
            button.setDisable(true);
            playerTurn++;
            checkWinCondition();

            if (!gameOver && playerTurn % 2 == 1) {
                botMove();
            }
        });
    }

    private void botMove() {
        if (difficulty.equals("normal")) {
            randomMove();
        } else {
            smartMove();
        }
        checkWinCondition();
    }

    private void randomMove() {
        Random rand = new Random();
        ArrayList<Button> availableButtons = new ArrayList<>();
        for (Button button : buttons) {
            if (button.getText().isEmpty()) {
                availableButtons.add(button);
            }
        }
        if (!availableButtons.isEmpty()) {
            Button randomButton = availableButtons.get(rand.nextInt(availableButtons.size()));
            setSymbol(randomButton, "O");
            randomButton.setDisable(true);
            playerTurn++;
        }
    }

    private void smartMove() {
        for (Button button : buttons) {
            if (button.getText().isEmpty()) {
                setSymbol(button, "O");
                if (checkForWin("O")) {
                    return;
                }
                button.setText("");
            }
        }

        // Jeśli nie może wygrać, blokuje gracza
        for (Button button : buttons) {
            if (button.getText().isEmpty()) {
                setSymbol(button, "X");
                if (checkForWin("X")) {
                    setSymbol(button, "O");
                    button.setDisable(true);
                    playerTurn++;
                    return;
                }
                button.setText("");
            }
        }

        // Jeśli nic nie można zrobić, wykonuje ruch losowy
        randomMove();
    }

    private boolean checkForWin(String symbol) {
        for (int i = 0; i < 8; i++) {
            String check = switch (i) {
                case 0 -> button00.getText() + button01.getText() + button02.getText();
                case 1 -> button10.getText() + button11.getText() + button12.getText();
                case 2 -> button20.getText() + button21.getText() + button22.getText();
                case 3 -> button00.getText() + button10.getText() + button20.getText();
                case 4 -> button01.getText() + button11.getText() + button21.getText();
                case 5 -> button02.getText() + button12.getText() + button22.getText();
                case 6 -> button00.getText() + button11.getText() + button22.getText();
                case 7 -> button02.getText() + button11.getText() + button20.getText();
                default -> null;
            };
            if (check.equals(symbol + symbol + symbol)) {
                return true;
            }
        }
        return false;
    }

    public void checkWinCondition() {
        for (int i = 0; i < 8; i++) {
            String check = switch (i) {
                case 0 -> button00.getText() + button01.getText() + button02.getText();
                case 1 -> button10.getText() + button11.getText() + button12.getText();
                case 2 -> button20.getText() + button21.getText() + button22.getText();
                case 3 -> button00.getText() + button10.getText() + button20.getText();
                case 4 -> button01.getText() + button11.getText() + button21.getText();
                case 5 -> button02.getText() + button12.getText() + button22.getText();
                case 6 -> button00.getText() + button11.getText() + button22.getText();
                case 7 -> button02.getText() + button11.getText() + button20.getText();
                default -> null;
            };

            if (check.equals("XXX")) {
                winnerText.setText("Wygrał krzyżyk");
                disableAllButtons();
                gameOver = true;
                return;
            } else if (check.equals("OOO")) {
                winnerText.setText("Wygrał bot");
                disableAllButtons();
                gameOver = true;
                return;
            }

        }

        boolean isDraw = buttons.stream().allMatch(button -> !button.getText().isEmpty());
        if (isDraw) {
            winnerText.setText("Remis");
            disableAllButtons();
            gameOver = true;
        }

    }

    private void setSymbol(Button button, String symbol) {
        button.setText(symbol);
        if (symbol.equals("X")) {
            button.setStyle("-fx-text-fill: blue;");
        } else if (symbol.equals("O")) {
            button.setStyle("-fx-text-fill: red;");
        }
    }

    private void disableAllButtons() {
        buttons.forEach(button -> button.setDisable(true));
    }

    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
