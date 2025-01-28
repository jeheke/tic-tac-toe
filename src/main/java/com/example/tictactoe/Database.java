package com.example.tictactoe;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa odpowiedzialna za komunikację z bazą danych, w tym logowanie, zakładanie kont i zmianę hasła.
 * Używa JDBC do łączenia się z bazą danych Oracle.
 */
public class Database {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE"; // URL do bazy danych.
    private static final String DB_USERNAME = "system"; // Nazwa użytkownika bazy danych.
    private static final String DB_PASSWORD = "123456"; // Hasło do bazy danych.
    private Connection connection; // Połączenie z bazą danych.

    private static final Logger logger = Logger.getLogger(Database.class.getName()); // Logger do logowania informacji.

    /**
     * Konstruktor klasy, który nawiązuje połączenie z bazą danych.
     */
    public Database() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            logger.info("Połączenie z bazą danych nawiązane.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd połączenia z bazą danych.", e);
        }
    }

    /**
     * Zamyka połączenie z bazą danych, jeżeli jest otwarte.
     */
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

    /**
     * Sprawdza, czy dane logowania (nazwa użytkownika i hasło) są poprawne.
     * @param username Nazwa użytkownika.
     * @param password Hasło użytkownika.
     * @return Zwraca true, jeśli logowanie jest poprawne, w przeciwnym razie false.
     */
    public boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true; // Jeśli znaleziono użytkownika, logowanie jest poprawne.
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd przy walidacji logowania.", e);
        }
        return false; // Zwrócenie false, jeśli logowanie nie jest poprawne.
    }

    /**
     * Tworzy nowe konto użytkownika w bazie danych.
     * @param username Nazwa użytkownika.
     * @param password Hasło użytkownika.
     * @return Zwraca true, jeśli konto zostało założone pomyślnie, w przeciwnym razie false.
     */
    public boolean createAccount(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Konto zostało założone pomyślnie dla użytkownika: " + username);
                return true; // Konto zostało założone.
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Błąd przy zakładaniu konta.", e);
        }
        return false; // Konto nie zostało założone.
    }

    /**
     * Zmienia hasło użytkownika, o ile stare hasło jest poprawne.
     * @param username Nazwa użytkownika.
     * @param oldPassword Stare hasło użytkownika.
     * @param newPassword Nowe hasło użytkownika.
     * @return Zwraca true, jeśli hasło zostało pomyślnie zmienione, w przeciwnym razie false.
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        // Sprawdzenie, czy stare hasło jest poprawne.
        if (validateLogin(username, oldPassword)) {
            String sql = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    logger.info("Hasło zostało zmienione pomyślnie dla użytkownika: " + username);
                    return true; // Hasło zostało zmienione.
                }
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Błąd przy zmianie hasła.", e);
            }
        } else {
            logger.warning("Niepoprawne stare hasło dla użytkownika: " + username);
        }
        return false; // Hasło nie zostało zmienione.
    }
}
