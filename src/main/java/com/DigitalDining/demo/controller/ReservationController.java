package com.DigitalDining.demo.controller;

import java.security.Principal;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.DigitalDining.demo.dto.ReservationForm;
import com.DigitalDining.demo.model.TableReservation;
import com.DigitalDining.demo.service.ReservationService;

import jakarta.validation.Valid;

@Controller
public class ReservationController {

	private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservation/book")
    public String book(
            @Valid @ModelAttribute("reservationForm") ReservationForm reservationForm,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        addSelectedFilters(reservationForm, redirectAttributes);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("reservationError", "Ellenőrizd a foglalási adatokat.");
            return "redirect:/contact";
        }

        try {
            TableReservation reservation = reservationService.createReservation(reservationForm, principal);

            redirectAttributes.addFlashAttribute("reservationSuccess",
                    "Foglalás sikeres! Kód: " + reservation.getReservationCode()
                            + " | Asztal: " + reservation.getDiningTable().getTableCode()
                            + " | " + reservation.getReservationDate() + " "
                            + reservation.getStartTime() + "–" + reservation.getEndTime());

        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("reservationError", ex.getMessage());
        }

        return "redirect:/contact";
    }

    @PostMapping("/reservation/cancel")
    public String cancel(
            String reservationCode,
            String email,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        try {
            reservationService.cancelReservation(reservationCode, email, admin);
            redirectAttributes.addFlashAttribute("reservationSuccess", "A foglalás sikeresen felszabadítva.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("reservationError", ex.getMessage());
        }

        return "redirect:/contact";
    }

    private void addSelectedFilters(ReservationForm form, RedirectAttributes redirectAttributes) {
        if (form.getReservationDate() != null) {
            redirectAttributes.addAttribute("reservationDate", form.getReservationDate());
        }
        if (form.getPartySize() != null) {
            redirectAttributes.addAttribute("partySize", form.getPartySize());
        }
    }
}
