package com.example.tictactoe;

import java.io.PrintWriter;
import java.util.Random;

/**
 * Klasa obsługująca sesję gry z botem.
 */
public class GameSessionBot {
    private int playerTurn = 0;
    private char[] gameBoard = new char[9];
    private String difficulty;
    private int clientId;
    private boolean gameOver = false;

    /**
     * Konstruktor sesji gry z botem.
     *
     * @param difficulty Poziom trudności gry ("Łatwy" lub "Trudny").
     * @param clientId   Identyfikator klienta.
     */
    public GameSessionBot(String difficulty, int clientId) {
        this.difficulty = difficulty;
        this.clientId = clientId;

        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }

        System.out.println("Stworzono nową sesję gry dla Gracza " + clientId + " na poziomie trudności: " + difficulty);
    }

    /**
     * Obsługuje ruch gracza.
     *
     * @param position Pozycja, na którą gracz wykonał ruch.
     */
    public void handlePlayerMove(int position) {
        if (gameOver || gameBoard[position] != '-') {
            return;
        }
        gameBoard[position] = 'X';
        playerTurn = 1;
        sendToClient("UPDATE_BOARD:" + String.valueOf(gameBoard));
        checkGameState();

        if (!gameOver) {
            botMove();
        }
    }

    /**
     * Wykonuje ruch bota.
     */
    private void botMove() {
        int move = -1;

        if (difficulty.equals("Trudny")) {
            move = findBestMove();
        }
        if (move == -1) {
            move = findRandomMove();
        }
        if (move != -1) {
            gameBoard[move] = 'O';
            playerTurn = 0;

            sendToClient("UPDATE_BOARD:" + String.valueOf(gameBoard));
            checkGameState();
        }
    }

    /**
     * Znajduje najlepszy możliwy ruch dla bota w trybie "Trudny".
     *
     * @return Indeks najlepszego ruchu lub -1, jeśli brak możliwych ruchów.
     */
    private int findBestMove() {
        for (int i = 0; i < gameBoard.length; i++) {
            if (gameBoard[i] == '-') {
                gameBoard[i] = 'O';
                if (checkWin('O')) {
                    gameBoard[i] = '-';
                    return i;
                }
                gameBoard[i] = '-';
            }
        }
        for (int i = 0; i < gameBoard.length; i++) {
            if (gameBoard[i] == '-') {
                gameBoard[i] = 'X';
                if (checkWin('X')) {
                    gameBoard[i] = '-';
                    return i;
                }
                gameBoard[i] = '-';
            }
        }
        return -1;
    }

    /**
     * Wybiera losowy ruch dla bota.
     *
     * @return Indeks losowego ruchu.
     */
    private int findRandomMove() {
        Random random = new Random();
        int move;
        do {
            move = random.nextInt(9);
        } while (gameBoard[move] != '-');
        return move;
    }

    /**
     * Sprawdza stan gry po każdym ruchu.
     */
    private void checkGameState() {
        if (checkWin('X')) {
            sendToClient("GAME_OVER:Wygrałeś!");
            gameOver = true;
        } else if (checkWin('O')) {
            sendToClient("GAME_OVER:Przegrałeś!");
            gameOver = true;
        } else if (isBoardFull()) {
            sendToClient("GAME_OVER:Remis!");
            gameOver = true;
        }
    }

    /**
     * Sprawdza, czy podany symbol spełnia warunki zwycięstwa.
     *
     * @param symbol Symbol do sprawdzenia ('X' lub 'O').
     * @return {@code true}, jeśli warunki zwycięstwa są spełnione; {@code false} w przeciwnym wypadku.
     */
    private boolean checkWin(char symbol) {
        int[][] winConditions = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] condition : winConditions) {
            if (gameBoard[condition[0]] == symbol &&
                    gameBoard[condition[1]] == symbol &&
                    gameBoard[condition[2]] == symbol) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza, czy plansza jest pełna.
     *
     * @return {@code true}, jeśli plansza jest pełna; {@code false} w przeciwnym wypadku.
     */
    private boolean isBoardFull() {
        for (char c : gameBoard) {
            if (c == '-') {
                return false;
            }
        }
        return true;
    }

    /**
     * Wysyła wiadomość do klienta.
     *
     * @param message Treść wiadomości.
     */
    private void sendToClient(String message) {
        try {
            PrintWriter out = Server.getClientOutputStream(clientId);
            if (out != null) {
                out.println(message);
            }
        } catch (Exception e) {
            System.out.println("Błąd wysyłania wiadomości do klienta: " + e.getMessage());
        }
    }

    /**
     * Resetuje stan gry, umożliwiając rozpoczęcie nowej rundy.
     */
    public void restart() {
        gameOver = false;
        playerTurn = 0;
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }
        sendToClient("UPDATE_BOARD:" + String.valueOf(gameBoard));
    }
}
