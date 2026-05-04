package com.DigitalDining.demo.model;

public enum WeeklyMenuCategory {

	SOUP("Leves", 1),
    MAIN("Főétel", 2),
    DESSERT("Desszert", 3);

    private final String displayName;
    private final int sortOrder;

    WeeklyMenuCategory(String displayName, int sortOrder) {
        this.displayName = displayName;
        this.sortOrder = sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
