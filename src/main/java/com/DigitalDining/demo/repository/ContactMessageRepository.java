package com.DigitalDining.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DigitalDining.demo.model.ContactMessage;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

	List<ContactMessage> findByFromEmailContainingIgnoreCase(String fromEmailPart);

	List<ContactMessage> findBySubjectContainingIgnoreCase(String subjectPart);

	List<ContactMessage> findByNameContainingIgnoreCase(String namePart);
}
