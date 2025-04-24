package org.example.habittracker.repository;

import org.example.habittracker.model.HabitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRecordRepository extends JpaRepository<HabitRecord, Long> {
    List<HabitRecord> findByHabitIdAndRecordDateBetween(Long habitId, LocalDate startDate, LocalDate endDate);
    Optional<HabitRecord> findByHabitIdAndRecordDate(Long habitId, LocalDate date);
}