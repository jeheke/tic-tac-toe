package com.example.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Klasa SceneController odpowiada za zarządzanie przełączaniem scen w aplikacji.
 * Obsługuje różne widoki i umożliwia nawigację pomiędzy nimi.
 */
public class SceneController {
    private Stage stage;
    private Scene scene;

    /**
     * Wczytuje plik FXML.
     *
     * @param fxmlFile nazwa pliku FXML do wczytania
     * @return obiekt {@link Parent} reprezentujący główny kontener sceny
     * @throws IOException jeśli wystąpi błąd podczas wczytywania pliku
     */
    private Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        return loader.load();
    }

    /**
     * Przełącza widok na ekran wyboru trybu gry.
     *
     * @param event zdarzenie wywołane przez użytkownika
     * @throws IOException jeśli wystąpi błąd podczas ładowania sceny
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
     *
     * @param event zdarzenie wywołane przez użytkownika
     * @throws IOException jeśli wystąpi błąd podczas ładowania sceny
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
     *
     * @param event zdarzenie wywołane przez użytkownika
     * @throws IOException jeśli wystąpi błąd podczas ładowania sceny
     */
    public void switchToDifficulty(ActionEvent event) throws IOException {
        Parent root = loadFXML("difficulty.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran gry Player vs Player (PvP).
     *
     * @param event zdarzenie wywołane przez użytkownika
     * @throws IOException jeśli wystąpi błąd podczas ładowania sceny
     */
    public void switchToGamePvP(ActionEvent event) throws IOException {
        Client client = Client.getInstance();
        client.getOut().println("START_PVP");

        Parent root = loadFXML("game-board-pvp.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran gry z botem na poziomie normalnym.
     *
     * @param event zdarzenie wywołane przez użytkownika
     * @throws IOException jeśli wystąpi błąd podczas ładowania sceny
     */
    public void switchToGameBotNormal(ActionEvent event) throws IOException {
        Client client = Client.getInstance();

        String difficulty = "Normalny";
        client.getOut().println("SET_DIFFICULTY:" + difficulty);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Przełącza widok na ekran gry z botem na poziomie trudnym.
     *
     * @param event zdarzenie wywołane przez użytkownika
     * @throws IOException jeśli wystąpi błąd podczas ładowania sceny
     */
    public void switchToGameBotHard(ActionEvent event) throws IOException {
        Client client = Client.getInstance();

        String difficulty = "Trudny";
        client.getOut().println("SET_DIFFICULTY:" + difficulty);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Kończy działanie aplikacji i zamyka połączenie klienta.
     *
     * @param event zdarzenie wywołane przez użytkownika
     */
    public void exit(ActionEvent event) {
        Client client = Client.getInstance();
        client.disconnect();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

        System.exit(0);
    }
}
