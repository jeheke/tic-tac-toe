package com.example.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class SceneController {
    private Stage stage;
    private Scene scene;

    private Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        return loader.load();
    }

    public void switchToMode(ActionEvent event) throws IOException {
        Parent root = loadFXML("mode-selection.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = loadFXML("main-menu.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToDifficulty(ActionEvent event) throws IOException {
        Parent root = loadFXML("difficulty.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToScoreboard(ActionEvent event) throws IOException {
        Parent root = loadFXML("scoreboard.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void switchToGamePvP(ActionEvent event) throws IOException {
        Parent root = loadFXML("game-board-pvp.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public void switchToGameBotNormal(ActionEvent event) throws IOException {
        Client client = Client.getInstance();

        String difficulty = "Normal";
        client.sendDifficultyToServer(difficulty);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }



    public void switchToGameBotHard(ActionEvent event) throws IOException {
        Client client = Client.getInstance();

        String difficulty = "Hard";
        client.sendDifficultyToServer(difficulty);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("game-board-bot.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    public void exit(ActionEvent event) {
        Client client = Client.getInstance();
        client.disconnect();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();

        System.exit(0);
    }
}

