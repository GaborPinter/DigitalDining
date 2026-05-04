package com.DigitalDining.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.DiningTable;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
	List<DiningTable> findByActiveTrueOrderByCapacityAscTableCodeAsc();

    boolean existsByTableCodeIgnoreCase(String tableCode);
}
