package com.DigitalDining.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.MenuCategory;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
	boolean existsByNameIgnoreCase(String name);
    List<MenuCategory> findAllByActiveTrueOrderByDisplayOrderAscNameAsc();
}
