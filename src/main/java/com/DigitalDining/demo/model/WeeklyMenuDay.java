package com.DigitalDining.demo.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "weekly_menu_days",
    uniqueConstraints = @UniqueConstraint(name = "uk_weekly_menu_day_date", columnNames = "menu_date")
)
public class WeeklyMenuDay {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<WeeklyMenuItem> items = new ArrayList<>();
    
    public WeeklyMenuDay() {
	}

	public void addItem(WeeklyMenuItem item) {
        item.setDay(this);
        items.add(item);
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getMenuDate() {
		return menuDate;
	}

	public void setMenuDate(LocalDate menuDate) {
		this.menuDate = menuDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public List<WeeklyMenuItem> getItems() {
		return items;
	}

	public void setItems(List<WeeklyMenuItem> items) {
		this.items = items;
	}
}
