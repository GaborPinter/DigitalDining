package com.DigitalDining.demo.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.DigitalDining.demo.service.CartService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributes {

	private final CartService cartService;

    public GlobalModelAttributes(CartService cartService) {
        this.cartService = cartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        return cartService.getCount(session);
    }
}
