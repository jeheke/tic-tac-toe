package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 2;

    private static final AtomicInteger clientCounter = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, GameSessionBot> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, PrintWriter> clientOutputs = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + clientSocket.getInetAddress());

                int clientId = clientCounter.getAndIncrement();

                threadPool.submit(new ClientHandler(clientSocket, clientId));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
                out.println("Witaj na serwerze, Twój ID to: " + clientId);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Otrzymano od klienta " + clientId + ": " + message);

                    if (message.startsWith("SET_DIFFICULTY:")) {
                        String difficulty = message.substring("SET_DIFFICULTY:".length());
                        System.out.println("Ustawiony poziom trudności od klienta " + clientId + ": " + difficulty);

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
                        System.out.println("Klient " + clientId + " zakończył grę.");
                        sessions.remove(clientId); // Usuwanie sesji gry klienta
                        gameSession = null;
                        out.println("Gra została zakończona.");
                    } else {
                        out.println("Nieznane polecenie: " + message);
                    }
                }

                System.out.println("Połączenie z klientem " + clientId + " zostało zakończone.");
            } catch (IOException e) {
                System.out.println("Błąd połączenia z klientem " + clientId + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                    clientOutputs.remove(clientId);
                    sessions.remove(clientId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static PrintWriter getClientOutputStream(int clientId) {
        return clientOutputs.get(clientId);
    }
}
