package com.example.tictactoe;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USERNAME = "SYSTEM";
    private static final String DB_PASSWORD = "vMpeap9Rqb";
    private Connection connection;

    private static final Logger logger = Logger.getLogger(Database.class.getName());

    public Database() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            logger.info("Połączenie z bazą danych nawiązane.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd połączenia z bazą danych.", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Połączenie z bazą danych zamknięte.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd zamykania połączenia z bazą danych.", e);
        }
    }

    public boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd przy walidacji logowania.", e);
        }
        return false;
    }

    public boolean createAccount(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Konto zostało założone pomyślnie dla użytkownika: " + username);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd przy zakładaniu konta.", e);
        }
        return false;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (validateLogin(username, oldPassword)) {
            String sql = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    logger.info("Hasło zostało zmienione pomyślnie dla użytkownika: " + username);
                    return true;
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Błąd przy zmianie hasła.", e);
            }
        } else {
            logger.warning("Niepoprawne stare hasło dla użytkownika: " + username);
        }
        return false;
    }
}
