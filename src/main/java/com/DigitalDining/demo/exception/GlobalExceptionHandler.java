package com.DigitalDining.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(ResourceAlreadyExistsException.class)
	public ResponseEntity<Map<String, String>> handleExists(ResourceAlreadyExistsException ex) {
		Map<String, String> err = Map.of("error", ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleAny(Exception ex) {
		Map<String, String> err = Map.of("error", "Váratlan hiba: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
	}
}
