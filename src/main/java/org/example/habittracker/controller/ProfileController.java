package org.example.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.habittracker.model.User;
import org.springframework.stereotype.Controller;

@Controller
public class ProfileController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private Label totalHabitsLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
        updateUI();
    }

    private void updateUI() {
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            emailLabel.setText(user.getEmail());
            createdAtLabel.setText(user.getCreatedAt().toString());
            totalHabitsLabel.setText(String.valueOf(user.getHabits().size()));
        }
    }

    @FXML
    public void handleChangePassword() {
        // Логика для изменения пароля
    }

    @FXML
    public void handleDeleteAccount() {
        // Логика для удаления аккаунта
    }
}