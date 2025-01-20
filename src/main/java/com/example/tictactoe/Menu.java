package com.example.tictactoe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Menu extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;

    private static Menu instance;  // Instancja klasy Menu
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle("Kółko i krzyżyk");
        stage.setScene(scene);

        // Rejestracja zdarzenia zamknięcia okna
        stage.setOnCloseRequest(event -> {
            // Kiedy okno jest zamykane, wykonaj disconnect
            disconnect();
            System.exit(0); // Opcjonalnie, kończy aplikację
        });

        stage.show();

        // Uruchamiamy połączenie z serwerem w osobnym wątku
        new Thread(() -> {
            try {
                connect();  // Uruchomienie połączenia
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Metoda do połączenia z serwerem
    public void connect() throws IOException {
        socket = new Socket(SERVER_ADDRESS, SERVER_PORT);  // Tworzymy połączenie z serwerem
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Strumień wejściowy
        out = new PrintWriter(socket.getOutputStream(), true);  // Strumień wyjściowy

        // Odczytujemy powitanie od serwera
        System.out.println(in.readLine());

        String message;
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            // Odczytujemy wiadomość od użytkownika
            System.out.print("Wpisz wiadomość: ");
            message = userInput.readLine();

            if ("exit".equalsIgnoreCase(message)) {
                break;  // Jeśli użytkownik wpisze "exit", przerywamy połączenie
            }

            // Wysyłamy wiadomość do serwera
            out.println(message);

            // Odbieramy odpowiedź od serwera
            System.out.println("Odpowiedź serwera: " + in.readLine());
        }

        // Po zakończeniu komunikacji rozłączamy klienta
        disconnect();
    }

    // Metoda do rozłączenia z serwerem
    public void disconnect() {
        try {
            if (out != null) out.close();  // Zamykanie strumienia wyjściowego
            if (in != null) in.close();  // Zamykanie strumienia wejściowego
            if (socket != null) socket.close();  // Zamykanie gniazda połączenia
            System.out.println("Połączenie z serwerem zostało zamknięte.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda zwracająca instancję klasy Menu
    public static Menu getInstance() {
        if (instance == null) {
            instance = new Menu();
        }
        return instance;
    }

    public static void main(String[] args) {
        launch();  // Uruchomienie aplikacji JavaFX
    }
}
