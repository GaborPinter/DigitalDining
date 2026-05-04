package com.DigitalDining.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.WeeklyMenuDay;

public interface WeeklyMenuDayRepository extends JpaRepository<WeeklyMenuDay, Long> {
    Optional<WeeklyMenuDay> findByMenuDate(LocalDate menuDate);

    List<WeeklyMenuDay> findByMenuDateBetweenOrderByMenuDateAsc(LocalDate from, LocalDate to);
}