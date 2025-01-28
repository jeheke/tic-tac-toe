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

/**
 * Kontroler gry w trybie z botem. Odpowiada za zarządzanie stanem gry, interakcję z serwerem
 * oraz aktualizowanie widoku gry.
 */
public class GameControllerBot {
    @FXML
    private Button button00, button01, button02, button10, button11, button12, button20, button21, button22;
    @FXML
    private Text winnerText;

    private char[] gameBoard = new char[9]; // Tablica przechowująca stan planszy gry.
    private boolean gameOver = false; // Flaga wskazująca, czy gra się zakończyła.
    private Client client; // Instancja klienta służącego do komunikacji z serwerem.
    private Thread serverListenerThread; // Wątek nasłuchujący na wiadomości od serwera.

    /**
     * Inicjalizuje kontroler gry i rozpoczyna nasłuchiwanie na serwerze.
     */
    @FXML
    public void initialize() {
        client = Client.getInstance();
        startListeningToServer();
        // Ustawienie początkowego stanu planszy.
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }
    }

    /**
     * Obsługuje kliknięcie przycisku na planszy przez gracza.
     * Wysyła ruch do serwera i aktualizuje stan gry.
     * @param event Zdarzenie kliknięcia przycisku.
     */
    @FXML
    private void buttonClicked(ActionEvent event) {
        if (gameOver) {
            return; // Jeśli gra się zakończyła, nie wykonuj żadnych ruchów.
        }

        Button clickedButton = (Button) event.getSource();
        int position = getButtonIndex(clickedButton);

        if (position != -1 && gameBoard[position] == '-') {
            client.getOut().println("PLAYER_MOVE:" + position); // Wysłanie ruchu gracza do serwera.
        }
    }

    /**
     * Zmienia widok na menu główne, kończąc bieżącą grę.
     * @param event Zdarzenie przełączania sceny.
     * @throws IOException Gdy wystąpi problem podczas ładowania widoku.
     */
    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        client.getOut().println("END_GAME"); // Powiadomienie serwera o zakończeniu gry.

        if (serverListenerThread != null && serverListenerThread.isAlive()) {
            serverListenerThread.interrupt(); // Zakończenie wątku nasłuchującego.
        }

        gameOver = false; // Resetowanie stanu gry.
        resetGameState(); // Resetowanie planszy.

        // Ładowanie widoku menu głównego.
        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Resetuje stan gry, przygotowując nową grę.
     * @param event Zdarzenie przycisku resetowania gry.
     */
    @FXML
    private void onResetButton(ActionEvent event) {
        client.getOut().println("RESTART_GAME"); // Powiadomienie serwera o rozpoczęciu nowej gry.
        resetGameState(); // Resetowanie stanu gry lokalnie.
    }

    /**
     * Rozpoczyna nasłuchiwanie na wiadomości z serwera.
     * Wątek nasłuchujący będzie aktualizował stan gry na podstawie danych z serwera.
     */
    private void startListeningToServer() {
        serverListenerThread = new Thread(() -> {
            try {
                BufferedReader in = client.getInReader();
                String serverMessage;

                while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                    if (serverMessage.startsWith("UPDATE_BOARD:")) {
                        String boardState = serverMessage.substring("UPDATE_BOARD:".length());
                        updateGameBoard(boardState); // Aktualizowanie stanu planszy.
                    } else if (serverMessage.startsWith("GAME_OVER:")) {
                        String result = serverMessage.substring("GAME_OVER:".length());
                        updateWinnerText(result); // Aktualizowanie tekstu o zwycięzcy.
                        gameOver = true; // Gra została zakończona.
                    }
                }
            } catch (IOException e) {
                System.out.println("Błąd komunikacji z serwerem: " + e.getMessage());
            } finally {
                System.out.println("Wątek nasłuchujący został zakończony.");
            }
        });
        serverListenerThread.start(); // Uruchomienie wątku nasłuchującego.
    }

    /**
     * Aktualizuje stan gry na planszy na podstawie danych otrzymanych z serwera.
     * @param boardState Nowy stan planszy w formie tekstowej.
     */
    private void updateGameBoard(String boardState) {
        Platform.runLater(() -> {
            for (int i = 0; i < boardState.length(); i++) {
                gameBoard[i] = boardState.charAt(i);
                updateButton(i, gameBoard[i]); // Aktualizacja przycisków na planszy.
            }
        });
    }

    /**
     * Aktualizuje stan pojedynczego przycisku na planszy.
     * @param index Indeks przycisku na planszy.
     * @param value Wartość, która ma być przypisana przyciskowi ('X', 'O' lub '-').
     */
    private void updateButton(int index, char value) {
        Button button = getButtonByIndex(index);
        if (button != null) {
            if (value == 'X' || value == 'O') {
                button.setText(String.valueOf(value)); // Ustawienie tekstu przycisku.
                setColor(button, value); // Ustawienie koloru przycisku.
                button.setDisable(true); // Zablokowanie przycisku.
            } else {
                button.setText(""); // Czyszczenie tekstu przycisku.
                button.setDisable(false); // Odblokowanie przycisku.
            }
        }
    }

    /**
     * Ustawia kolor przycisku na podstawie wartości (X lub O).
     * @param button Przycisk, który ma zostać pokolorowany.
     * @param value Wartość, która decyduje o kolorze (X lub O).
     */
    private void setColor(Button button, char value) {
        if (value == 'X') {
            button.setStyle("-fx-text-fill: blue; -fx-background-color: #C3B091; -fx-opacity: 1;");
        } else if (value == 'O') {
            button.setStyle("-fx-text-fill: red; -fx-background-color: #C3B091; -fx-opacity: 1;");
        }
    }

    /**
     * Zwraca odpowiedni przycisk na podstawie indeksu.
     * @param index Indeks przycisku na planszy.
     * @return Przycisk odpowiadający indeksowi.
     */
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

    /**
     * Zwraca indeks przycisku na podstawie obiektu przycisku.
     * @param button Przycisk, którego indeks ma zostać zwrócony.
     * @return Indeks przycisku, lub -1 jeśli nie znaleziono.
     */
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

    /**
     * Aktualizuje tekst informujący o zwycięzcy gry.
     * @param result Wynik gry, który ma być wyświetlony.
     */
    private void updateWinnerText(String result) {
        Platform.runLater(() -> {
            winnerText.setText(result);
        });
    }

    /**
     * Resetuje stan gry do początkowego stanu.
     */
    private void resetGameState() {
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
            updateButton(i, '-');
        }
        winnerText.setText("Kółko i krzyżyk");
        gameOver = false;
    }
}
