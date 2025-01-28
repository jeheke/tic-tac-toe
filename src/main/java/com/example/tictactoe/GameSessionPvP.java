package com.example.tictactoe;

import java.io.PrintWriter;

/**
 * Klasa GameSessionPvP reprezentuje sesję gry Kółko i Krzyżyk dla dwóch graczy w trybie Player vs Player (PvP).
 */
public class GameSessionPvP {

    private char[] gameBoard = new char[9];
    private boolean gameOver = false;
    private int player1;
    private int player2;
    private int currentPlayer;
    private boolean player1ReturnedToMenu = false;
    private boolean player2ReturnedToMenu = false;

    /**
     * Tworzy nową sesję gry PvP dla dwóch graczy.
     *
     * @param player1 Identyfikator Gracza 1.
     * @param player2 Identyfikator Gracza 2.
     */
    public GameSessionPvP(int player1, int player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = player1;

        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = '-';
        }

        System.out.println("Stworzono nową sesję gry dla Gracza " + player1 + " i Gracza " + player2);
        sendMessage(player1, "GAME_STARTED: 1");
        sendMessage(player2, "GAME_STARTED: 2");
        sendMessage(player1, "ASSIGN_SYMBOL:X");
        sendMessage(player2, "ASSIGN_SYMBOL:O");
    }

    /**
     * Obsługuje ruch gracza na wskazanej pozycji na planszy.
     *
     * @param clientId Identyfikator gracza wykonującego ruch.
     * @param position Indeks pozycji na planszy, gdzie wykonano ruch.
     */
    public synchronized void handlePlayerMove(int clientId, int position) {
        PrintWriter out1 = Server.getClientOutputStream(player1);
        PrintWriter out2 = Server.getClientOutputStream(player2);

        System.out.println("Ruch gracza " + clientId + ": " + position);

        if (gameOver || gameBoard[position] != '-') {
            return;
        }

        if (clientId == currentPlayer) {
            System.out.println("Wykonano ruch gracza " + clientId + " na pozycji " + position);
            gameBoard[position] = (currentPlayer == player1) ? 'X' : 'O';
            sendBoardState(out1, out2);

            if (checkWin()) {
                gameOver = true;
                sendMessage(player1, "GAME_OVER_PVP: Gracz " + clientId + " wygrał!");
                sendMessage(player2, "GAME_OVER_PVP: Gracz " + clientId + " wygrał!");
                return;
            }

            if (isBoardFull()) {
                sendMessage(player1, "GAME_OVER_PVP: Remis!");
                sendMessage(player2, "GAME_OVER_PVP: Remis!");
                gameOver = true;
                return;
            }

            currentPlayer = (currentPlayer == player1) ? player2 : player1;

            sendMessage(player1, (currentPlayer == player1) ? "YOUR_TURN" : "ENEMY_TURN");
            sendMessage(player2, (currentPlayer == player2) ? "YOUR_TURN" : "ENEMY_TURN");
        }
    }

    /**
     * Wysyła aktualny stan planszy do obu graczy.
     *
     * @param out1 Strumień wyjściowy Gracza 1.
     * @param out2 Strumień wyjściowy Gracza 2.
     */
    private void sendBoardState(PrintWriter out1, PrintWriter out2) {
        String boardState = "UPDATE_BOARD_PVP:" + String.valueOf(gameBoard);
        if (out1 != null) {
            out1.println(boardState);
            out1.flush();
        }
        if (out2 != null) {
            out2.println(boardState);
            out2.flush();
        }
    }

    /**
     * Sprawdza, czy któryś z graczy wygrał grę.
     *
     * @return {@code true}, jeśli któryś z graczy wygrał; {@code false} w przeciwnym wypadku.
     */
    private boolean checkWin() {
        int[][] winPatterns = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] pattern : winPatterns) {
            if (gameBoard[pattern[0]] != '-' &&
                    gameBoard[pattern[0]] == gameBoard[pattern[1]] &&
                    gameBoard[pattern[1]] == gameBoard[pattern[2]]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza, czy plansza jest w pełni wypełniona.
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
     * Wysyła wiadomość do wskazanego gracza.
     *
     * @param clientId Identyfikator gracza.
     * @param message  Treść wiadomości.
     */
    private void sendMessage(int clientId, String message) {
        PrintWriter out = Server.getClientOutputStream(clientId);
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    /**
     * Ustawia flagę, czy Gracz 1 powrócił do menu.
     *
     * @param returned {@code true}, jeśli Gracz 1 powrócił do menu; {@code false} w przeciwnym wypadku.
     */
    public void setPlayer1ReturnedToMenu(boolean returned) {
        this.player1ReturnedToMenu = returned;
    }

    /**
     * Ustawia flagę, czy Gracz 2 powrócił do menu.
     *
     * @param returned {@code true}, jeśli Gracz 2 powrócił do menu; {@code false} w przeciwnym wypadku.
     */
    public void setPlayer2ReturnedToMenu(boolean returned) {
        this.player2ReturnedToMenu = returned;
    }

    /**
     * Sprawdza, czy obaj gracze powrócili do menu.
     *
     * @return {@code true}, jeśli obaj gracze powrócili do menu; {@code false} w przeciwnym wypadku.
     */
    public boolean haveBothPlayersReturnedToMenu() {
        return player1ReturnedToMenu && player2ReturnedToMenu;
    }

    /**
     * Zwraca identyfikator Gracza 1.
     *
     * @return Identyfikator Gracza 1.
     */
    public int getPlayer1Id() {
        return player1;
    }

    /**
     * Zwraca identyfikator Gracza 2.
     *
     * @return Identyfikator Gracza 2.
     */
    public int getPlayer2Id() {
        return player2;
    }
}

