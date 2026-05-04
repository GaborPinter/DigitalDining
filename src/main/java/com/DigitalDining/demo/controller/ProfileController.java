package com.DigitalDining.demo.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.service.OrderService;
import com.DigitalDining.demo.service.UserService;

@Controller
public class ProfileController {
	
	private final OrderService orderService;
    private final UserService userService;

    public ProfileController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {

        if (principal != null) {
            User user = userService.findEntityByUsername(principal.getName());
            model.addAttribute("orders", orderService.getOrdersForUser(user));
        }

        return "profile";
    }
}
