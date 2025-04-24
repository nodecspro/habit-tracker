package org.example.habittracker.controller;// Для аннотации FXML

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.habittracker.model.Habit;
import org.example.habittracker.model.User;
import org.example.habittracker.service.HabitService;

import java.util.List;

// Другие часто используемые компоненты JavaFX


public class HabitController {
    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ListView<Habit> habitsListView;

    private HabitService habitService;
    private User currentUser;

    public void initialize() {
        // Инициализация контроллера
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserHabits();
    }

    private void loadUserHabits() {
        List<Habit> habits = habitService.getUserHabits(currentUser.getId());
        habitsListView.getItems().setAll(habits);
    }

    @FXML
    public void handleCreateHabit() {
        String name = nameField.getText();
        String description = descriptionArea.getText();

        habitService.createHabit(name, description, currentUser);

        // Очистить поля
        nameField.clear();
        descriptionArea.clear();

        // Обновить список привычек
        loadUserHabits();
    }

    @FXML
    public void handleEditHabit() {
        Habit selectedHabit = habitsListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            String name = nameField.getText();
            String description = descriptionArea.getText();

            habitService.updateHabit(selectedHabit.getId(), name, description);

            // Обновить список привычек
            loadUserHabits();
        }
    }

    @FXML
    public void handleDeleteHabit() {
        Habit selectedHabit = habitsListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            habitService.deleteHabit(selectedHabit.getId());

            // Обновить список привычек
            loadUserHabits();
        }
    }

    @FXML
    public void handleMarkHabit() {
        Habit selectedHabit = habitsListView.getSelectionModel().getSelectedItem();
        if (selectedHabit != null) {
            // Открыть календарь для отметки привычки
            // ...
        }
    }
}