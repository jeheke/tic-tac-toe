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

public class Menu extends Application {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private static Menu instance;  // Instancja klasy Menu
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

        stage.setOnCloseRequest(event -> {
            disconnect();
            System.exit(0);
        });

        stage.show();
        connect();

    }

    public void connect(){
            try {
                System.out.println("Próbuję połączyć z serwerem...");
                socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                setConnected(true);

                // Notify the GUI thread
                Platform.runLater(() -> {
                    System.out.println("Połączenie z serwerem nawiązane!");
                    System.out.println("Aktualny stan połączenia (w GUI): " + isConnected());
                });

                System.out.println(in.readLine());

            }catch(SocketException e) {
                System.out.println("Coś poszło nie tak z socketem: " + e.getMessage());
            }
            catch (IOException e) {
                System.out.println("Błąd połączenia z serwerem: " + e.getMessage());
                setConnected(false);

                Platform.runLater(() -> {
                    System.out.println("Błąd połączenia z serwerem.");
                });
            }
    }


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

    public static Menu getInstance() {
        return instance;
    }

    public PrintWriter getOut() {
        return out;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

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

    public BufferedReader getInReader() {
        return in;
    }

    public static void main(String[] args) {
        launch();
    }
}
