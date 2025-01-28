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

/**
 * Klasa reprezentująca logikę bota w grze "Kółko i Krzyżyk".
 * Obsługuje ruchy gracza i bota oraz sprawdza warunki zwycięstwa.
 */
public class botLogic implements Initializable {
    @FXML
    private Button button00, button01, button02, button10, button11, button12, button20, button21, button22;
    @FXML
    private Text winnerText;

    private int playerTurn = 0; // Liczba ruchów gracza.
    private ArrayList<Button> buttons; // Lista przycisków reprezentujących planszę.
    private String difficulty; // Poziom trudności gry.
    private boolean gameOver = false; // Flaga oznaczająca, czy gra jest zakończona.

    /**
     * Inicjalizuje przyciski oraz ustawia zdarzenia dla każdego z nich.
     * @param url URL, którego używa FXML.
     * @param resource Zasoby do użycia w FXML.
     */
    @Override
    public void initialize(URL url, ResourceBundle resource) {
        buttons = new ArrayList<>(Arrays.asList(button00, button01, button02, button10, button11, button12, button20, button21, button22));
        buttons.forEach(button -> {
            setupButton(button);
            button.setFocusTraversable(false); // Wyłącza focus na przyciskach.
        });
    }

    /**
     * Ustawia poziom trudności gry (normalny lub inteligentny bot).
     * @param difficulty Poziom trudności.
     */
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Rozpoczyna nową grę, resetując wszystkie przyciski.
     * @param event Zdarzenie wywołane przyciskiem "restart".
     */
    @FXML
    void restart(ActionEvent event) {
        buttons.forEach(this::resetButton);
        winnerText.setText("Kółko i krzyżyk");
        playerTurn = 0;
        gameOver = false;
    }

    /**
     * Resetuje stan przycisku do początkowego (pusty tekst i włączony przycisk).
     * @param button Przycisk do zresetowania.
     */
    public void resetButton(Button button) {
        button.setDisable(false);
        button.setText("");
    }

    /**
     * Ustawia zdarzenie dla przycisku, aby po kliknięciu ustawić symbol gracza i sprawdzić warunki zwycięstwa.
     * @param button Przycisk do ustawienia zdarzenia.
     */
    private void setupButton(Button button) {
        button.setOnMouseClicked(event -> {
            setSymbol(button, "X"); // Ustawia symbol "X" dla gracza.
            button.setDisable(true); // Dezaktywuje przycisk po użyciu.
            playerTurn++;
            checkWinCondition(); // Sprawdza, czy gracz wygrał.

            if (!gameOver && playerTurn % 2 == 1) { // Jeżeli gra nie zakończona, wykonuje ruch bota.
                botMove();
            }
        });
    }

    /**
     * Zleca botowi wykonanie ruchu, wybierając sposób zależnie od poziomu trudności.
     */
    private void botMove() {
        if (difficulty.equals("normal")) {
            randomMove(); // Ruch losowy.
        } else {
            smartMove(); // Ruch inteligentny.
        }
        checkWinCondition(); // Sprawdza, czy bot wygrał.
    }

    /**
     * Wykonuje losowy ruch bota.
     */
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
            setSymbol(randomButton, "O"); // Ustawia symbol "O" dla bota.
            randomButton.setDisable(true); // Dezaktywuje przycisk po ruchu bota.
            playerTurn++;
        }
    }

    /**
     * Wykonuje inteligentny ruch bota, sprawdzając możliwość wygranej lub blokując gracza.
     */
    private void smartMove() {
        // Sprawdzanie możliwości wygranej.
        for (Button button : buttons) {
            if (button.getText().isEmpty()) {
                setSymbol(button, "O");
                if (checkForWin("O")) { // Jeżeli bot może wygrać, wykonuje ruch.
                    return;
                }
                button.setText(""); // Anuluje ruch, jeżeli nie prowadzi do wygranej.
            }
        }

        // Blokowanie gracza przed wygraną.
        for (Button button : buttons) {
            if (button.getText().isEmpty()) {
                setSymbol(button, "X");
                if (checkForWin("X")) { // Jeżeli gracz może wygrać, blokuje ruch.
                    setSymbol(button, "O");
                    button.setDisable(true);
                    playerTurn++;
                    return;
                }
                button.setText(""); // Anuluje ruch, jeżeli nie blokuje wygranej.
            }
        }

        // Jeżeli bot nie może wygrać ani zablokować gracza, wykonuje ruch losowy.
        randomMove();
    }

    /**
     * Sprawdza, czy dany symbol (X lub O) wygrał.
     * @param symbol Symbol, który sprawdzamy ("X" lub "O").
     * @return Zwraca true, jeśli gracz lub bot wygrał, w przeciwnym razie false.
     */
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

    /**
     * Sprawdza ogólny stan gry, czy ktoś wygrał, czy jest remis.
     */
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

        // Sprawdza, czy gra zakończyła się remisem.
        boolean isDraw = buttons.stream().allMatch(button -> !button.getText().isEmpty());
        if (isDraw) {
            winnerText.setText("Remis");
            disableAllButtons();
            gameOver = true;
        }
    }

    /**
     * Ustawia symbol na przycisku oraz zmienia jego styl.
     * @param button Przycisk, na którym ustawiamy symbol.
     * @param symbol Symbol do ustawienia ("X" lub "O").
     */
    private void setSymbol(Button button, String symbol) {
        button.setText(symbol);
        if (symbol.equals("X")) {
            button.setStyle("-fx-text-fill: blue; -fx-background-color: #C3B091; -fx-opacity: 1;");
        } else if (symbol.equals("O")) {
            button.setStyle("-fx-text-fill: red; -fx-background-color: #C3B091; -fx-opacity: 1;");
        }
    }

    /**
     * Dezaktywuje wszystkie przyciski po zakończeniu gry.
     */
    private void disableAllButtons() {
        buttons.forEach(button -> button.setDisable(true));
        buttons.forEach(button -> button.setStyle("-fx-opacity: 1; -fx-background-color: #C3B091;"));
    }

    /**
     * Przechodzi do menu głównego.
     * @param event Zdarzenie wywołane przez przycisk.
     * @throws IOException Wyrzuca wyjątek w przypadku problemu z ładowaniem pliku FXML.
     */
    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("main-menu.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
