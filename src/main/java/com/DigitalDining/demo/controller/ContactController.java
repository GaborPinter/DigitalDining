package com.DigitalDining.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.DigitalDining.demo.dto.ContactForm;
import com.DigitalDining.demo.service.ContactEmailService;

import jakarta.validation.Valid;

@Controller
public class ContactController {

	private final ContactEmailService emailService;

	public ContactController(ContactEmailService emailService) {
		this.emailService = emailService;
	}

	@PostMapping("/contact/send")
	public String handleContact(@Valid @ModelAttribute ContactForm contactForm, BindingResult bindingResult,
			RedirectAttributes redirectAttributes) throws Exception {
		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("contactError", "Kérlek töltsd ki megfelelően az űrlapot.");
			return "redirect:/contact";
		}

		try {
			emailService.sendContact(contactForm);
			redirectAttributes.addFlashAttribute("contactSuccess", "Köszönjük! Üzeneted elküldtük.");
		} catch (Exception ex) {
			redirectAttributes.addFlashAttribute("contactError",
					"Hiba történt az üzenet küldése közben. Kérjük próbáld később.");
			ex.printStackTrace();
		    throw ex;
		}

		return "redirect:/contact";
	}
}
