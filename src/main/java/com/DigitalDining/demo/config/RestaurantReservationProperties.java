package com.DigitalDining.demo.config;

import java.time.LocalTime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "restaurant.reservation")
public class RestaurantReservationProperties {

	private LocalTime openTime = LocalTime.of(11, 0);
    private LocalTime closeTime = LocalTime.of(23, 0);
    private int slotDurationMinutes = 120;
    private int maxDaysAhead = 30;

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(int slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public int getMaxDaysAhead() {
        return maxDaysAhead;
    }

    public void setMaxDaysAhead(int maxDaysAhead) {
        this.maxDaysAhead = maxDaysAhead;
    }
}
