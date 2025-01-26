package com.example.tictactoe;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class AccountController {
    @FXML
    private PasswordField password;
    @FXML
    private TextField passwordVisible;
    @FXML
    private Button togglePassword;
    @FXML
    private TextField login;
    @FXML
    private Text errorText;

    private Stage stage;
    private Scene scene;

    private Parent loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        return loader.load();
    }

    @FXML
    private void initialize() {
    }

    @FXML
    public void logIn(ActionEvent event) {
        String login1 = login.getText();
        String password1 = password.getText();

        // Sprawdź, czy login i hasło nie są puste
        if (login1.isEmpty() || password1.isEmpty()) {
            errorText.setText("Pola nie mogą być puste.");
            return;
        }

        // Pobierz instancję Client
        Client client = Client.getInstance();

        try {
            // Wyślij dane logowania do serwera
            PrintWriter out = client.getOut();
            BufferedReader in = client.getInReader();

            String message = "LOG_IN:" + login1 + ":" + password1;
            out.println(message); // Wyślij wiadomość do serwera
            System.out.println("Wysłano dane logowania: " + message);

            // Odbierz odpowiedź od serwera
            String response = in.readLine();
            System.out.println("Odpowiedź serwera: " + response);

            if ("OK".equalsIgnoreCase(response)) {
                System.out.println("Logowanie powiodło się!");
                switchToMenu(event); // Przełącz scenę na główną
            } else if ("FAILED".equalsIgnoreCase(response)) {
                errorText.setText("Nieprawidłowy login lub hasło.");
            } else {
                System.out.println("Nieznana odpowiedź serwera: " + response);
            }

        } catch (IOException e) {
            System.out.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

   @FXML
    private void togglePasswordVisibility() {
        if (password.isVisible()) {
            passwordVisible.setText(password.getText()); // Przenieś tekst
            password.setVisible(false);
            passwordVisible.setVisible(true);
        } else {
            password.setText(passwordVisible.getText()); // Przenieś tekst
            password.setVisible(true);
            passwordVisible.setVisible(false);
        }
    }

    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = loadFXML("main-menu.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToPasswordChange(javafx.scene.input.MouseEvent event) throws IOException {
        Parent root = loadFXML("change-password.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToSignIn(ActionEvent event) throws IOException {
        Parent root = loadFXML("sign-in.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToLogIn(ActionEvent event) throws IOException {
        Parent root = loadFXML("log-in.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}