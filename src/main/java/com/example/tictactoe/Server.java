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
         * @param clientId identyfikator klienta
         * @param database instancja bazy danych
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
                    logger.info("Otrzymano od klienta " + clientId + ": " + message);
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
         * Obsługuje działania klienta związane z trybem gry PvP.
         *
         * @param message wiadomość od klienta
         * @param out strumień wyjściowy do klienta
         */
        private void handleGamePvp(String message, PrintWriter out) {
            // Implementacja logiki PvP...
        }

        /**
         * Obsługuje działania klienta związane z trybem gry z botem.
         *
         * @param message wiadomość od klienta
         * @param out strumień wyjściowy do klienta
         */
        private void handleGameBot(String message, PrintWriter out) {
            // Implementacja logiki z botem...
        }

        /**
         * Obsługuje logowanie, rejestrację i zmianę hasła użytkownika.
         *
         * @param message wiadomość od klienta
         * @param out strumień wyjściowy do klienta
         */
        private void handleLogin(String message, PrintWriter out) {
            // Implementacja logowania i rejestracji...
        }

        /**
         * Czyści zasoby po zakończeniu pracy z klientem.
         */
        private void cleanup() {
            // Implementacja czyszczenia zasobów...
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
