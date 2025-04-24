package org.example.habittracker.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.example.habittracker.model.Habit;
import org.example.habittracker.model.User;
import org.example.habittracker.service.HabitService;
import org.example.habittracker.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class StatisticsController {

    @FXML
    private ComboBox<Habit> habitComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private BarChart<String, Number> weeklyChart;

    @FXML
    private LineChart<String, Number> monthlyChart;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label completionRateLabel;

    @FXML
    private Label currentStreakLabel;

    @FXML
    private Label bestStreakLabel;

    private User user;
    private final HabitService habitService;
    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(HabitService habitService, StatisticsService statisticsService) {
        this.habitService = habitService;
        this.statisticsService = statisticsService;
    }

    public void initialize() {
        // Настройка DatePicker с значениями по умолчанию
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        startDatePicker.setValue(firstDayOfMonth);
        endDatePicker.setValue(today);

        // Настройка обработчиков событий
        habitComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateStatistics();
            }
        });

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateStatistics());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateStatistics());
    }

    public void setUser(User user) {
        this.user = user;
        loadUserHabits();
    }

    private void loadUserHabits() {
        if (user != null) {
            List<Habit> habits = habitService.getUserHabits(user.getId());
            habitComboBox.setItems(FXCollections.observableArrayList(habits));

            // Выбираем первую привычку, если она есть
            if (!habits.isEmpty()) {
                habitComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    @FXML
    public void applyFilter() {
        updateStatistics();
    }

    private void updateStatistics() {
        Habit selectedHabit = habitComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedHabit == null || startDate == null || endDate == null) {
            return;
        }

        // Проверка корректности дат
        if (startDate.isAfter(endDate)) {
            // Показать ошибку
            return;
        }

        // Обновление графиков и статистики
        updateWeeklyChart(selectedHabit, startDate, endDate);
        updateMonthlyChart(selectedHabit, startDate, endDate);
        updateCalendarView(selectedHabit, startDate, endDate);
        updateStatisticsLabels(selectedHabit, startDate, endDate);
    }

    private void updateWeeklyChart(Habit habit, LocalDate startDate, LocalDate endDate) {
        weeklyChart.getData().clear();

        // Если период больше 2 недель, группируем по неделям
        if (ChronoUnit.DAYS.between(startDate, endDate) > 14) {
            updateWeeklyAggregatedChart(habit, startDate, endDate);
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(habit.getName());

        Map<LocalDate, Boolean> statistics = statisticsService.getHabitStatistics(habit.getId(), startDate, endDate);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            String dateStr = dayName + " " + date.format(DateTimeFormatter.ofPattern("dd/MM"));

            boolean completed = statistics.getOrDefault(date, false);
            series.getData().add(new XYChart.Data<>(dateStr, completed ? 1 : 0));
        }

        weeklyChart.getData().add(series);
    }

    private void updateWeeklyAggregatedChart(Habit habit, LocalDate startDate, LocalDate endDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(habit.getName());

        Map<LocalDate, Boolean> statistics = statisticsService.getHabitStatistics(habit.getId(), startDate, endDate);

        // Группировка по неделям
        LocalDate currentWeekStart = startDate;
        while (!currentWeekStart.isAfter(endDate)) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }

            int completedDays = 0;
            int totalDays = 0;

            for (LocalDate date = currentWeekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
                if (statistics.getOrDefault(date, false)) {
                    completedDays++;
                }
                totalDays++;
            }

            double completionRate = totalDays > 0 ? (double) completedDays / totalDays : 0;
            String weekLabel = currentWeekStart.format(DateTimeFormatter.ofPattern("dd/MM")) +
                    " - " +
                    weekEnd.format(DateTimeFormatter.ofPattern("dd/MM"));

            series.getData().add(new XYChart.Data<>(weekLabel, completionRate * 100));

            currentWeekStart = weekEnd.plusDays(1);
        }

        weeklyChart.getData().add(series);
    }

    private void updateMonthlyChart(Habit habit, LocalDate startDate, LocalDate endDate) {
        monthlyChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completion Rate (%)");

        // Если период больше 31 дня, группируем по неделям
        if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
            updateMonthlyAggregatedChart(habit, startDate, endDate);
            return;
        }

        // Расчет скользящего среднего за 7 дней
        List<Double> movingAverages = calculateMovingAverage(habit, startDate, endDate, 7);

        int index = 0;
        for (LocalDate date = startDate.plusDays(6); !date.isAfter(endDate); date = date.plusDays(1)) {
            if (index < movingAverages.size()) {
                String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM"));
                series.getData().add(new XYChart.Data<>(dateStr, movingAverages.get(index) * 100));
                index++;
            }
        }

        monthlyChart.getData().add(series);
    }

    private void updateMonthlyAggregatedChart(Habit habit, LocalDate startDate, LocalDate endDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Completion Rate (%)");

        // Группировка по месяцам
        LocalDate currentMonth = startDate.withDayOfMonth(1);
        while (!currentMonth.isAfter(endDate)) {
            LocalDate monthEnd = currentMonth.with(TemporalAdjusters.lastDayOfMonth());
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            LocalDate actualStart = currentMonth.isBefore(startDate) ? startDate : currentMonth;

            double completionRate = statisticsService.getHabitCompletionRate(
                    habit.getId(), actualStart, monthEnd);

            String monthLabel = currentMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()) +
                    " " + currentMonth.getYear();

            series.getData().add(new XYChart.Data<>(monthLabel, completionRate * 100));

            currentMonth = currentMonth.plusMonths(1);
        }

        monthlyChart.getData().add(series);
    }

    private List<Double> calculateMovingAverage(Habit habit, LocalDate startDate, LocalDate endDate, int windowSize) {
        Map<LocalDate, Boolean> statistics = statisticsService.getHabitStatistics(habit.getId(), startDate, endDate);
        List<Double> movingAverages = new ArrayList<>();

        for (LocalDate date = startDate.plusDays(windowSize - 1); !date.isAfter(endDate); date = date.plusDays(1)) {
            int completed = 0;

            for (int i = 0; i < windowSize; i++) {
                LocalDate day = date.minusDays(i);
                if (statistics.getOrDefault(day, false)) {
                    completed++;
                }
            }

            double average = (double) completed / windowSize;
            movingAverages.add(average);
        }

        return movingAverages;
    }

    private void updateCalendarView(Habit habit, LocalDate startDate, LocalDate endDate) {
        calendarGrid.getChildren().clear();

        // Получаем статистику привычки
        Map<LocalDate, Boolean> statistics = statisticsService.getHabitStatistics(habit.getId(), startDate, endDate);

        // Определяем количество недель для отображения
        LocalDate firstDayOfCalendar = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfCalendar = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        int weeks = (int) (ChronoUnit.DAYS.between(firstDayOfCalendar, lastDayOfCalendar) + 1) / 7;

        // Добавляем заголовки дней недели
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.of(i + 1);
            Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()));
            dayLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Заполняем календарь
        LocalDate currentDate = firstDayOfCalendar;
        for (int week = 0; week < weeks; week++) {
            for (int day = 0; day < 7; day++) {
                StackPane dayCell = createDayCell(currentDate, statistics);
                calendarGrid.add(dayCell, day, week + 1);
                currentDate = currentDate.plusDays(1);
            }
        }
    }

    private StackPane createDayCell(LocalDate date, Map<LocalDate, Boolean> statistics) {
        StackPane cell = new StackPane();
        cell.getStyleClass().add("calendar-day");

        // Создаем фон ячейки
        Rectangle background = new Rectangle(40, 40);
        background.setArcWidth(10);
        background.setArcHeight(10);

        // Определяем цвет в зависимости от статуса выполнения
        if (statistics.containsKey(date) && statistics.get(date)) {
            background.setFill(Color.valueOf("#4CAF50")); // Зеленый для выполненных
            cell.getStyleClass().add("calendar-day-completed");
        } else if (date.isBefore(LocalDate.now().plusDays(1))) {
            background.setFill(Color.valueOf("#F44336")); // Красный для пропущенных
            cell.getStyleClass().add("calendar-day-missed");
        } else {
            background.setFill(Color.valueOf("#E0E0E0")); // Серый для будущих
        }

        // Выделяем сегодняшний день
        if (date.equals(LocalDate.now())) {
            cell.getStyleClass().add("calendar-day-today");
        }

        // Добавляем текст с датой
        Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dateLabel.setTextFill(Color.WHITE);

        cell.getChildren().addAll(background, dateLabel);
        return cell;
    }

    private void updateStatisticsLabels(Habit habit, LocalDate startDate, LocalDate endDate) {
        // Расчет процента выполнения
        double completionRate = statisticsService.getHabitCompletionRate(habit.getId(), startDate, endDate);
        completionRateLabel.setText(String.format("%.1f%%", completionRate * 100));

        // Расчет текущей серии
        int currentStreak = statisticsService.getHabitCurrentStreak(habit.getId());
        currentStreakLabel.setText(currentStreak + " days");

        // Расчет лучшей серии
        int bestStreak = statisticsService.getBestStreak(habit.getId(), startDate, endDate);
        bestStreakLabel.setText(bestStreak + " days");
    }

    /**
     * Метод для экспорта статистики в CSV файл
     */
    @FXML
    public void exportStatistics() {
        Habit selectedHabit = habitComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedHabit == null || startDate == null || endDate == null) {
            // Показать сообщение об ошибке
            return;
        }

        try {
            // Создаем диалог выбора файла
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Statistics");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName(selectedHabit.getName() + "_statistics.csv");

            File file = fileChooser.showSaveDialog(calendarGrid.getScene().getWindow());
            if (file != null) {
                exportToCsv(selectedHabit, startDate, endDate, file);

                // Показать сообщение об успешном экспорте
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText(null);
                alert.setContentText("Statistics have been exported successfully.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            // Показать сообщение об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Export Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while exporting statistics: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Экспорт данных в CSV файл
     */
    private void exportToCsv(Habit habit, LocalDate startDate, LocalDate endDate, File file) throws IOException {
        Map<LocalDate, Boolean> statistics = statisticsService.getHabitStatistics(habit.getId(), startDate, endDate);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Записываем заголовок
            writer.println("Date,Day,Completed");

            // Записываем данные
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
                boolean completed = statistics.getOrDefault(date, false);

                writer.println(date + "," + dayName + "," + (completed ? "Yes" : "No"));
            }
        }
    }

    /**
     * Метод для печати отчета
     */
    @FXML
    public void printReport() {
        Habit selectedHabit = habitComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedHabit == null || startDate == null || endDate == null) {
            // Показать сообщение об ошибке
            return;
        }

        try {
            // Создаем временный HTML-файл для печати
            File tempFile = File.createTempFile("habit_report_", ".html");
            generateHtmlReport(selectedHabit, startDate, endDate, tempFile);

            // Открываем файл в браузере для печати
            Desktop.getDesktop().browse(tempFile.toURI());

            // Удаляем файл при выходе из приложения
            tempFile.deleteOnExit();
        } catch (Exception e) {
            // Показать сообщение об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Print Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while preparing the report: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Генерация HTML-отчета
     */
    private void generateHtmlReport(Habit habit, LocalDate startDate, LocalDate endDate, File file) throws IOException {
        Map<LocalDate, Boolean> statistics = statisticsService.getHabitStatistics(habit.getId(), startDate, endDate);
        double completionRate = statisticsService.getHabitCompletionRate(habit.getId(), startDate, endDate);
        int currentStreak = statisticsService.getHabitCurrentStreak(habit.getId());
        int bestStreak = statisticsService.getBestStreak(habit.getId(), startDate, endDate);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Habit Report: " + habit.getName() + "</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1, h2 { color: #3f51b5; }");
            writer.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println("tr:nth-child(even) { background-color: #f9f9f9; }");
            writer.println(".completed { color: green; }");
            writer.println(".missed { color: red; }");
            writer.println(".stats { display: flex; justify-content: space-between; margin: 20px 0; }");
            writer.println(".stat-box { border: 1px solid #ddd; padding: 15px; border-radius: 5px; width: 30%; text-align: center; }");
            writer.println(".stat-value { font-size: 24px; font-weight: bold; color: #3f51b5; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");

            // Заголовок
            writer.println("<h1>Habit Report: " + habit.getName() + "</h1>");
            writer.println("<p>Period: " + startDate + " to " + endDate + "</p>");

            // Статистика
            writer.println("<div class='stats'>");
            writer.println("<div class='stat-box'>");
            writer.println("<p>Completion Rate</p>");
            writer.println("<p class='stat-value'>" + String.format("%.1f%%", completionRate * 100) + "</p>");
            writer.println("</div>");
            writer.println("<div class='stat-box'>");
            writer.println("<p>Current Streak</p>");
            writer.println("<p class='stat-value'>" + currentStreak + " days</p>");
            writer.println("</div>");
            writer.println("<div class='stat-box'>");
            writer.println("<p>Best Streak</p>");
            writer.println("<p class='stat-value'>" + bestStreak + " days</p>");
            writer.println("</div>");
            writer.println("</div>");

            // Таблица с данными
            writer.println("<h2>Daily Records</h2>");
            writer.println("<table>");
            writer.println("<tr><th>Date</th><th>Day</th><th>Status</th></tr>");

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
                boolean completed = statistics.getOrDefault(date, false);

                writer.println("<tr>");
                writer.println("<td>" + date + "</td>");
                writer.println("<td>" + dayName + "</td>");

                if (completed) {
                    writer.println("<td class='completed'>Completed</td>");
                } else if (date.isBefore(LocalDate.now().plusDays(1))) {
                    writer.println("<td class='missed'>Missed</td>");
                } else {
                    writer.println("<td>Upcoming</td>");
                }

                writer.println("</tr>");
            }

            writer.println("</table>");

            // Описание привычки
            if (habit.getDescription() != null && !habit.getDescription().isEmpty()) {
                writer.println("<h2>Habit Description</h2>");
                writer.println("<p>" + habit.getDescription() + "</p>");
            }

            writer.println("<script>");
            writer.println("window.onload = function() { window.print(); }");
            writer.println("</script>");
            writer.println("</body>");
            writer.println("</html>");
        }
    }

    /**
     * Метод для обновления данных на графиках
     */
    @FXML
    public void refreshData() {
        updateStatistics();
    }

    /**
     * Метод для сброса фильтров
     */
    @FXML
    public void resetFilters() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());

        startDatePicker.setValue(firstDayOfMonth);
        endDatePicker.setValue(today);

        if (!habitComboBox.getItems().isEmpty()) {
            habitComboBox.getSelectionModel().selectFirst();
        }

        updateStatistics();
    }
}