package com.DigitalDining.demo.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.DigitalDining.demo.dto.WeeklyMenuSelectionForm;
import com.DigitalDining.demo.model.WeeklyMenuDayView;
import com.DigitalDining.demo.model.WeeklyMenuOrderSummary;
import com.DigitalDining.demo.service.CartService;
import com.DigitalDining.demo.service.WeeklyMenuPdfService;
import com.DigitalDining.demo.service.WeeklyMenuService;
import com.DigitalDining.demo.service.impl.UserServiceImpl;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/weekly-menu")
public class WeeklyMenuController {

	private final WeeklyMenuService weeklyMenuService;
    private final WeeklyMenuPdfService weeklyMenuPdfService;
    private final CartService cartService;
    private final UserServiceImpl userServiceImpl;

    public WeeklyMenuController(WeeklyMenuService weeklyMenuService,
                                WeeklyMenuPdfService weeklyMenuPdfService,
                                CartService cartService,
                                UserServiceImpl userServiceImpl) {
        this.weeklyMenuService = weeklyMenuService;
        this.weeklyMenuPdfService = weeklyMenuPdfService;
        this.cartService = cartService;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping({"", "/"})
    public String weeklyMenu(Model model) {
        List<WeeklyMenuDayView> days = weeklyMenuService.getNextSevenDays();
        model.addAttribute("days", days);
        return "weekly-menu/list";
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> downloadPdf() {
        List<WeeklyMenuDayView> days = weeklyMenuService.getNextSevenDays();
        byte[] pdf = weeklyMenuPdfService.generatePdf(days);

        String fileName = "heti-menu.pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
    @PostMapping("/subscribe")
    public String subscribeToWeeklyMenu(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            userServiceImpl.subscribeToWeeklyMenu(principal.getName());
            weeklyMenuPdfService.sendWeeklyMenuPdfByEmail(principal.getName());
            redirectAttributes.addFlashAttribute("weeklyMenuMessage", "Sikeresen feliratkoztál a hírlevélre, ezért az aktuális heti menüt elküldtük e-mailben.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("weeklyMenuError", "Sikertelen feliratkozás: " + ex.getMessage());
        }

        return "redirect:/weekly-menu";
    }
    
    @PostMapping("/unsubscribe")
    public String unsubscribeFromWeeklyMenu(Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
        	userServiceImpl.unsubscribeFromWeeklyMenu(principal.getName());
            redirectAttributes.addFlashAttribute("weeklyMenuMessage",
                    "Sikeresen leiratkoztál a heti menü hírlevélről.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("weeklyMenuError", ex.getMessage());
        }

        return "redirect:/weekly-menu";
    }

    @PostMapping("/order/{date}")
    public String orderDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @ModelAttribute WeeklyMenuSelectionForm form,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (!form.hasAnySelection()) {
                redirectAttributes.addFlashAttribute("weeklyMenuError", "Válassz legalább egy opciót.");
                return "redirect:/weekly-menu";
            }

            WeeklyMenuOrderSummary summary = weeklyMenuService.buildOrderSummary(
                    date,
                    form.getSoupOption(),
                    form.getMainOption(),
                    form.getDessertOption()
            );

            cartService.addWeeklyMenuOrder(session, summary);
            redirectAttributes.addFlashAttribute("weeklyMenuMessage", "A kiválasztott napi menü bekerült a kosárba.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("weeklyMenuError", ex.getMessage());
        }

        return "redirect:/weekly-menu";
    }
}
