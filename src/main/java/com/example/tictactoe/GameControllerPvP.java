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
 * Kontroler gry PvP (Player vs Player) w grze kółko i krzyżyk
 * Obsługuje logikę gry, interakcje z serwerem oraz zarządza interfejsem użytkownika.
 */
public class GameControllerPvP {
    @FXML
    private Button button00, button01, button02, button10, button11, button12, button20, button21, button22;
    @FXML
    private Text winnerText, roundText, waitForPlayerText;

    private char[] gameBoard = new char[9]; // Tablica przechowująca stan planszy gry
    private boolean gameOver = false; // Flaga informująca, czy gra zakończona
    private boolean myTurn = false; // Flaga informująca, czy to moja tura
    private Client client; // Obiekt klienta komunikującego się z serwerem
    private Thread serverListenerThread; // Wątek nasłuchujący komunikaty z serwera
    private char mySymbol; // Mój symbol (X lub O)
    private char enemySymbol; // Symbol przeciwnika

    /**
     * Inicjalizuje grę i rozpoczyna nasłuchiwanie serwera.
     * Ustawia początkowy stan gry.
     */
    @FXML
    public void initialize() {
        client = Client.getInstance();
        startListeningToServer(); // Rozpocznij nasłuchiwanie serwera

        // Ustawienie początkowego stanu planszy
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }

        // Ustawienie komunikatu oczekiwania na przeciwnika
        waitForPlayerText.setText("Oczekiwanie na przeciwnika...");
        roundText.setText("");
    }

    /**
     * Obsługuje kliknięcie przycisku na planszy.
     * Wykonuje ruch, jeśli to moja tura i gra nie jest zakończona.
     *
     * @param event Zdarzenie kliknięcia przycisku
     */
    @FXML
    public void buttonClicked(ActionEvent event) {
        if (gameOver || !myTurn) {
            return; // Ignoruj kliknięcia, jeśli gra jest zakończona lub nie moja tura
        }

        Button clickedButton = (Button) event.getSource();
        int position = getButtonIndex(clickedButton); // Pobierz indeks klikniętego przycisku

        if (position != -1 && gameBoard[position] == '-') {
            gameBoard[position] = mySymbol; // Zaktualizuj stan planszy
            client.getOut().println("PLAYER_MOVE_PVP:" + position); // Wyślij ruch do serwera
            myTurn = false; // Zmiana tury
        }
    }

    /**
     * Przełącza do menu głównego po zakończeniu gry.
     * Kończy nasłuchiwanie serwera i resetuje stan gry.
     *
     * @param event Zdarzenie kliknięcia przycisku powrotu do menu
     * @throws IOException W przypadku błędu przy ładowaniu widoku
     */
    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        client.getOut().println("END_GAME_PVP"); // Powiadomienie serwera o zakończeniu gry

        // Zatrzymaj wątek nasłuchujący serwera, jeśli jest aktywny
        if (serverListenerThread != null && serverListenerThread.isAlive()) {
            serverListenerThread.interrupt();
        }

        gameOver = false; // Zresetuj stan gry

        // Przeładuj widok menu głównego
        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Rozpoczyna nasłuchiwanie wiadomości z serwera w nowym wątku.
     * Aktualizuje stan gry na podstawie otrzymanych komunikatów.
     */
    private void startListeningToServer() {
        serverListenerThread = new Thread(() -> {
            try {
                BufferedReader in = client.getInReader();
                String serverMessage;
                while (!Thread.currentThread().isInterrupted() && (serverMessage = in.readLine()) != null) {
                    // Przetwarzanie wiadomości z serwera
                    if (serverMessage.startsWith("UPDATE_BOARD_PVP:")) {
                        String boardState = serverMessage.substring("UPDATE_BOARD_PVP:".length());
                        updateGameBoard(boardState); // Zaktualizuj stan planszy
                    } else if (serverMessage.startsWith("GAME_OVER_PVP:")) {
                        String result = serverMessage.substring("GAME_OVER_PVP:".length());
                        updateWinnerText(result); // Zaktualizuj wynik gry
                        gameOver = true; // Ustaw stan zakończenia gry
                        roundText.setText("");
                    } else if (serverMessage.startsWith("GAME_STARTED")) {
                        updateWaitingText(false); // Zaktualizuj komunikat o oczekiwaniu
                        int player = Integer.parseInt(serverMessage.substring("GAME_STARTED:".length()).trim());
                        myTurn = player == 1; // Ustal, kto zaczyna grę
                    } else if (serverMessage.equals("YOUR_TURN")) {
                        myTurn = true; // Moja tura
                        roundText.setText("Twoja tura");
                    } else if (serverMessage.equals("ENEMY_TURN")) {
                        myTurn = false; // Tura przeciwnika
                        roundText.setText("Tura przeciwnika");
                    } else if (serverMessage.startsWith("ASSIGN_SYMBOL:")) {
                        mySymbol = serverMessage.charAt("ASSIGN_SYMBOL:".length());
                        enemySymbol = (mySymbol == 'X') ? 'O' : 'X'; // Przypisanie symboli
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

    /**
     * Aktualizuje stan planszy gry na podstawie komunikatu z serwera.
     *
     * @param boardState Nowy stan planszy
     */
    private void updateGameBoard(String boardState) {
        Platform.runLater(() -> {
            for (int i = 0; i < boardState.length(); i++) {
                gameBoard[i] = boardState.charAt(i);
                updateButton(i, gameBoard[i]); // Zaktualizuj przycisk na planszy
            }
        });
    }

    /**
     * Aktualizuje tekst na przycisku na planszy.
     *
     * @param index Indeks przycisku na planszy
     * @param value Nowa wartość (X, O lub puste)
     */
    private void updateButton(int index, char value) {
        Button button = getButtonByIndex(index);
        if (button != null) {
            if (value == 'X' || value == 'O') {
                button.setText(String.valueOf(value)); // Ustaw tekst na przycisku
                setColor(button, value); // Ustaw kolor przycisku
                button.setDisable(true); // Zablokuj przycisk po wykonaniu ruchu
            } else {
                button.setText(""); // Wyczyść przycisk
                button.setDisable(false); // Odblokuj przycisk
            }
        }
    }

    /**
     * Ustawia kolor przycisku w zależności od wartości (X lub O).
     *
     * @param button Przycisk, którego kolor ma zostać zmieniony
     * @param value Wartość (X lub O)
     */
    private void setColor(Button button, char value) {
        if (value == 'X') {
            button.setStyle("-fx-text-fill: blue; -fx-background-color: #C3B091; -fx-opacity: 1;");
        } else if (value == 'O') {
            button.setStyle("-fx-text-fill: red; -fx-background-color: #C3B091; -fx-opacity: 1;");
        }
    }

    /**
     * Zwraca przycisk odpowiadający danemu indeksowi w tablicy.
     *
     * @param index Indeks przycisku
     * @return Przycisk na planszy
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
     * Zwraca indeks przycisku na planszy na podstawie obiektu Button.
     *
     * @param button Przycisk
     * @return Indeks przycisku w tablicy
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
     * Aktualizuje tekst informujący o zwycięzcy.
     *
     * @param result Wynik gry (zwycięzca lub remis)
     */
    private void updateWinnerText(String result) {
        Platform.runLater(() -> {
            winnerText.setText(result); // Wyświetl wynik
        });
    }

    /**
     * Aktualizuje tekst informujący o oczekiwaniach.
     *
     * @param waiting Flaga informująca, czy oczekujemy na drugiego gracza
     */
    private void updateWaitingText(boolean waiting) {
        Platform.runLater(() -> {
            if (waiting) {
                waitForPlayerText.setText("Oczekiwanie na drugiego gracza..");
            } else {
                waitForPlayerText.setText(""); // Wyczyść tekst po rozpoczęciu gry
            }
        });
    }
}
