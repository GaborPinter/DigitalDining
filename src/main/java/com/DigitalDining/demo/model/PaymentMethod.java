package com.DigitalDining.demo.model;

public enum PaymentMethod {
	CASH("Készpénz"), CARD("Bankkártya"), PAYPAL("PayPal");
	
	private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
