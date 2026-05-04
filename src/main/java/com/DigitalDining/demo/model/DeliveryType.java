package com.DigitalDining.demo.model;

public enum DeliveryType {
	TAKEAWAY("Elvitel"), DELIVERY("Kiszállítás");
	
	private final String displayName;

    DeliveryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
