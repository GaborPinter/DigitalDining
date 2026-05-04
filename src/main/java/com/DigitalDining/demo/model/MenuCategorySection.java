package com.DigitalDining.demo.model;

import java.util.List;

public class MenuCategorySection {

	private final MenuCategory category;
    private final String displayName;
    private final List<MenuItem> items;
    
	public MenuCategorySection(MenuCategory category, String displayName, List<MenuItem> items) {
		this.category = category;
		this.displayName = displayName;
		this.items = items;
	}

	public MenuCategory getCategory() {
		return category;
	}

	public String getDisplayName() {
		return displayName;
	}

	public List<MenuItem> getItems() {
		return items;
	}

}
