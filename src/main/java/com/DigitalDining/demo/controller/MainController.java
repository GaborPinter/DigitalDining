package com.DigitalDining.demo.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.DigitalDining.demo.dto.ContactForm;
import com.DigitalDining.demo.dto.ReservationForm;
import com.DigitalDining.demo.dto.UserResponse;
import com.DigitalDining.demo.service.ContactEmailService;
import com.DigitalDining.demo.service.ReservationService;
import com.DigitalDining.demo.service.UserService;

@Controller
public class MainController {
	
	private final ReservationService reservationService;
	private final UserService userService;
	private final ContactEmailService emailService;
	
	public MainController(ReservationService reservationService, UserService userService, ContactEmailService emailService) {
		this.reservationService = reservationService;
		this.userService = userService;
		this.emailService = emailService;
	}

	@GetMapping({"/", "/mainPage"})
    public String showMainPage(Model model) {
		model.addAttribute("restaurantName", "Digital Dining");   // <- ide kell
	    model.addAttribute("slogan", "Finom ételek. Gyors rendelés.");
	    model.addAttribute("featuredDishes", List.of("Pizza", "Leves", "Saláta"));
        return "mainPage";
    }
	
	@GetMapping("/about")
    public String aboutPage(Model model) {
        // opcionálisan adhatsz ide oldal-specifikus adatot
        model.addAttribute("aboutTitle", "Rólunk");
        model.addAttribute("aboutText", "Üdv nálunk! A Digital Dining célja, hogy gyors és minőségi étkeztetést biztosítson.");
        return "about";
    }
	
	@GetMapping("/contact")
	public String contactPage(
	        Model model,
	        @RequestParam(value = "reservationDate", required = false) String reservationDate,
	        @RequestParam(value = "partySize", required = false) Integer partySize,
	        Principal principal) {

	    model.addAttribute("restaurantName", "Digital Dining");
	    model.addAttribute("contactForm", new ContactForm());

	    model.addAttribute("contactTitle", "Kapcsolat");
	    model.addAttribute("phone", "+36 30 086 3460");
	    model.addAttribute("phone2", "+36 30 216 3091");
	    model.addAttribute("openingHours", "H-Cs: 10:00-19:45 · P-Szo: 11:00-20:30");
	    model.addAttribute("address", "9021 Győr Bajcsy-Zsilinszky utca 30-32");
	    model.addAttribute("email", "info@digitaldining.hu");

	    LocalDate selectedDate = parseReservationDate(reservationDate);
	    int selectedPartySize = partySize != null ? partySize : 2;

	    ReservationForm reservationForm = new ReservationForm();
	    reservationForm.setReservationDate(selectedDate);
	    reservationForm.setPartySize(selectedPartySize);

	    if (principal != null) {
	        UserResponse currentUser = userService.findByUsername(principal.getName());
	        if (currentUser != null) {
	            reservationForm.setGuestName(currentUser.getUsername());
	            reservationForm.setGuestEmail(currentUser.getEmail());
	        }
	    }

	    model.addAttribute("reservationForm", reservationForm);
	    model.addAttribute("availableSlots", reservationService.getAvailability(selectedDate, selectedPartySize));
	    model.addAttribute("maxPartySize", reservationService.getMaxPartySize());

	    return "contact";
	}
	
	@GetMapping("/faq")
    public String faqPage(Model model) {
        model.addAttribute("restaurantName", "Digital Dining");
        return "faq";
    }
	
	private LocalDate parseReservationDate(String raw) {
	    if (raw == null || raw.isBlank()) {
	        return LocalDate.now();
	    }

	    String value = raw.trim();

	    List<DateTimeFormatter> formats = List.of(
	            DateTimeFormatter.ISO_LOCAL_DATE,
	            DateTimeFormatter.ofPattern("yyyy. M. d."),
	            DateTimeFormatter.ofPattern("yyyy. MM. dd."),
	            DateTimeFormatter.ofPattern("yyyy. M. d"),
	            DateTimeFormatter.ofPattern("yyyy. MM. dd")
	    );

	    for (DateTimeFormatter formatter : formats) {
	        try {
	            return LocalDate.parse(value, formatter);
	        } catch (DateTimeParseException ignored) {
	        }
	    }

	    return LocalDate.now();
	}
}
