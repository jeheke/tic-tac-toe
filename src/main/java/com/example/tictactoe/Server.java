package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

/**
 * Klasa Server obsługuje połączenia sieciowe i zarządza grą wieloosobową.
 * Obsługuje komunikację klientów, tworzenie sesji gry oraz rejestrację/logowanie użytkowników.
 */
public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 50;

    private static final AtomicInteger clientCounter = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, GameSessionBot> botSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, PrintWriter> clientOutputs = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, GameSessionPvP> pvpSessions = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Integer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static final ConcurrentHashMap<Integer, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

    static {
        try {
            FileHandler fileHandler = new FileHandler("server_logs.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Nie udało się skonfigurować logowania do pliku: " + e.getMessage());
        }
    }

    /**
     * Główna metoda serwera. Rozpoczyna nasłuchiwanie na porcie i obsługuje przychodzące połączenia.
     *
     * @param args argumenty wiersza poleceń (nieużywane)
     */
    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Serwer nasłuchuje na porcie " + PORT);
            Database database = new Database();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.getAndIncrement();
                logger.info("Połączono z klientem: " + clientId);
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId, database);
                clientHandlers.put(clientId, clientHandler);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Błąd główny serwera", e);
        }
    }

    /**
     * Klasa wewnętrzna ClientHandler obsługująca pojedynczego klienta.
     */
    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int clientId;
        private GameSessionBot gameSession;
        private GameSessionPvP gameSessionPvp;

        /**
         * Konstruktor klasy ClientHandler.
         *
         * @param clientSocket gniazdo klienta
         * @param clientId     identyfikator klienta
         * @param database     instancja bazy danych
         */
        public ClientHandler(Socket clientSocket, int clientId, Database database) {
            this.clientSocket = clientSocket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                clientOutputs.put(clientId, out);
                logger.info("Klient " + clientId + " połączony.");

                String message;
                while ((message = in.readLine()) != null) {
                    logger.info("Otrzymano od klienta " + clientId + ": " + message);// Obsługa logowania
                    handleLogin(message, out);
                    handleGamePvp(message, out);
                    handleGameBot(message, out);
                }
                logger.info("Połączenie z klientem " + clientId + " zostało zakończone.");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Błąd połączenia z klientem " + clientId, e);
            } finally {
                cleanup();
            }
        }

        /**
         * Obsługuje działania klienta związane z trybem gry z botem.
         *
         * @param message wiadomość od klienta
         * @param out     strumień wyjściowy do klienta
         */
        private void handleGameBot(String message, PrintWriter out) {
            if (message.startsWith("SET_DIFFICULTY:")) {
                String difficulty = message.substring("SET_DIFFICULTY:".length());
                logger.info("Ustawiono " + difficulty + " poziom trudności dla Gracza " + clientId);
                gameSession = new GameSessionBot(difficulty, clientId);
                botSessions.put(clientId, gameSession);
            } else if (message.startsWith("PLAYER_MOVE:")) {
                if (gameSession != null) {
                    int position = Integer.parseInt(message.substring("PLAYER_MOVE:".length()));
                    gameSession.handlePlayerMove(position);
                }
            } else if (message.equals("RESTART_GAME")) {
                if (gameSession != null) {
                    gameSession.restart();
                }
            } else if (message.equals("END_GAME")) {
                endGame(out);
            }
        }

        /**
         * Obsługuje działania klienta związane z trybem gry PvP.
         *
         * @param message wiadomość od klienta
         * @param out     strumień wyjściowy do klienta
         */
        private void handleGamePvp(String message, PrintWriter out) {
            if (message.startsWith("START_PVP")) {
                synchronized (waitingPlayers) {
                    Integer opponentId = waitingPlayers.poll();
                    if (opponentId == null) {
                        waitingPlayers.add(clientId);
                        logger.info("Gracz " + clientId + " dodany do kolejki oczekujących.");
                        out.println("Czekam na drugiego gracza...");
                    } else {
                        logger.info("Gracz " + clientId + " połączony z graczem " + opponentId + ".");
                        GameSessionPvP session = new GameSessionPvP(clientId, opponentId);
                        pvpSessions.put(clientId, session);
                        pvpSessions.put(opponentId, session);
                        ClientHandler opponentHandler = Server.getClientHandler(opponentId);
                        if (opponentHandler != null) {
                            opponentHandler.setGameSessionPvp(session);
                        }
                        this.gameSessionPvp = session;
                        sendMessageToPlayer(clientId, "Połączono z przeciwnikiem! Gra PvP rozpoczęta.");
                        sendMessageToPlayer(opponentId, "Połączono z przeciwnikiem! Gra PvP rozpoczęta.");

                        sendMessageToPlayer(clientId, "YOUR_TURN");
                        sendMessageToPlayer(opponentId, "ENEMY_TURN");
                    }
                }
            } else if (message.startsWith("PLAYER_MOVE_PVP:")) {
                if (gameSessionPvp == null) {
                    logger.warning("Brak sesji PvP dla klienta " + clientId);
                    out.println("Nie jesteś w żadnej grze PvP!");
                } else {
                    int position = Integer.parseInt(message.substring("PLAYER_MOVE_PVP:".length()));
                    logger.info("Gracz " + clientId + " wykonuje ruch na pozycji " + position);
                    gameSessionPvp.handlePlayerMove(clientId, position);
                }
            } else if (message.equals("END_GAME_PVP")) {
                if (gameSessionPvp == null) {
                    out.println("Nie znajdujesz się w żadnej grze PvP!");
                    return;
                }
                if (gameSessionPvp.getPlayer1Id() == clientId) {
                    gameSessionPvp.setPlayer1ReturnedToMenu(true);
                    logger.info("Gracz " + clientId + " wrócił do menu.");
                } else if (gameSessionPvp.getPlayer2Id() == clientId) {
                    gameSessionPvp.setPlayer2ReturnedToMenu(true);
                    logger.info("Gracz " + clientId + " wrócił do menu.");
                }
                if (gameSessionPvp.haveBothPlayersReturnedToMenu()) {
                    logger.info("Obaj gracze wrócili do menu. Usuwanie sesji PvP.");

                    int player1Id = gameSessionPvp.getPlayer1Id();
                    int player2Id = gameSessionPvp.getPlayer2Id();
                    pvpSessions.remove(player1Id);
                    pvpSessions.remove(player2Id);

                    sendMessageToPlayer(player1Id, "Obaj gracze wrócili do menu. Sesja została zakończona.");
                    sendMessageToPlayer(player2Id, "Obaj gracze wrócili do menu. Sesja została zakończona.");

                    gameSessionPvp = null;
                } else {
                    out.println("Oczekuję, aż drugi gracz wróci do menu...");
                }
            }
        }


        /**
         * Obsługuje logowanie, rejestrację i zmianę hasła użytkownika.
         *
         * @param message wiadomość od klienta
         * @param out     strumień wyjściowy do klienta
         */
        private void handleLogin(String message, PrintWriter out) {
            if (message.startsWith("LOG_IN:")) {
                String[] parts = message.split(":");
                if (parts.length != 3) {
                    out.println("Błąd: Niepoprawny format logowania. Oczekiwany: LOG_IN:username:password");
                    return;
                }

                String username = parts[1];
                String password = parts[2];

                Database database = new Database();

                if (database.validateLogin(username, password)) {
                    out.println("OK");
                } else {
                    out.println("FAILED");
                }

                database.closeConnection();
            }

            if (message.startsWith("SIGN_IN:")) {
                String[] parts = message.split(":");
                if (parts.length != 3) {
                    out.println("Błąd: Niepoprawny format rejestracji. Oczekiwany: SIGN_IN:username:password");
                    return;
                }

                String username = parts[1];
                String password = parts[2];

                Database database = new Database();

                if (database.createAccount(username, password)) {
                    out.println("OK");
                } else {
                    out.println("FAILED");
                }

                database.closeConnection();
            }

            if (message.startsWith("CHANGE_PASSWORD:")) {
                String[] parts = message.split(":");
                if (parts.length != 4) {
                    out.println("Błąd: Niepoprawny format zmiany hasła. Oczekiwany: CHANGE_PASSWORD:username:old_password:new_password");
                    return;
                }

                String username = parts[1];
                String oldPassword = parts[2];
                String newPassword = parts[3];

                Database database = new Database();

                if (database.changePassword(username, oldPassword, newPassword)) {
                    out.println("OK");
                } else {
                    out.println("FAILED");
                }

                database.closeConnection();
            }
        }


        private void sendMessageToPlayer(int clientId, String message) {
            PrintWriter out = clientOutputs.get(clientId);
            if (out != null) {
                out.println(message);
            }
        }


        private void endGame(PrintWriter out) {
            logger.info("Klient " + clientId + " zakończył grę.");
            botSessions.remove(clientId);
            gameSession = null;
            gameSessionPvp = null;
        }

        /**
         * Czyści zasoby po zakończeniu pracy z klientem.
         */
        private void cleanup() {
            try {
                clientSocket.close();
                clientOutputs.remove(clientId);
                botSessions.remove(clientId);
                waitingPlayers.remove(clientId);
                clientHandlers.remove(clientId); // Usunięcie z mapy
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    public void setGameSessionPvp(GameSessionPvP session) {
        this.gameSessionPvp = session;
    }
}

    /**
     * Pobiera strumień wyjściowy do klienta o podanym identyfikatorze.
     *
     * @param clientId identyfikator klienta
     * @return strumień wyjściowy {@link PrintWriter} lub null, jeśli klient nie istnieje
     */
    public static PrintWriter getClientOutputStream(int clientId) {
        return clientOutputs.get(clientId);
    }

    /**
     * Pobiera obiekt ClientHandler obsługujący klienta o podanym identyfikatorze.
     *
     * @param clientId identyfikator klienta
     * @return obiekt {@link ClientHandler} lub null, jeśli klient nie istnieje
     */
    public static ClientHandler getClientHandler(int clientId) {
        return clientHandlers.get(clientId);
    }
}
