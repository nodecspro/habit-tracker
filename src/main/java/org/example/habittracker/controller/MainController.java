package org.example.habittracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.example.habittracker.model.User;
import org.example.habittracker.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class MainController {

    @FXML
    private StackPane contentArea;

    private User currentUser;

    private final HabitService habitService;

    @Autowired
    public MainController(HabitService habitService) {
        this.habitService = habitService;
    }

    public void initialize() {
        // Инициализация контроллера
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Инициализация интерфейса с данными пользователя
        updateUI();
    }

    private void updateUI() {
        // Обновление интерфейса в зависимости от данных пользователя
        try {
            showDashboard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent profileView = loader.load();

            ProfileController controller = loader.getController();
            controller.setUser(currentUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(profileView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        try {
            // Логика для выхода из аккаунта
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();

            // Заменяем всю сцену на экран входа
            contentArea.getScene().setRoot(loginView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent dashboardView = loader.load();

            DashboardController controller = loader.getController();
            controller.setUser(currentUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showHabits() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/habits.fxml"));
            Parent habitsView = loader.load();

            HabitController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(habitsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/statistics.fxml"));
            Parent statisticsView = loader.load();

            StatisticsController controller = loader.getController();
            controller.setUser(currentUser);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(statisticsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}