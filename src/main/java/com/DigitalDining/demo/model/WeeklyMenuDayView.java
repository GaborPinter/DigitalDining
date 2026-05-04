package com.DigitalDining.demo.model;

import java.time.LocalDate;
import java.util.List;

public record WeeklyMenuDayView(
        LocalDate menuDate,
        String dayName,
        String dateLabel,
        boolean today,
        String title,
        String subtitle,
        List<WeeklyMenuItemView> soups,
        List<WeeklyMenuItemView> mains,
        List<WeeklyMenuItemView> desserts
) {}
