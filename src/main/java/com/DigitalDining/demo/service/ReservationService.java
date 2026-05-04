package com.DigitalDining.demo.service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DigitalDining.demo.config.RestaurantReservationProperties;
import com.DigitalDining.demo.dto.ReservationForm;
import com.DigitalDining.demo.dto.SlotAvailabilityView;
import com.DigitalDining.demo.dto.UserResponse;
import com.DigitalDining.demo.model.DiningTable;
import com.DigitalDining.demo.model.ReservationStatus;
import com.DigitalDining.demo.model.TableReservation;
import com.DigitalDining.demo.repository.DiningTableRepository;
import com.DigitalDining.demo.repository.TableReservationRepository;

@Service
@Transactional
public class ReservationService {

	private final DiningTableRepository diningTableRepository;
    private final TableReservationRepository tableReservationRepository;
    private final RestaurantReservationProperties properties;
    private final UserService userService;

    public ReservationService(DiningTableRepository diningTableRepository,
                              TableReservationRepository tableReservationRepository,
                              RestaurantReservationProperties properties,
                              UserService userService) {
        this.diningTableRepository = diningTableRepository;
        this.tableReservationRepository = tableReservationRepository;
        this.properties = properties;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public int getMaxPartySize() {
        return diningTableRepository.findByActiveTrueOrderByCapacityAscTableCodeAsc()
                .stream()
                .mapToInt(DiningTable::getCapacity)
                .max()
                .orElse(8);
    }

    @Transactional(readOnly = true)
    public List<SlotAvailabilityView> getAvailability(LocalDate selectedDate, int partySize) {
        LocalDate date = normalizeDate(selectedDate);
        int size = Math.max(1, partySize);

        List<DiningTable> eligibleTables = diningTableRepository.findByActiveTrueOrderByCapacityAscTableCodeAsc()
                .stream()
                .filter(t -> t.getCapacity() >= size)
                .toList();

        Set<Long> bookedTableIds = new HashSet<>();

        List<SlotAvailabilityView> slots = buildSlots().stream()
                .filter(slot -> shouldShowSlot(date, slot))
                .map(slot -> {
                    bookedTableIds.clear();
                    bookedTableIds.addAll(
                            tableReservationRepository.findByReservationDateAndStartTimeAndStatus(
                                    date, slot, ReservationStatus.ACTIVE
                            ).stream()
                             .map(r -> r.getDiningTable().getId())
                             .toList()
                    );

                    long available = eligibleTables.stream()
                            .filter(t -> !bookedTableIds.contains(t.getId()))
                            .count();

                    return new SlotAvailabilityView(
                            slot,
                            slot.plusMinutes(properties.getSlotDurationMinutes()),
                            available
                    );
                })
                .toList();

        return slots;
    }

    public TableReservation createReservation(ReservationForm form, Principal principal) {
        LocalDate date = normalizeDate(form.getReservationDate());
        LocalTime startTime = form.getStartTime();
        int partySize = form.getPartySize() == null ? 1 : form.getPartySize();

        validateDate(date);
        validateSlot(startTime);
        validatePartySize(partySize);

        String guestName = safeTrim(form.getGuestName());
        String guestEmail = safeTrim(form.getGuestEmail());
        String guestPhone = safeTrim(form.getGuestPhone());

        if ((guestName == null || guestName.isBlank()) && principal != null) {
            UserResponse currentUser = userService.findByUsername(principal.getName());
            if (currentUser != null) {
                guestName = currentUser.getUsername();
            }
        }

        if ((guestEmail == null || guestEmail.isBlank()) && principal != null) {
            UserResponse currentUser = userService.findByUsername(principal.getName());
            if (currentUser != null) {
                guestEmail = currentUser.getEmail();
            }
        }

        if (guestName == null || guestName.isBlank()) {
            throw new IllegalArgumentException("A név megadása kötelező.");
        }
        if (guestEmail == null || guestEmail.isBlank()) {
            throw new IllegalArgumentException("Az e-mail megadása kötelező.");
        }

        List<DiningTable> candidateTables = diningTableRepository.findByActiveTrueOrderByCapacityAscTableCodeAsc()
                .stream()
                .filter(t -> t.getCapacity() >= partySize)
                .toList();

        if (candidateTables.isEmpty()) {
            throw new IllegalArgumentException("Nincs megfelelő méretű szabad asztal ehhez a létszámhoz.");
        }

        DiningTable assignedTable = null;

        for (DiningTable table : candidateTables) {
            boolean occupied = tableReservationRepository.existsByDiningTableAndReservationDateAndStartTimeAndStatus(
                    table, date, startTime, ReservationStatus.ACTIVE
            );
            if (!occupied) {
                assignedTable = table;
                break;
            }
        }

        if (assignedTable == null) {
            throw new IllegalArgumentException("Erre az időpontra már nincs szabad asztal.");
        }

        TableReservation reservation = new TableReservation();
        reservation.setReservationCode(generateUniqueCode());
        reservation.setReservationDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(startTime.plusMinutes(properties.getSlotDurationMinutes()));
        reservation.setPartySize(partySize);
        reservation.setGuestName(guestName);
        reservation.setGuestEmail(guestEmail);
        reservation.setGuestPhone(guestPhone);
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setDiningTable(assignedTable);

        return tableReservationRepository.save(reservation);
    }

    public void cancelReservation(String reservationCode, String email, boolean admin) {
        String code = safeTrim(reservationCode);
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Add meg a foglaláskódot.");
        }

        TableReservation reservation = tableReservationRepository
                .findFirstByReservationCodeIgnoreCaseAndStatus(code, ReservationStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Nincs aktív foglalás ezzel a kóddal."));

        if (!admin) {
            String normalizedEmail = safeTrim(email);
            if (normalizedEmail == null || normalizedEmail.isBlank()) {
                throw new IllegalArgumentException("A foglalás felszabadításához add meg az e-mail címet is.");
            }
            if (!reservation.getGuestEmail().equalsIgnoreCase(normalizedEmail)) {
                throw new IllegalArgumentException("Az e-mail nem egyezik a foglaláshoz tartozó címmel.");
            }
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        tableReservationRepository.save(reservation);
    }

    private List<LocalTime> buildSlots() {
        List<LocalTime> slots = new java.util.ArrayList<>();
        LocalTime current = properties.getOpenTime();
        LocalTime lastStart = properties.getCloseTime().minusMinutes(properties.getSlotDurationMinutes());

        while (!current.isAfter(lastStart)) {
            slots.add(current);
            current = current.plusMinutes(properties.getSlotDurationMinutes());
        }
        return slots;
    }

    private boolean shouldShowSlot(LocalDate date, LocalTime slot) {
        if (!date.equals(LocalDate.now())) {
            return true;
        }
        return !slot.isBefore(LocalTime.now());
    }

    private LocalDate normalizeDate(LocalDate date) {
        if (date == null) {
            return LocalDate.now();
        }
        if (date.isBefore(LocalDate.now())) {
            return LocalDate.now();
        }
        LocalDate maxDate = LocalDate.now().plusDays(properties.getMaxDaysAhead());
        if (date.isAfter(maxDate)) {
            return maxDate;
        }
        return date;
    }

    private void validateDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(properties.getMaxDaysAhead());

        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Múltbeli dátumra nem lehet foglalni.");
        }
        if (date.isAfter(maxDate)) {
            throw new IllegalArgumentException("Erre a dátumra még nem lehet foglalni.");
        }
    }

    private void validateSlot(LocalTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Válassz időpontot.");
        }
        boolean valid = buildSlots().contains(startTime);
        if (!valid) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
            throw new IllegalArgumentException("Érvénytelen időpont: " + startTime.format(fmt));
        }
    }

    private void validatePartySize(int partySize) {
        if (partySize < 1) {
            throw new IllegalArgumentException("A létszám legalább 1 fő legyen.");
        }
        int max = getMaxPartySize();
        if (partySize > max) {
            throw new IllegalArgumentException("Ekkora létszámra nincs megfelelő asztal.");
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "DD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (tableReservationRepository.existsByReservationCodeIgnoreCase(code));
        return code;
    }

    private String safeTrim(String value) {
        return value == null ? null : value.trim();
    }
}
