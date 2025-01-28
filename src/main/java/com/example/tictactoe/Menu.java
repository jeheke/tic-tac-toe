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
 * Klasa Menu obsługuje główne menu aplikacji oraz połączenie z serwerem.
 */
public class Menu extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private static Menu instance;  // Instancja singletona klasy Menu
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected;

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Kółko i krzyżyk");
        stage.setScene(scene);

        // Obsługa zamknięcia okna aplikacji
        stage.setOnCloseRequest(event -> {
            disconnect();
            System.exit(0);
        });

        stage.show();
        connect(); // Nawiązanie połączenia z serwerem
    }

    /**
     * Metoda nawiązuje połączenie z serwerem.
     */
    public void connect() {
        try {
            System.out.println("Próbuję połączyć z serwerem...");
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            setConnected(true);

            // Aktualizacja GUI w głównym wątku
            Platform.runLater(() -> {
                System.out.println("Połączenie z serwerem nawiązane!");
                System.out.println("Aktualny stan połączenia (w GUI): " + isConnected());
            });

            System.out.println(in.readLine()); // Odczyt komunikatu od serwera

        } catch (SocketException e) {
            System.out.println("Coś poszło nie tak z socketem: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Błąd połączenia z serwerem: " + e.getMessage());
            setConnected(false);

            Platform.runLater(() -> {
                System.out.println("Błąd połączenia z serwerem.");
            });
        }
    }

    /**
     * Zamyka połączenie z serwerem.
     */
    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Połączenie z serwerem zostało zamknięte.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Zwraca instancję singletona klasy Menu.
     *
     * @return instancja Menu
     */
    public static Menu getInstance() {
        return instance;
    }

    /**
     * Zwraca strumień wyjściowy do wysyłania danych do serwera.
     *
     * @return strumień wyjściowy
     */
    public PrintWriter getOut() {
        return out;
    }

    /**
     * Sprawdza, czy połączenie z serwerem jest aktywne.
     *
     * @return true, jeśli połączenie jest aktywne; false w przeciwnym razie
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Ustawia stan połączenia.
     *
     * @param connected stan połączenia
     */
    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * Wysyła poziom trudności do serwera.
     *
     * @param difficulty poziom trudności ("easy", "normal", "hard")
     */
    public void sendDifficultyToServer(String difficulty) {
        if (isConnected()) {
            if (out != null) {
                out.println("SET_DIFFICULTY:" + difficulty);
                System.out.println("Wysłano poziom trudności: " + difficulty);
            } else {
                System.out.println("Strumień wyjściowy (out) nie został zainicjowany.");
            }
        } else {
            System.out.println("Połączenie z serwerem nie zostało jeszcze nawiązane.");
        }
    }

    /**
     * Zwraca strumień wejściowy do odbierania danych z serwera.
     *
     * @return strumień wejściowy
     */
    public BufferedReader getInReader() {
        return in;
    }

    public static void main(String[] args) {
        launch();
    }
}
