package com.example.tictactoe;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private static final int PORT = 8080;
    private static final int MAX_CLIENTS = 2;

    // Atomic Integer, aby zapewnić bezpieczne zwiększanie identyfikatorów klientów
    private static final AtomicInteger clientCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serwer nasłuchuje na porcie " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Połączono z klientem: " + clientSocket.getInetAddress());

                // Przypisz unikalny ID klienta
                int clientId = clientCounter.getAndIncrement();

                // Obsługuje każdego klienta w osobnym wątku
                threadPool.submit(new ClientHandler(clientSocket, clientId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Klasa obsługująca klienta
    static class ClientHandler implements Runnable {

        private Socket clientSocket;
        private int clientId;

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
                // Wysłanie powitania z identyfikatorem klienta
                out.println("Witaj na serwerze, Twój ID to: " + clientId);

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Otrzymano od klienta " + clientId + ": " + message);
                    out.println("Potwierdzenie dla klienta " + clientId + ": " + message); // Wysłanie potwierdzenia
                }

                // Jeśli klient wysłał null (połączenie zostało zamknięte), wypisz komunikat w konsoli
                System.out.println("Połączenie z klientem " + clientId + " zostało zakończone.");

            } catch (IOException e) {
                // W przypadku błędu połączenia, wypisz informację
                System.out.println("Błąd połączenia z klientem " + clientId + ": " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
