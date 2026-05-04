package com.DigitalDining.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.DigitalDining.demo.dto.ForgotPasswordRequest;
import com.DigitalDining.demo.dto.LoginRequest;
import com.DigitalDining.demo.dto.RegisterRequest;
import com.DigitalDining.demo.exception.ResourceAlreadyExistsException;
import com.DigitalDining.demo.model.ResetPasswordRequest;
import com.DigitalDining.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest dto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(dto);
        } catch (ResourceAlreadyExistsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }

        redirectAttrs.addFlashAttribute("message", "Sikeres regisztráció! Jelentkezz be.");
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }
    
    @GetMapping("/forgot-password")
    public String showForgotPassword(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String verifyForgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest dto,
                                       BindingResult bindingResult,
                                       Model model,
                                       HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        boolean exists = userService.existsByUsernameAndEmail(dto.getUsername(), dto.getEmail());
        if (!exists) {
            model.addAttribute("errorMessage", "A megadott felhasználónév és email cím páros nem található.");
            return "forgot-password";
        }

        session.setAttribute("passwordResetUsername", dto.getUsername());
        session.setAttribute("passwordResetEmail", dto.getEmail());

        return "redirect:/auth/reset-password";
    }

    @GetMapping("/reset-password")
    public String showResetPassword(Model model, HttpSession session) {
        String username = (String) session.getAttribute("passwordResetUsername");
        String email = (String) session.getAttribute("passwordResetEmail");

        if (username == null || email == null) {
            return "redirect:/auth/forgot-password";
        }

        model.addAttribute("resetPasswordRequest", new ResetPasswordRequest());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest dto,
                                BindingResult bindingResult,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttrs) {
        String username = (String) session.getAttribute("passwordResetUsername");
        String email = (String) session.getAttribute("passwordResetEmail");

        if (username == null || email == null) {
            return "redirect:/auth/forgot-password";
        }

        if (bindingResult.hasErrors()) {
            return "reset-password";
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            model.addAttribute("errorMessage", "A két jelszó nem egyezik.");
            return "reset-password";
        }

        userService.resetPassword(username, email, dto.getNewPassword());

        session.removeAttribute("passwordResetUsername");
        session.removeAttribute("passwordResetEmail");

        redirectAttrs.addFlashAttribute("message", "Sikeres jelszóváltoztatás! Most már az új jelszavaddal tudsz bejelentkezni.");
        return "redirect:/auth/login";
    }

//    @PostMapping("/login")
//    public String loginSubmit(@Valid @ModelAttribute("loginRequest") LoginRequest dto,
//                              BindingResult bindingResult,
//                              RedirectAttributes redirectAttrs,
//                              HttpSession session,
//                              HttpServletRequest request,
//                              HttpServletResponse response) {
//        if (bindingResult.hasErrors()) {
//            return "login";
//        }
//
//        boolean ok = userService.authenticate(dto.getUsername(), dto.getPassword());
//        if (!ok) {
//            redirectAttrs.addFlashAttribute("message", "Hibás felhasználónév vagy jelszó.");
//            return "redirect:/auth/login";
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUsername());
//
//        Authentication authentication =
//                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//
//        SecurityContext context = SecurityContextHolder.createEmptyContext();
//        context.setAuthentication(authentication);
//        SecurityContextHolder.setContext(context);
//
//        new HttpSessionSecurityContextRepository().saveContext(context, request, response);
//
//        session.setAttribute("currentUser", userService.findByUsername(dto.getUsername()));
//
//        return "redirect:/";
//    }

//    @PostMapping("/do-logout")
//    public String doLogout(HttpServletRequest request, HttpServletResponse response) {
//        HttpSession session = request.getSession(false);
//        if (session != null) {
//            session.removeAttribute("currentUser");
//            session.invalidate();
//        }
//
//        SecurityContextHolder.clearContext();
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null) {
//            new SecurityContextLogoutHandler().logout(request, response, auth);
//        }
//
//        return "redirect:/auth/login?logout";
//    }
}