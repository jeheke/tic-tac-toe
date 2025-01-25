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

public class GameControllerPvP {
    @FXML
    private Button button00, button01, button02, button10, button11, button12, button20, button21, button22;
    @FXML
    private Text winnerText, roundText, waitForPlayerText;

    private char[] gameBoard = new char[9];
    private boolean gameOver = false;
    private boolean myTurn = false;
    private Client client;
    private Thread serverListenerThread;
    private char mySymbol;
    private char enemySymbol;


    @FXML
    public void initialize() {
        client = Client.getInstance();
        startListeningToServer();

        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }

        waitForPlayerText.setText("Oczekiwanie na przeciwnika...");
        roundText.setText("");
    }

    @FXML
    public void buttonClicked(ActionEvent event) {
        if (gameOver || !myTurn) {
            return;
        }

        Button clickedButton = (Button) event.getSource();
        int position = getButtonIndex(clickedButton);

        if (position != -1 && gameBoard[position] == '-') {
            gameBoard[position] = mySymbol;
            client.getOut().println("PLAYER_MOVE_PVP:" + position);
            myTurn = false;
        }
    }

    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        client.getOut().println("END_GAME_PVP");

        if (serverListenerThread != null && serverListenerThread.isAlive()) {
            serverListenerThread.interrupt();
        }

        gameOver = false;

        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private void startListeningToServer() {
        serverListenerThread = new Thread(() -> {
            try {
                BufferedReader in = client.getInReader();
                String serverMessage;
                while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                    if (serverMessage.startsWith("UPDATE_BOARD_PVP:")) {
                        String boardState = serverMessage.substring("UPDATE_BOARD_PVP:".length());
                        updateGameBoard(boardState);
                    } else if (serverMessage.startsWith("GAME_OVER_PVP:")) {
                        String result = serverMessage.substring("GAME_OVER_PVP:".length());
                        updateWinnerText(result);
                        gameOver = true;
                        roundText.setText("");
                    } else if (serverMessage.startsWith("GAME_STARTED")) {
                        updateWaitingText(false);
                        int player = Integer.parseInt(serverMessage.substring("GAME_STARTED:".length()).trim());
                        if (player == 1) {
                            myTurn = true;
                        } else if (player == 2) {
                            myTurn = false;
                        }
                    } else if (serverMessage.equals("YOUR_TURN")) {
                            myTurn = true;
                            roundText.setText("Twoja tura");
                    } else if (serverMessage.equals("ENEMY_TURN")) {
                            myTurn = false;
                            roundText.setText("Tura przeciwnika");
                    } else if (serverMessage.startsWith("ASSIGN_SYMBOL:")) {
                        mySymbol = serverMessage.charAt("ASSIGN_SYMBOL:".length());
                        enemySymbol = (mySymbol == 'X') ? 'O' : 'X';
                    }
                }
            } catch (IOException e) {
                System.out.println("Błąd komunikacji z serwerem: " + e.getMessage());
            } finally {
                System.out.println("Wątek nasłuchujący został zakończony.");
            }
        });
        serverListenerThread.start();
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

    private void updateWaitingText(boolean waiting) {
        Platform.runLater(() -> {
            if (waiting) {
                waitForPlayerText.setText("Oczekiwanie na drugiego gracza..");
            } else {
                waitForPlayerText.setText("");
            }
        });
    }


}
