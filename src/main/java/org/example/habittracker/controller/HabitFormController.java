package org.example.habittracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.habittracker.model.Habit;
import org.example.habittracker.model.User;
import org.example.habittracker.service.HabitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class HabitFormController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionArea;

    private User user;
    private Habit habitToEdit;
    private Runnable onSaveCallback;

    private final HabitService habitService;

    @Autowired
    public HabitFormController(HabitService habitService) {
        this.habitService = habitService;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setHabitToEdit(Habit habit) {
        this.habitToEdit = habit;
        nameField.setText(habit.getName());
        descriptionArea.setText(habit.getDescription());
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    public void handleSave() {
        String name = nameField.getText();
        String description = descriptionArea.getText();

        if (name == null || name.trim().isEmpty()) {
            // Показать ошибку
            return;
        }

        if (habitToEdit == null) {
            // Создание новой привычки
            habitService.createHabit(name, description, user);
        } else {
            // Обновление существующей привычки
            habitService.updateHabit(habitToEdit.getId(), name, description);
        }

        if (onSaveCallback != null) {
            onSaveCallback.run();
        }

        // Закрыть окно
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML
    public void handleCancel() {
        // Закрыть окно без сохранения
        ((Stage) nameField.getScene().getWindow()).close();
    }
}