package com.example.tictactoe;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Klasa reprezentująca klienta gry w "Kółko i Krzyżyk" w aplikacji sieciowej.
 * Używa połączenia z serwerem, aby komunikować się i przesyłać dane gry.
 */
public class Client extends Application {
    private static final String SERVER_ADDRESS = "localhost"; // Adres serwera.
    private static final int SERVER_PORT = 12345; // Port serwera.

    private static Client instance; // Instancja klienta.
    private Socket socket; // Połączenie z serwerem.
    private PrintWriter out; // Strumień do wysyłania danych do serwera.
    private BufferedReader in; // Strumień do odbierania danych od serwera.

    /**
     * Główna metoda startowa aplikacji.
     * Inicjalizuje połączenie z serwerem oraz wyświetla interfejs logowania.
     *
     * @param stage Główne okno aplikacji.
     * @throws IOException Jeżeli wystąpi błąd przy ładowaniu pliku FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        instance = this;

        // Ładowanie pliku FXML i ustawienie sceny.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("log-in.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Kółko i krzyżyk");
        stage.setScene(scene);

        // Obsługa zamknięcia aplikacji.
        stage.setOnCloseRequest(event -> {
            getOut().println("END_GAME"); // Wysyła sygnał zakończenia gry do serwera.
            disconnect(); // Rozłącza klienta.
            System.exit(0); // Zamyka aplikację.
        });

        stage.show();
        connect(); // Próba połączenia z serwerem.
    }

    /**
     * Nawiązuje połączenie z serwerem gry.
     * W przypadku błędu wyświetla odpowiedni komunikat.
     */
    public void connect() {
        try {
            System.out.println("Próbuję połączyć z serwerem...");
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // Łączy z serwerem.
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Strumień do odbierania danych.
            out = new PrintWriter(socket.getOutputStream(), true); // Strumień do wysyłania danych.

            // Informuje o pomyślnym połączeniu na wątku JavaFX.
            Platform.runLater(() -> {
                System.out.println("Połączenie z serwerem nawiązane!");
            });

        } catch (SocketException e) {
            System.out.println("Problem z socketem: " + e.getMessage());
        } catch (IOException e) {
            Platform.runLater(() -> System.out.println("Błąd połączenia z serwerem."));
        }
    }

    /**
     * Zamyka połączenie z serwerem oraz strumienie.
     */
    public void disconnect() {
        try {
            if (out != null) out.close(); // Zamyka strumień wyjściowy.
            if (in != null) in.close(); // Zamyka strumień wejściowy.
            if (socket != null) socket.close(); // Zamyka połączenie z serwerem.
            System.out.println("Połączenie z serwerem zostało zamknięte.");
        } catch (IOException e) {
            System.err.println("Błąd przy zamykaniu połączenia: " + e.getMessage());
        }
    }

    /**
     * Zwraca instancję klienta.
     * @return Instancja klasy Client.
     */
    public static Client getInstance() {
        return instance;
    }

    /**
     * Zwraca strumień do wysyłania danych do serwera.
     * @return Strumień wyjściowy.
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * Zwraca strumień do odbierania danych od serwera.
     * @return Strumień wejściowy.
     */
    public BufferedReader getInReader() {
        return in;
    }

    /**
     * Uruchamia aplikację JavaFX.
     * @param args Argumenty wywołania aplikacji.
     */
    public static void main(String[] args) {
        launch(); // Uruchamia aplikację.
    }
}
