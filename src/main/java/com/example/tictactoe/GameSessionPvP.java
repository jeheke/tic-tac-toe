package com.example.tictactoe;

import java.io.PrintWriter;

public class GameSessionPvP {
    private char[] gameBoard = new char[9];
    private boolean gameOver = false;

    private int player1;
    private int player2;
    private int currentPlayer;
    private boolean player1ReturnedToMenu = false;
    private boolean player2ReturnedToMenu = false;

    public GameSessionPvP(int player1, int player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;

        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }

        System.out.println("Rozpoczęto nową grę pvp");
        sendMessage(player1, "GAME_STARTED: 1");
        sendMessage(player2, "GAME_STARTED: 2");
        sendMessage(player1, "ASSIGN_SYMBOL:X");
        sendMessage(player2, "ASSIGN_SYMBOL:O");
    }

    public synchronized void handlePlayerMove(int clientId, int position) {
        PrintWriter out1 = Server.getClientOutputStream(player1);
        PrintWriter out2 = Server.getClientOutputStream(player2);

        // Logi debugowe
        System.out.println("Otrzymano od gracza " + clientId + ": PLAYER_MOVE_PVP:" + position);
        System.out.println("Aktualny gracz: " + currentPlayer);

        // Sprawdzamy, czy gra jest zakończona lub czy pole już zostało zajęte
        if (gameOver || gameBoard[position] != '-') {
            return;
        }

        // Sprawdzamy, czy ruch wykonuje aktualny gracz
        if (clientId == currentPlayer) {
            System.out.println("Wykonano ruch gracza " + clientId + " na pozycji " + position);
            gameBoard[position] = (currentPlayer == player1) ? 'X' : 'O'; // Gracz 1 to 'X', gracz 2 to 'O'
            sendBoardState(out1, out2);

            // Sprawdzamy warunki zakończenia gry
            if (checkWin()) {
                gameOver = true;
                sendMessage(player1, "GAME_OVER_PVP: Gracz " + clientId + " wygrał!");
                sendMessage(player2, "GAME_OVER_PVP: Gracz " + clientId + " wygrał!");
                return; // Koniec gry
            }

            // Sprawdzamy, czy plansza jest pełna
            if (isBoardFull()) {
                sendMessage(player1, "GAME_OVER_PVP: Remis!");
                sendMessage(player2, "GAME_OVER_PVP: Remis!");
                gameOver = true;
                return; // Koniec gry
            }

            // Przełączamy na następnego gracza
            currentPlayer = (currentPlayer == player1) ? player2 : player1;

            // Powiadamianie graczy o aktualnej turze
            sendMessage(player1, (currentPlayer == player1) ? "YOUR_TURN" : "ENEMY_TURN");
            sendMessage(player2, (currentPlayer == player2) ? "YOUR_TURN" : "ENEMY_TURN");
        } else {
            System.out.println("Gracz " + clientId + " próbował wykonać ruch, ale to nie jego tura.");
            sendMessage(clientId, "Nie Twoja tura!");
        }
    }

    private void sendBoardState(PrintWriter out1, PrintWriter out2) {
        String boardState = "UPDATE_BOARD_PVP:" + String.valueOf(gameBoard);
        System.out.println("Wysyłanie stanu planszy: " + boardState); // Debug log
        if (out1 != null) {
            out1.println(boardState);
            out1.flush();
        }
        if (out2 != null) {
            out2.println(boardState);
            out2.flush();
        }
    }

    private boolean checkWin() {
        int[][] winPatterns = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };

        for (int[] pattern : winPatterns) {
            if (gameBoard[pattern[0]] != '-' && gameBoard[pattern[0]] == gameBoard[pattern[1]] && gameBoard[pattern[1]] == gameBoard[pattern[2]]) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull() {
        for (char c : gameBoard) {
            if (c == '-') {
                return false;
            }
        }
        return true;
    }

    private void sendMessage(int clientId, String message) {
        PrintWriter out = Server.getClientOutputStream(clientId);
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    public void setPlayer1ReturnedToMenu(boolean returned) {
        this.player1ReturnedToMenu = returned;
    }

    public void setPlayer2ReturnedToMenu(boolean returned) {
        this.player2ReturnedToMenu = returned;
    }

    public boolean haveBothPlayersReturnedToMenu() {
        return player1ReturnedToMenu && player2ReturnedToMenu;
    }

    public int getPlayer1Id() {
        return player1;
    }

    public int getPlayer2Id() {
        return player2;
    }
}
