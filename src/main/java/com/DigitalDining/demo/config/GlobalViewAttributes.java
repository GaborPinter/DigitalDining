package com.DigitalDining.demo.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.DigitalDining.demo.dto.UserResponse;
import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.service.UserService;

@ControllerAdvice
public class GlobalViewAttributes {

    private final UserService userService;

    public GlobalViewAttributes(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public UserResponse currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userService.findByUsername(principal.getName());
    }

    @ModelAttribute("isLoggedIn")
    public boolean isLoggedIn(Principal principal) {
        return principal != null;
    }

    @ModelAttribute("restaurantName")
    public String restaurantName() {
        return "Digital Dining";
    }
    
    @ModelAttribute("weeklyMenuSubscribed")
    public boolean weeklyMenuSubscribed(Principal principal) {
        if (principal == null) {
            return false;
        }

        User user = userService.findEntityByUsername(principal.getName());
        return user != null && user.isWeeklyMenuSubscribed();
    }
}
