package com.DigitalDining.demo.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record SlotAvailabilityView(
        LocalTime startTime,
        LocalTime endTime,
        long availableTables
) {
    public String label() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return startTime.format(fmt) + " – " + endTime.format(fmt);
    }
}
