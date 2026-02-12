package dev.kreaker.cnc.web.controller;

import dev.kreaker.cnc.security.dto.RegisterDTO;
import dev.kreaker.cnc.security.model.CncUser;
import dev.kreaker.cnc.security.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@GetMapping("/login")
	public String loginPage() {
		return "auth/login";
	}

	@GetMapping("/register")
	public String registerPage(Model model) {
		model.addAttribute("registerDTO", new RegisterDTO());
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(@Valid @ModelAttribute RegisterDTO registerDTO,
						   BindingResult bindingResult,
						   RedirectAttributes redirectAttributes) {
		if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
			bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "Passwords do not match");
		}

		if (userRepository.existsByUsername(registerDTO.getUsername())) {
			bindingResult.rejectValue("username", "error.username", "Username is already taken");
		}

		if (userRepository.existsByEmail(registerDTO.getEmail())) {
			bindingResult.rejectValue("email", "error.email", "Email is already registered");
		}

		if (bindingResult.hasErrors()) {
			return "auth/register";
		}

		var user = CncUser.builder()
				.username(registerDTO.getUsername())
				.email(registerDTO.getEmail())
				.password(passwordEncoder.encode(registerDTO.getPassword()))
				.displayName(registerDTO.getDisplayName())
				.enabled(true)
				.build();

		userRepository.save(user);

		redirectAttributes.addFlashAttribute("success",
				"User '" + user.getUsername() + "' registered successfully.");
		return "redirect:/catalogs";
	}
}
