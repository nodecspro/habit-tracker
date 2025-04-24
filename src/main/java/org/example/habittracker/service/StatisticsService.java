package org.example.habittracker.service;

import org.example.habittracker.model.Habit;
import org.example.habittracker.model.HabitRecord;
import org.example.habittracker.repository.HabitRecordRepository;
import org.example.habittracker.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StatisticsService {

    private final HabitRecordRepository recordRepository;
    private final HabitRepository habitRepository;

    @Autowired
    public StatisticsService(HabitRecordRepository recordRepository, HabitRepository habitRepository) {
        this.recordRepository = recordRepository;
        this.habitRepository = habitRepository;
    }

    /**
     * Получает статистику выполнения привычки за указанный период
     */
    public Map<LocalDate, Boolean> getHabitStatistics(Long habitId, LocalDate startDate, LocalDate endDate) {
        List<HabitRecord> records = recordRepository.findByHabitIdAndRecordDateBetween(habitId, startDate, endDate);

        Map<LocalDate, Boolean> statistics = new HashMap<>();

        // Заполняем все даты в диапазоне значением false
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            statistics.put(date, false);
        }

        // Обновляем значения для дат, когда привычка была выполнена
        for (HabitRecord record : records) {
            statistics.put(record.getRecordDate(), record.isCompleted());
        }

        return statistics;
    }

    /**
     * Рассчитывает процент выполнения конкретной привычки за указанный период
     */
    public double getHabitCompletionRate(Long habitId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Boolean> statistics = getHabitStatistics(habitId, startDate, endDate);

        long completedDays = statistics.values().stream().filter(Boolean::booleanValue).count();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        return totalDays > 0 ? (double) completedDays / totalDays : 0;
    }

    /**
     * Рассчитывает средний процент выполнения всех привычек пользователя за указанный период
     */
    public double getUserCompletionRate(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Habit> habits = habitRepository.findByUserId(userId);
        if (habits.isEmpty()) {
            return 0;
        }

        double totalRate = 0;
        for (Habit habit : habits) {
            totalRate += getHabitCompletionRate(habit.getId(), startDate, endDate);
        }

        return totalRate / habits.size();
    }

    /**
     * Рассчитывает текущую серию выполнения конкретной привычки
     */
    public int getHabitCurrentStreak(Long habitId) {
        // Логика для расчета текущей серии выполнения привычки
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate currentDate = today;

        while (true) {
            if (isHabitCompletedForDate(habitId, currentDate)) {
                streak++;
                currentDate = currentDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * Рассчитывает текущую серию выполнения всех привычек пользователя
     */
    public int getUserCurrentStreak(Long userId) {
        // Логика для расчета текущей серии выполнения всех привычек пользователя
        List<Habit> habits = habitRepository.findByUserId(userId);
        if (habits.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        int streak = 0;

        // Проверяем каждый день, начиная с сегодняшнего и идя назад
        LocalDate currentDate = today;
        boolean allCompleted;

        do {
            allCompleted = true;
            for (Habit habit : habits) {
                if (!isHabitCompletedForDate(habit.getId(), currentDate)) {
                    allCompleted = false;
                    break;
                }
            }

            if (allCompleted) {
                streak++;
                currentDate = currentDate.minusDays(1);
            }
        } while (allCompleted);

        return streak;
    }

    /**
     * Проверяет, выполнена ли привычка в указанную дату
     */
    public boolean isHabitCompletedForDate(Long habitId, LocalDate date) {
        Optional<HabitRecord> record = recordRepository.findByHabitIdAndRecordDate(habitId, date);
        return record.isPresent() && record.get().isCompleted();
    }

    /**
     * Рассчитывает лучшую серию выполнения привычки за указанный период
     */
    public int getBestStreak(Long habitId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Boolean> statistics = getHabitStatistics(habitId, startDate, endDate);

        int bestStreak = 0;
        int currentStreak = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean completed = statistics.getOrDefault(date, false);

            if (completed) {
                currentStreak++;
                bestStreak = Math.max(bestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return bestStreak;
    }
}