package com.DigitalDining.demo.model;

import java.math.BigDecimal;

public record WeeklyMenuItemView(
        String optionLabel,
        String name,
        String description,
        BigDecimal price
) {}
