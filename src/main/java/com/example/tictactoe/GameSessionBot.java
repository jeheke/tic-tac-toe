package com.example.tictactoe;

import java.io.PrintWriter;
import java.util.Random;

public class GameSessionBot {
    private int playerTurn = 0; // 0: player, 1: bot
    private char[] gameBoard = new char[9];
    private String difficulty;
    private int clientId;
    private boolean gameOver = false;

    public GameSessionBot(String difficulty, int clientId) {
        this.difficulty = difficulty;
        this.clientId = clientId;

        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }

        System.out.println("Rozpoczęto nową grę z botem.");
        System.out.println("Poziom trudności: " + difficulty + ", Gracz ID: " + clientId);
    }

    public void processClientMessage(String message) {
        if (message.startsWith("PLAYER_MOVE:")) {
            int position = Integer.parseInt(message.substring("PLAYER_MOVE:".length()));
            handlePlayerMove(position);
        }
    }

    public void handlePlayerMove(int position) {
        if (gameOver || gameBoard[position] != '-') {
            return;
        }

        gameBoard[position] = 'X';
        playerTurn = 1; // Switch to bot's turn

        sendToClient("UPDATE_BOARD:" + String.valueOf(gameBoard)); // Send updated board to client
        checkGameState(); // Check the game state

        if (!gameOver) {
            botMove();
        }
    }

    private void botMove() {
        int move = -1;

        if (difficulty.equals("Hard")) {
            move = findBestMove();
        }

        if (move == -1) { // No best move found, or difficulty is Normal
            move = findRandomMove();
        }

        if (move != -1) {
            gameBoard[move] = 'O';
            playerTurn = 0; // Switch back to player's turn

            sendToClient("UPDATE_BOARD:" + String.valueOf(gameBoard)); // Send updated board to client before checking win
            checkGameState(); // Check the game state after the bot's move
        }
    }

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

    private int findRandomMove() {
        Random random = new Random();
        int move;
        do {
            move = random.nextInt(9);
        } while (gameBoard[move] != '-');

        return move;
    }

    private void checkGameState() {
        if (checkWin('X')) {
            sendToClient("GAME_OVER:Player wins!");
            gameOver = true;
        } else if (checkWin('O')) {
            sendToClient("GAME_OVER:Bot wins!");
            gameOver = true;
        } else if (isBoardFull()) {
            sendToClient("GAME_OVER:Draw!");
            gameOver = true;
        }
    }

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

    private boolean isBoardFull() {
        for (char c : gameBoard) {
            if (c == '-') {
                return false;
            }
        }
        return true;
    }

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

    public void restart() {
        gameOver = false;
        playerTurn = 0;
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }
        sendToClient("UPDATE_BOARD:" + String.valueOf(gameBoard));
    }
}
