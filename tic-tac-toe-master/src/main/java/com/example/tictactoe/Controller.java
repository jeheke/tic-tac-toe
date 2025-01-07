package com.example.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    private Stage stage;
    private Scene scene;

    private double windowWidth;
    private double windowHeight;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        return loader.load();
    }

    public void saveWindowSize() {
        if (stage != null) {
            windowWidth = stage.getWidth();
            windowHeight = stage.getHeight();
        }
    }

    public void restoreWindowSize() {
        if (stage != null) {
            stage.setWidth(windowWidth);
            stage.setHeight(windowHeight);
        }
    }

    public void switchToMode(ActionEvent event) throws IOException {
        saveWindowSize();
        Parent root = loadFXML("mode-selection.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void switchToMenu(ActionEvent event) throws IOException {
        saveWindowSize();
        Parent root = loadFXML("main-menu.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void switchToDifficulty(ActionEvent event) throws IOException {
        saveWindowSize();
        Parent root = loadFXML("difficulty.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void switchToScoreboard(ActionEvent event) throws IOException {
        saveWindowSize();
        Parent root = loadFXML("scoreboard.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void switchToGamePvP(ActionEvent event) throws IOException {
        saveWindowSize();
        Parent root = loadFXML("game-board-pvp.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void switchToGameBotNormal(ActionEvent event) throws IOException {
        saveWindowSize();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        botLogic botLogicController = loader.getController();
        String difficulty = "Normal";
        botLogicController.setDifficulty(difficulty);

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void switchToGameBotHard(ActionEvent event) throws IOException {
        saveWindowSize();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        botLogic botLogicController = loader.getController();
        String difficulty = "Hard";
        botLogicController.setDifficulty(difficulty);

        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        restoreWindowSize();
    }

    public void exit(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
