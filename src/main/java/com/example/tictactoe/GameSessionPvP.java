package com.example.tictactoe;

import java.io.PrintWriter;

public class GameSessionPvP {
    private char[] gameBoard = new char[9];
    private boolean gameOver = false;
    private int turn = 0;

    private int player1;
    private int player2;

    public GameSessionPvP(int player1, int player2) {
        this.player1 = player1;
        this.player2 = player2;

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

        // Dodajemy debug logi
        System.out.println("Otrzymano od gracza " + clientId + ": PLAYER_MOVE_PVP:" + position);
        System.out.println("Aktualna tura: " + turn);

        if (gameOver || gameBoard[position] != '-') {
            return;
        }

        // Sprawdzamy, czy to odpowiednia tura gracza
        if ((turn % 2 == 0 && clientId == player1) || (turn % 2 != 0 && clientId == player2)) {
            System.out.println("Wykonano ruch gracza " + clientId + " na pozycji " + position);
            gameBoard[position] = (turn % 2 == 0) ? 'X' : 'O';  // Gracz 1 to 'X', gracz 2 to 'O'
            sendBoardState(out1, out2);

            // Sprawdzamy warunki zakończenia gry
            if (checkWin()) {
                gameOver = true;
                sendMessage(player1, "GAME_OVER_PVP: Gracz " + clientId + " wygrał!");
                sendMessage(player2, "GAME_OVER_PVP: Gracz " + clientId + " wygrał!");
            }

            if (isBoardFull()) {
                sendMessage(player1, "GAME_OVER_PVP: Remis!");
                sendMessage(player2, "GAME_OVER_PVP: Remis!");
                gameOver = true;
            }

            turn++;

            sendMessage(player1, (turn % 2 == 0) ? "YOUR_TURN" : "ENEMY_TURN");
            sendMessage(player2, (turn % 2 != 0) ? "YOUR_TURN" : "ENEMY_TURN");
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
}
