package com.DigitalDining.demo.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "menu_categories")
@NoArgsConstructor
public class MenuCategory {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Leves, Főétel, Desszert, Ital

    private String icon; // opcionális: pl. 🍲, 🍝, 🍰, 🥤

    private Integer displayOrder = 0;

    private boolean active = true;

    @OneToMany(mappedBy = "category")
    private List<MenuItem> items = new ArrayList<>();

    public MenuCategory(String name, String icon, Integer displayOrder, boolean active) {
        this.name = name;
        this.icon = icon;
        this.displayOrder = displayOrder;
        this.active = active;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getDisplayOrder() {
		return displayOrder;
	}

	public void setDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<MenuItem> getItems() {
		return items;
	}

	public void setItems(List<MenuItem> items) {
		this.items = items;
	}
}
