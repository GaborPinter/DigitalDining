package com.DigitalDining.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dining_tables")
public class DiningTable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String tableCode;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean active = true;

    public DiningTable() {
    }

    public DiningTable(String tableCode, int capacity, boolean active) {
        this.tableCode = tableCode;
        this.capacity = capacity;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
