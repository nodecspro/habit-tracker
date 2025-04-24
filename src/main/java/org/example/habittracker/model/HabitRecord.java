package org.example.habittracker.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "habit_records")
public class HabitRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(nullable = false)
    private boolean completed;

    @ManyToOne
    @JoinColumn(name = "habit_id")
    private Habit habit;

    // Конструкторы
    public HabitRecord() {
        // Пустой конструктор для JPA
    }

    public HabitRecord(LocalDate recordDate, boolean completed) {
        this.recordDate = recordDate;
        this.completed = completed;
    }

    public HabitRecord(LocalDate recordDate, boolean completed, Habit habit) {
        this.recordDate = recordDate;
        this.completed = completed;
        this.habit = habit;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    // equals, hashCode и toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabitRecord record = (HabitRecord) o;
        return Objects.equals(id, record.id) &&
                Objects.equals(recordDate, record.recordDate) &&
                Objects.equals(habit, record.habit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recordDate, habit);
    }

    @Override
    public String toString() {
        return "HabitRecord{" +
                "id=" + id +
                ", recordDate=" + recordDate +
                ", completed=" + completed +
                '}';
    }
}