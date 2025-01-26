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
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class AccountController {
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private PasswordField newPassword;
    @FXML
    private PasswordField oldPassword;
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

        if (login1.isEmpty() || password1.isEmpty()) {
            errorText.setText("Pola nie mogą być puste.");
            return;
        }

        Client client = Client.getInstance();

        try {
            PrintWriter out = client.getOut();
            BufferedReader in = client.getInReader();

            String message = "LOG_IN:" + login1 + ":" + password1;
            out.println(message);
            System.out.println("Wysłano dane logowania: " + message);

            String response = in.readLine();
            System.out.println("Odpowiedź serwera: " + response);

            if ("OK".equalsIgnoreCase(response)) {
                System.out.println("Logowanie powiodło się!");
                switchToMenu(event);
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
    public void signUp(ActionEvent event) {
        String login1 = login.getText();
        String password1 = password.getText();
        String confirmPassword1 = confirmPassword.getText();

        if (login1.isEmpty() || password1.isEmpty() || confirmPassword1.isEmpty()) {
            errorText.setText("Pola nie mogą być puste.");
            return;
        }

        if (!password1.equals(confirmPassword1)) {
            errorText.setText("Hasła nie są takie same.");
            return;
        }

        Client client = Client.getInstance();

        try {
            PrintWriter out = client.getOut();
            BufferedReader in = client.getInReader();

            String message = "SIGN_IN:" + login1 + ":" + password1;
            out.println(message);
            System.out.println("Wysłano dane rejestracji: " + message);

            String response = in.readLine();
            System.out.println("Odpowiedź serwera: " + response);

            if ("OK".equalsIgnoreCase(response)) {
                System.out.println("Rejestracja powiodła się!");
                switchToLogIn(event);
            } else if ("FAILED".equalsIgnoreCase(response)) {
                errorText.setText("Rejestracja nie powiodła się.");
            } else {
                System.out.println("Nieznana odpowiedź serwera: " + response);
            }

        } catch (IOException e) {
            System.out.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    @FXML
    public void changePassword(ActionEvent event) {
        String login1 = login.getText();
        String oldPassword1 = oldPassword.getText();
        String newPassword1 = newPassword.getText();

        if (login1.isEmpty() || oldPassword1.isEmpty() || newPassword1.isEmpty()) {
            errorText.setText("Pola nie mogą być puste.");
            return;
        }

        Client client = Client.getInstance();

        try {
            PrintWriter out = client.getOut();
            BufferedReader in = client.getInReader();

            String message = "CHANGE_PASSWORD:" + login1 + ":" + oldPassword1 + ":" + newPassword1;
            out.println(message);
            System.out.println("Wysłano dane do zmiany hasła: " + message);

            String response = in.readLine();
            System.out.println("Odpowiedź serwera: " + response);

            if ("OK".equalsIgnoreCase(response)) {
                System.out.println("Zmiana hasła powiodła się!");
                switchToLogIn(event);
            } else if ("FAILED".equalsIgnoreCase(response)) {
                errorText.setText("Zmiana hasła nie powiodła się.");
            } else {
                System.out.println("Nieznana odpowiedź serwera: " + response);
            }

        } catch (IOException e) {
            System.out.println("Błąd komunikacji z serwerem: " + e.getMessage());
        }
    }

    // Zmiana widoczności hasła
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

    // Przełączenie na menu główne
    public void switchToMenu(ActionEvent event) throws IOException {
        Parent root = loadFXML("main-menu.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Przełączenie na ekran zmiany hasła
    @FXML
    public void switchToPasswordChange(MouseEvent event) throws IOException {
        Parent root = loadFXML("change-password.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Przełączenie na ekran rejestracji
    @FXML
    public void switchToSignIn(ActionEvent event) throws IOException {
        Parent root = loadFXML("sign-in.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // Przełączenie na ekran logowania
    @FXML
    public void switchToLogIn(ActionEvent event) throws IOException {
        Parent root = loadFXML("log-in.fxml");
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
