package com.DigitalDining.demo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WeeklyMenuOrderSummary(
        LocalDate date,
        String pageTitle,
        String cartLabel,
        String descriptionHtml,
        BigDecimal totalPrice
) {}