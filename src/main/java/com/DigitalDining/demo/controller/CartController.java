package com.DigitalDining.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.DigitalDining.demo.service.CartService;
import com.DigitalDining.demo.service.MenuItemService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final MenuItemService menuItemService;

    public CartController(CartService cartService, MenuItemService menuItemService) {
        this.cartService = cartService;
        this.menuItemService = menuItemService;
    }

    @GetMapping
    public String showCart(Model model, HttpSession session) {
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("total", cartService.getTotal(session));
        model.addAttribute("count", cartService.getCount(session));
        return "cart/cart";
    }

    @PostMapping("/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        cartService.addItem(session, menuItemService.getMenuItemById(id));

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/menu");
    }
    
    @PostMapping("/add-line/{lineId}")
    public String addByLineId(@PathVariable String lineId, HttpSession session, HttpServletRequest request) {
        cartService.increaseOne(session, lineId);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/cart");
    }

    @PostMapping("/remove/{lineId}")
    public String removeOne(@PathVariable String lineId, HttpSession session) {
        cartService.removeOne(session, lineId);
        return "redirect:/cart";
    }

    @PostMapping("/remove-all/{lineId}")
    public String removeAll(@PathVariable String lineId, HttpSession session) {
        cartService.removeAll(session, lineId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session) {
        cartService.clear(session);
        return "redirect:/cart";
    }
}