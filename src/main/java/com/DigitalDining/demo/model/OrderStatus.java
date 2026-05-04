package com.DigitalDining.demo.model;

public enum OrderStatus {
	PENDING("Függőben"), PAID("Kifizetve"), CANCELLED("Lemondva");
	
	private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
