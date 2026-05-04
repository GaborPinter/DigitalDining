package com.DigitalDining.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
	@EntityGraph(attributePaths = "category")
	List<MenuItem> findAllByOrderByCategory_DisplayOrderAscNameAsc();
	
	@EntityGraph(attributePaths = "category")
	List<MenuItem> findByNameContainingIgnoreCaseOrderByCategory_DisplayOrderAscNameAsc(String name);
}
