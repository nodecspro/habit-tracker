package org.example.habittracker.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.habittracker.model.Habit;
import org.example.habittracker.model.User;
import org.example.habittracker.service.HabitService;
import org.example.habittracker.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    @FXML
    private Label activeHabitsLabel;

    @FXML
    private Label completionRateLabel;

    @FXML
    private Label currentStreakLabel;

    @FXML
    private TableView<Habit> todayHabitsTable;

    @FXML
    private TableColumn<Habit, String> habitNameColumn;

    @FXML
    private TableColumn<Habit, String> habitStatusColumn;

    @FXML
    private TableColumn<Habit, Void> habitActionsColumn;

    private User user;
    private final HabitService habitService;
    private final StatisticsService statisticsService;

    @Autowired
    public DashboardController(HabitService habitService, StatisticsService statisticsService) {
        this.habitService = habitService;
        this.statisticsService = statisticsService;
    }

    public void initialize() {
        // Настройка таблицы и колонок
        setupTable();
    }

    private void setupTable() {
        // Настройка колонок таблицы
        habitNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        // Настройка колонки статуса
        habitStatusColumn.setCellValueFactory(cellData -> {
            Habit habit = cellData.getValue();
            boolean completed = statisticsService.isHabitCompletedForDate(habit.getId(), LocalDate.now());
            return new javafx.beans.property.SimpleStringProperty(completed ? "Completed" : "Pending");
        });

        // Настройка колонки действий (кнопки)
        habitActionsColumn.setCellFactory(col -> new TableCell<Habit, Void>() {
            private final Button markButton = new Button("Mark");

            {
                markButton.setOnAction(event -> {
                    Habit habit = getTableView().getItems().get(getIndex());
                    boolean currentStatus = statisticsService.isHabitCompletedForDate(habit.getId(), LocalDate.now());
                    habitService.markHabitAsCompleted(habit.getId(), LocalDate.now(), !currentStatus);
                    updateUI(); // Обновить интерфейс после изменения
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(markButton);
                }
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
        updateUI();
    }

    private void updateUI() {
        if (user != null) {
            // Загрузка привычек пользователя
            List<Habit> habits = habitService.getUserHabits(user.getId());

            // Обновление статистики
            activeHabitsLabel.setText(String.valueOf(habits.size()));

            // Расчет процента выполнения за последнюю неделю
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            double completionRate = statisticsService.getUserCompletionRate(user.getId(), weekAgo, today);
            completionRateLabel.setText(String.format("%.1f%%", completionRate * 100));

            // Текущая серия
            int streak = statisticsService.getUserCurrentStreak(user.getId());
            currentStreakLabel.setText(streak + " days");

            // Обновление таблицы привычек
            todayHabitsTable.getItems().setAll(habits);
        }
    }

    @FXML
    public void handleAddHabit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/habit-form.fxml"));
            Parent root = loader.load();

            HabitFormController controller = loader.getController();
            controller.setUser(user);
            controller.setOnSaveCallback(this::updateUI);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Add New Habit");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}