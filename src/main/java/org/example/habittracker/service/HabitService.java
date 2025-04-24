package org.example.habittracker.service;

import org.example.habittracker.model.Habit;
import org.example.habittracker.model.HabitRecord;
import org.example.habittracker.model.User;
import org.example.habittracker.repository.HabitRecordRepository;
import org.example.habittracker.repository.HabitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitRecordRepository recordRepository;

    @Autowired
    public HabitService(HabitRepository habitRepository, HabitRecordRepository recordRepository) {
        this.habitRepository = habitRepository;
        this.recordRepository = recordRepository;
    }

    public Habit createHabit(String name, String description, User user) {
        Habit habit = new Habit();
        habit.setName(name);
        habit.setDescription(description);
        habit.setCreatedAt(LocalDateTime.now());
        habit.setUser(user);

        return habitRepository.save(habit);
    }

    public List<Habit> getUserHabits(Long userId) {
        return habitRepository.findByUserId(userId);
    }

    public void markHabitAsCompleted(Long habitId, LocalDate date, boolean completed) {
        HabitRecord record = recordRepository.findByHabitIdAndRecordDate(habitId, date)
                .orElseGet(() -> {
                    HabitRecord newRecord = new HabitRecord();
                    newRecord.setHabit(habitRepository.findById(habitId).orElseThrow());
                    newRecord.setRecordDate(date);
                    return newRecord;
                });

        record.setCompleted(completed);
        recordRepository.save(record);
    }

    public void deleteHabit(Long habitId) {
        habitRepository.deleteById(habitId);
    }

    public Habit updateHabit(Long habitId, String name, String description) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));

        habit.setName(name);
        habit.setDescription(description);

        return habitRepository.save(habit);
    }
}