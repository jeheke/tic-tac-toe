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

public class Client extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private static Client instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-menu.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Kółko i krzyżyk");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            getOut().println("END_GAME");
            disconnect();
            System.exit(0);
        });

        stage.show();
        connect();

    }

    public void connect() {
            try {
                System.out.println("Próbuję połączyć z serwerem...");
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                Platform.runLater(() -> {
                    System.out.println("Połączenie z serwerem nawiązane!");
                });

            } catch (SocketException e) {
                System.out.println("Problem z socketem: " + e.getMessage());
            } catch (IOException e) {
                Platform.runLater(() -> System.out.println("Błąd połączenia z serwerem."));
            }
    }


    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("Połączenie z serwerem zostało zamknięte.");
        } catch (IOException e) {
            System.err.println("Błąd przy zamykaniu połączenia: " + e.getMessage());
        }
    }

    public static Client getInstance() {
        return instance;
    }

    public PrintWriter getOut() {
        return out;
    }

    public BufferedReader getInReader() {
        return in;
    }

    public static void main(String[] args) {
        launch();
    }
}
