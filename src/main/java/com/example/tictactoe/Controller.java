package com.example.tictactoe;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
/**
 * Odpowiada za zarządzanie przejściami między ekranami (widokami) aplikacji,
 * obsługując zdarzenia związane z kliknięciem przycisków oraz ładowaniem odpowiednich widoków FXML.
 */

public class Controller {
    private Stage stage;
    private Scene scene;

    /**
     * Ładuje plik FXML i zwraca korzeń widoku.
     *
     * @param fxmlFile ścieżka do pliku FXML
     * @return załadowany widok
     * @throws IOException jeśli nie uda się załadować pliku FXML
     */
    private Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        return loader.load();
    }

    /**
     * Przełącza widok na ekran wyboru trybu gry.
     */
    public void switchToMode(ActionEvent event) throws IOException {
        Parent root = loadFXML("mode-selection.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na menu główne.
     */
    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = loadFXML("main-menu.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran wyboru poziomu trudności.
     */
    public void switchToDifficulty(ActionEvent event) throws IOException {
        Parent root = loadFXML("difficulty.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran gry PvP (gracz przeciwko graczowi).
     */
    public void switchToGamePvP(ActionEvent event) throws IOException {
        Parent root = loadFXML("game-board-pvp.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran gry z botem na poziomie normalnym.
     */
    public void switchToGameBotNormal(ActionEvent event) throws IOException {
        Menu client = Menu.getInstance(); // Uzyskanie instancji klasy Menu

        String difficulty = "Normal";
        client.sendDifficultyToServer(difficulty); // Wysłanie poziomu trudności na serwer

        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran gry z botem na poziomie trudnym.
     */
    public void switchToGameBotHard(ActionEvent event) throws IOException {
        Menu client = Menu.getInstance();

        String difficulty = "Hard";
        client.sendDifficultyToServer(difficulty); // Wysłanie poziomu trudności na serwer

        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Zamyka aplikację.
     */
    public void exit(ActionEvent event) {
        Menu client = Menu.getInstance();
        client.disconnect(); // Rozłączenie z serwerem

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

        System.exit(0); // Zakończenie programu
    }
}
