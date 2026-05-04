package com.DigitalDining.demo.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.DigitalDining.demo.dto.ContactForm;
import com.DigitalDining.demo.model.ContactMessage;
import com.DigitalDining.demo.repository.ContactMessageRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class ContactEmailService {

    private final JavaMailSender mailSender;
    private final ContactMessageRepository repository;

    @Value("${contact.to.email:pinterg111@gmail.com}")
    private String toEmail;

    @Value("${contact.from.fallback:pinterg111@gmail.com}")
    private String fallbackFrom;

    @Autowired
    public ContactEmailService(JavaMailSender mailSender, ContactMessageRepository repository) {
        this.mailSender = mailSender;
        this.repository = repository;
    }

    public void sendContact(ContactForm form) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, false, StandardCharsets.UTF_8.name());

        helper.setTo(toEmail);

        String usedFrom = fallbackFrom;
        if (isValidEmail(form.getEmail())) {
            usedFrom = form.getEmail();
        }

        try {
            helper.setFrom(usedFrom);
        } catch (Exception ex) {
            // ha a SMTP vagy JavaMail nem engedi a From beállítást, fallback marad
            helper.setFrom(fallbackFrom);
            usedFrom = fallbackFrom;
        }

        // Reply-To mindig a felhasználó e-mail címe legyen (így a válasz hozzá megy)
        helper.setReplyTo(form.getEmail());

        // Tárgy: "Név - Tárgy"
        String composedSubject = form.getName() + " - " + form.getSubject();
        helper.setSubject(composedSubject);

        // Törzs: csak a felhasználó által megadott üzenet tartalma (nincs e-mail cím, nincs "Üzenet:" label)
        helper.setText(form.getMessage());

        // küldés
        mailSender.send(msg);

        // mentés az adatbázisba (kereshetőség miatt)
        ContactMessage cm = new ContactMessage();
        cm.setName(form.getName());
        cm.setFromEmail(usedFrom);
        cm.setToEmail(toEmail);
        cm.setSubject(composedSubject);
        cm.setMessage(form.getMessage());
        cm.setSentAt(LocalDateTime.now(ZoneId.of("Europe/Budapest")));

        repository.save(cm);
    }

    private boolean isValidEmail(String email) {
        if (email == null)
            return false;
        try {
            InternetAddress ia = new InternetAddress(email);
            ia.validate();
            return true;
        } catch (AddressException ex) {
            return false;
        }
    }
}