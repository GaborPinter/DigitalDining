package com.DigitalDining.demo.model;

public enum Role {
	USER("Felhasználó"),
    ADMIN("Adminisztrátor");
	
	private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
