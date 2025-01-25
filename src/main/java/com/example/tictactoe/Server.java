package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 2;

    private static final AtomicInteger clientCounter = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, GameSessionBot> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, PrintWriter> clientOutputs = new ConcurrentHashMap<>();
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

                    if (message.startsWith("SET_DIFFICULTY:")) {
                        String difficulty = message.substring("SET_DIFFICULTY:".length());
                        logger.info("Ustawiony poziom trudności od klienta " + clientId + ": " + difficulty);

                        // Tworzenie nowej gry z botem
                        gameSession = new GameSessionBot(difficulty, clientId);
                        sessions.put(clientId, gameSession);
                        out.println("Gra z botem została rozpoczęta na poziomie trudności: " + difficulty);
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
                        logger.info("Klient " + clientId + " zakończył grę.");

                        // Usuwanie sesji gry klienta i resetowanie stanu
                        if (sessions.containsKey(clientId)) {
                            sessions.remove(clientId); // Usuwanie istniejącej sesji gry
                        }

                        gameSession = null; // Resetowanie instancji gry
                        out.println("Gra została zakończona. Powrót do menu.");
                    } else {
                        out.println("Nieznane polecenie: " + message);
                    }
                }

                logger.info("Połączenie z klientem " + clientId + " zostało zakończone.");
            } catch (IOException e) {
                logger.log(Level.WARNING, "Błąd połączenia z klientem " + clientId, e);
            } finally {
                try {
                    clientSocket.close();
                    clientOutputs.remove(clientId);
                    sessions.remove(clientId);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Błąd zamykania połączenia z klientem " + clientId, e);
                }
            }
        }
    }

    public static PrintWriter getClientOutputStream(int clientId) {
        return clientOutputs.get(clientId);
    }
}
