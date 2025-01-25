package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 3;

    private static final AtomicInteger clientCounter = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, GameSessionBot> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, PrintWriter> clientOutputs = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, GameSessionPvP> pvpSessions = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Integer> waitingPlayers = new ConcurrentLinkedQueue<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());

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
                logger.info("Połączono z klientem: " + clientSocket.getInetAddress());

                int clientId = clientCounter.getAndIncrement();
                threadPool.submit(new ClientHandler(clientSocket, clientId));
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
                out.println("Witaj na serwerze, Twój ID to: " + clientId);

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
                startGameWithBot(message, out);
            } else if (message.startsWith("PLAYER_MOVE:")) {
                if (gameSession != null) {
                    int position = Integer.parseInt(message.substring("PLAYER_MOVE:".length()));
                    gameSession.handlePlayerMove(position);
                } else {
                    out.println("Rozgrywka nie została jeszcze rozpoczęta. Wybierz poziom trudności.");
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

        private void startGameWithBot(String message, PrintWriter out) {
            String difficulty = message.substring("SET_DIFFICULTY:".length());
            logger.info("Ustawiony poziom trudności od klienta " + clientId + ": " + difficulty);
            gameSession = new GameSessionBot(difficulty, clientId);
            sessions.put(clientId, gameSession);
            out.println("Gra z botem została rozpoczęta na poziomie trudności: " + difficulty);
        }

        private void handleGamePvp(String message, PrintWriter out) {
            if (message.startsWith("START_PVP")) {
                synchronized (waitingPlayers) {
                    System.out.println("Gracz " + clientId + " wysłał START_PVP.");

                    Integer opponentId = waitingPlayers.poll();
                    if (opponentId == null) {
                        waitingPlayers.add(clientId);
                        out.println("Czekam na drugiego gracza...");
                        System.out.println("Gracz " + clientId + " dodany do kolejki oczekujących.");
                    } else {
                        // Match with opponent
                        System.out.println("Gracz " + clientId + " połączony z graczem " + opponentId);

                        gameSessionPvp = new GameSessionPvP(clientId, opponentId);
                        pvpSessions.put(clientId, gameSessionPvp);
                        pvpSessions.put(opponentId, gameSessionPvp);

                        sendMessageToPlayer(clientId, "Połączono z przeciwnikiem! Gra PvP rozpoczęta.");
                        sendMessageToPlayer(opponentId, "Połączono z przeciwnikiem! Gra PvP rozpoczęta.");

                        sendMessageToPlayer(clientId, "YOUR_TURN");
                        sendMessageToPlayer(opponentId, "ENEMY_TURN");
                    }
                }
            } else if (message.startsWith("PLAYER_MOVE_PVP:")) {
                if (gameSessionPvp != null) {
                    int position = Integer.parseInt(message.substring("PLAYER_MOVE_PVP:".length()));
                    gameSessionPvp.handlePlayerMove(clientId, position);
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
            sessions.remove(clientId);
            gameSession = null;
            gameSessionPvp = null;
        }

        private void cleanup() {
            try {
                clientSocket.close();
                clientOutputs.remove(clientId);
                sessions.remove(clientId);
                waitingPlayers.remove(clientId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static PrintWriter getClientOutputStream(int clientId) {
        return clientOutputs.get(clientId);
    }
}
