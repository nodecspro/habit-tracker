package org.example.habittracker.controller;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import org.example.habittracker.model.User;
import org.example.habittracker.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller("authController")
public class AuthController {

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXPasswordField passwordField;

    @FXML
    private JFXTextField regUsernameField;

    @FXML
    private JFXTextField emailField;

    @FXML
    private JFXPasswordField regPasswordField;

    @FXML
    private JFXPasswordField confirmPasswordField;

    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public AuthController() {
        System.out.println("AuthController created");
    }

    /**
     * Метод для обработки нажатия кнопки входа
     */
    @FXML
    public void handleLogin() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            // Проверка ввода
            if (username == null || username.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Login Error", "Username cannot be empty");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Login Error", "Password cannot be empty");
                return;
            }

            // Попытка входа
            User user = authService.login(username, password);

            // Если успешно, переходим на главный экран
            loadMainScreen(user);

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Login Error", e.getMessage());
        }
    }

    /**
     * Метод для обработки нажатия кнопки регистрации
     */
    @FXML
    public void handleRegister() {
        try {
            String username = regUsernameField.getText();
            String email = emailField.getText();
            String password = regPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Проверка ввода
            if (username == null || username.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Registration Error", "Username cannot be empty");
                return;
            }

            if (email == null || email.trim().isEmpty() || !email.contains("@")) {
                showAlert(AlertType.ERROR, "Registration Error", "Please enter a valid email address");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                showAlert(AlertType.ERROR, "Registration Error", "Password cannot be empty");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert(AlertType.ERROR, "Registration Error", "Passwords do not match");
                return;
            }

            // Попытка регистрации
            User user = authService.register(username, email, password);

            // Показываем сообщение об успешной регистрации
            showAlert(AlertType.INFORMATION, "Registration Successful",
                    "You have been registered successfully. Please login with your credentials.");

            // Очищаем поля
            regUsernameField.clear();
            emailField.clear();
            regPasswordField.clear();
            confirmPasswordField.clear();

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Registration Error", e.getMessage());
        }
    }

    /**
     * Метод для обработки нажатия кнопки гостевого входа
     */
    @FXML
    public void handleGuestLogin() {
        try {
            // Создаем гостевого пользователя
            User guestUser = new User();
            guestUser.setUsername("Guest");
            guestUser.setEmail("guest@example.com");

            // Переходим на главный экран
            loadMainScreen(guestUser);

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Guest Login Error", e.getMessage());
        }
    }

    /**
     * Загружает главный экран приложения
     */
    private void loadMainScreen(User user) {
        try {
            // Загружаем FXML файл
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            // Получаем контроллер и передаем ему пользователя
            MainController controller = loader.getController();
            controller.setCurrentUser(user);

            // Создаем новую сцену
            Scene scene = new Scene(root);

            // Получаем текущее окно и меняем сцену
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Habit Tracker - " + user.getUsername());
            stage.show();

        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Navigation Error",
                    "Could not load main screen: " + e.getMessage());
        }
    }

    /**
     * Показывает диалоговое окно с сообщением
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}