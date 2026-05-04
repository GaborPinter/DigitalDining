package com.DigitalDining.demo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.DiningTable;
import com.DigitalDining.demo.model.ReservationStatus;
import com.DigitalDining.demo.model.TableReservation;

public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {
	boolean existsByDiningTableAndReservationDateAndStartTimeAndStatus(DiningTable diningTable,
			LocalDate reservationDate, LocalTime startTime, ReservationStatus status);

	List<TableReservation> findByReservationDateAndStartTimeAndStatus(LocalDate reservationDate, LocalTime startTime,
			ReservationStatus status);

	Optional<TableReservation> findFirstByReservationCodeIgnoreCaseAndStatus(String reservationCode,
			ReservationStatus status);

	boolean existsByReservationCodeIgnoreCase(String reservationCode);
}
