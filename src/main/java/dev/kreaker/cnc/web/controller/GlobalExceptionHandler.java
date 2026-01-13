package dev.kreaker.cnc.web.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(EntityNotFoundException.class)
	public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
		log.error("Entity not found", ex);
		model.addAttribute("error", ex.getMessage());
		model.addAttribute("status", 404);
		return "error/error";
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
		log.error("Illegal argument", ex);
		model.addAttribute("error", ex.getMessage());
		model.addAttribute("status", 400);
		return "error/error";
	}

	@ExceptionHandler(IllegalStateException.class)
	public String handleIllegalState(IllegalStateException ex, Model model) {
		log.error("Illegal state", ex);
		model.addAttribute("error", ex.getMessage());
		model.addAttribute("status", 400);
		return "error/error";
	}

	@ExceptionHandler(Exception.class)
	public String handleGeneralException(Exception ex, Model model) {
		log.error("Unexpected error", ex);
		model.addAttribute("error", "An unexpected error occurred");
		model.addAttribute("status", 500);
		return "error/error";
	}
}
