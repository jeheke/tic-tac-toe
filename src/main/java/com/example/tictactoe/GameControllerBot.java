package com.example.tictactoe;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.IOException;

public class GameControllerBot {
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

    private char[] gameBoard = new char[9];
    private boolean gameOver = false;
    private Menu client;

    @FXML
    public void initialize() {
        client = Menu.getInstance();
        startListeningToServer();

        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }
    }

    @FXML
    private void buttonClicked(ActionEvent event) {
        if (gameOver) {
            return;
        }

        Button clickedButton = (Button) event.getSource();
        int position = getButtonIndex(clickedButton);

        if (position != -1 && gameBoard[position] == '-') {
            client.getOut().println("PLAYER_MOVE:" + position); // Wysłanie ruchu do serwera
        }
    }

    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        // Informowanie serwera o zakończeniu gry
        client.getOut().println("END_GAME");
        gameOver = false;
        resetGameState();
        // Przełączanie do menu
        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void handleWindowClose(WindowEvent event) {
        // Informowanie serwera o zakończeniu gry przed zamknięciem aplikacji
        client.getOut().println("END_GAME");
    }

    private void startListeningToServer() {
        new Thread(() -> {
            try {
                BufferedReader in = client.getInReader();
                String serverMessage;

                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.startsWith("UPDATE_BOARD:")) {
                        String boardState = serverMessage.substring("UPDATE_BOARD:".length());
                        updateGameBoard(boardState);
                    } else if (serverMessage.startsWith("GAME_OVER:")) {
                        String result = serverMessage.substring("GAME_OVER:".length());
                        updateWinnerText(result);
                        gameOver = true;
                    }
                }
            } catch (IOException e) {
                System.out.println("Błąd komunikacji z serwerem: " + e.getMessage());
            }
        }).start();
    }

    private void updateGameBoard(String boardState) {
        Platform.runLater(() -> {
            for (int i = 0; i < boardState.length(); i++) {
                gameBoard[i] = boardState.charAt(i);
                updateButton(i, gameBoard[i]);
            }
        });
    }

    private void updateButton(int index, char value) {
        Button button = getButtonByIndex(index);
        if (button != null) {
            if (value == 'X' || value == 'O') {
                button.setText(String.valueOf(value));
                setColor(button, value);
                button.setDisable(true);
            } else {
                button.setText("");
                button.setDisable(false);
            }
        }
    }

    private void setColor(Button button, char value) {
        if (value == 'X') {
            button.setStyle("-fx-text-fill: blue; -fx-background-color: #C3B091; -fx-opacity: 1;");
        } else if (value == 'O') {
            button.setStyle("-fx-text-fill: red; -fx-background-color: #C3B091; -fx-opacity: 1;");
        }
    }

    private Button getButtonByIndex(int index) {
        return switch (index) {
            case 0 -> button00;
            case 1 -> button01;
            case 2 -> button02;
            case 3 -> button10;
            case 4 -> button11;
            case 5 -> button12;
            case 6 -> button20;
            case 7 -> button21;
            case 8 -> button22;
            default -> null;
        };
    }

    private int getButtonIndex(Button button) {
        if (button == button00) return 0;
        if (button == button01) return 1;
        if (button == button02) return 2;
        if (button == button10) return 3;
        if (button == button11) return 4;
        if (button == button12) return 5;
        if (button == button20) return 6;
        if (button == button21) return 7;
        if (button == button22) return 8;
        return -1;
    }

    private void updateWinnerText(String result) {
        Platform.runLater(() -> {
            winnerText.setText(result);
        });
    }

    private void resetGameState() {
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
            updateButton(i, '-');
        }
        winnerText.setText("");
        gameOver = false;
    }

    @FXML
    private void onResetButton(ActionEvent event) {
        client.getOut().println("RESTART_GAME");
        resetGameState();
    }
}
