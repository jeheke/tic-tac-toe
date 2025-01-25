package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

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

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.getAndIncrement();
                logger.info("Połączono z klientem: " + clientId);
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                clientHandlers.put(clientId, clientHandler);
                threadPool.submit(clientHandler);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Błąd główny serwera", e);
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private int clientId;
        private GameSessionBot gameSession;
        private GameSessionPvP gameSessionPvp;

        public ClientHandler(Socket clientSocket, int clientId) {
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
                out.println("Witaj na serwerze, Twoje ID to: " + clientId);

                String message;
                while ((message = in.readLine()) != null) {
                    logger.info("Otrzymano od klienta " + clientId + ": " + message);
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
            } else {
                out.println("Nieznane polecenie: " + message);
            }
        }


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

                    gameSessionPvp=null;
                } else {
                    out.println("Oczekuję, aż drugi gracz wróci do menu...");
                }
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

    public static PrintWriter getClientOutputStream(int clientId) {
        return clientOutputs.get(clientId);
    }

    public static ClientHandler getClientHandler(int clientId) {
        return clientHandlers.get(clientId); // Pobierz obiekt ClientHandler z mapy
    }

}