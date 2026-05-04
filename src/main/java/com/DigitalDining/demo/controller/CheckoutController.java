package com.DigitalDining.demo.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.DigitalDining.demo.dto.CheckoutForm;
import com.DigitalDining.demo.model.DeliveryType;
import com.DigitalDining.demo.model.OrderEntity;
import com.DigitalDining.demo.model.PaymentMethod;
import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.service.CartService;
import com.DigitalDining.demo.service.OrderService;
import com.DigitalDining.demo.service.PaypalService;
import com.DigitalDining.demo.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+36|06)\\d{9}$");
    private static final Pattern HUNGARIAN_ADDRESS_PATTERN = Pattern.compile("^\\d{4}\\s+.+$");

    private final OrderService orderService;
    private final CartService cartService;
    private final PaypalService paypalService;
    private final Validator validator;
    private final UserService userService;

    @Value("${paypal.client-id}")
    private String paypalClientId;

    @Value("${paypal.currency:HUF}")
    private String paypalCurrency;

    public CheckoutController(OrderService orderService,
                              CartService cartService,
                              PaypalService paypalService,
                              Validator validator,
                              UserService userService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paypalService = paypalService;
        this.validator = validator;
        this.userService = userService;
    }

    @GetMapping
    public String checkoutPage(HttpSession session, Model model) {
        if (cartService.getCount(session) == 0) {
            return "redirect:/cart";
        }

        CheckoutForm form = new CheckoutForm();
        form.setDeliveryType(DeliveryType.TAKEAWAY);
        form.setPaymentMethod(PaymentMethod.CASH);

        model.addAttribute("checkoutForm", form);
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("total", cartService.getTotal(session));
        model.addAttribute("count", cartService.getCount(session));
        model.addAttribute("paypalClientId", paypalClientId);
        model.addAttribute("paypalCurrency", paypalCurrency);
        return "cart/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@jakarta.validation.Valid @ModelAttribute("checkoutForm") CheckoutForm form,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             Principal principal) {

        validateDeliveryFields(form, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("items", cartService.getItems(session));
            model.addAttribute("total", cartService.getTotal(session));
            model.addAttribute("count", cartService.getCount(session));
            model.addAttribute("paypalClientId", paypalClientId);
            model.addAttribute("paypalCurrency", paypalCurrency);
            return "cart/checkout";
        }

        if (principal == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findEntityByUsername(principal.getName());

        OrderEntity order = orderService.placeCashOrCardOrder(form, session, user);
        return "redirect:/checkout/success/" + order.getOrderNumber();
    }

    @GetMapping("/success/{orderNumber}")
    public String success(@PathVariable String orderNumber, Model model) {
        model.addAttribute("order", orderService.getOrderByNumber(orderNumber));
        return "cart/success";
    }

    @ResponseBody
    @PostMapping("/paypal/create-order")
    public ResponseEntity<Map<String, String>> createPaypalOrder(@RequestBody CheckoutForm form, HttpSession session) {
        validateCheckoutForm(form);

        if (form.getPaymentMethod() != PaymentMethod.PAYPAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A PayPal fizetéshez a paymentMethod mezőnek PAYPAL-nak kell lennie");
        }

        if (cartService.getCount(session) == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A kosár üres");
        }

        orderService.savePendingCheckoutForm(session, form);

        BigDecimal total = cartService.getTotal(session);
        String paypalOrderId = paypalService.createOrder(total);

        return ResponseEntity.ok(Map.of("orderID", paypalOrderId));
    }

    @ResponseBody
    @PostMapping("/paypal/capture-order")
    public ResponseEntity<Map<String, String>> capturePaypalOrder(@RequestBody Map<String, String> payload,
                                                                  HttpSession session,
                                                                  Principal principal) {
        String orderId = payload.get("orderID");
        if (orderId == null || orderId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hiányzó PayPal order ID");
        }

        CheckoutForm form = orderService.getPendingCheckoutForm(session);
        if (form == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nincs mentett checkout űrlap");
        }

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nincs bejelentkezett felhasználó");
        }

        User user = userService.findEntityByUsername(principal.getName());

        PaypalService.PaypalCaptureResult captureResult = paypalService.captureOrder(orderId);
        if (!"COMPLETED".equalsIgnoreCase(captureResult.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A PayPal fizetés nem teljesült");
        }

        OrderEntity order = orderService.placePaypalOrder(form, orderId, session, user);

        Map<String, String> response = new HashMap<>();
        response.put("redirectUrl", "/checkout/success/" + order.getOrderNumber());
        return ResponseEntity.ok(response);
    }

    private void validateCheckoutForm(CheckoutForm form) {
        Set<ConstraintViolation<CheckoutForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.iterator().next().getMessage());
        }

        if (form.getPhone() == null || form.getPhone().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A telefonszám kötelező");
        }

        String normalized = form.getPhone().replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Érvénytelen telefonszám");
        }

        validateDeliveryRequirement(form);

        if (form.getDeliveryType() == DeliveryType.DELIVERY) {
            if (form.getAddress() == null || form.getAddress().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Házhozszállításnál a cím kötelező");
            }
            if (!HUNGARIAN_ADDRESS_PATTERN.matcher(form.getAddress().trim()).matches()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A cím formátuma érvénytelen");
            }
        }
    }

    private void validateDeliveryFields(CheckoutForm form, BindingResult bindingResult) {
        validatePhone(form, bindingResult);
        validateDeliveryRequirement(form);

        if (form.getDeliveryType() == DeliveryType.DELIVERY) {
            if (form.getAddress() == null || form.getAddress().isBlank()) {
                bindingResult.rejectValue("address", "address.required", "Házhozszállításnál a cím kötelező");
            } else if (!HUNGARIAN_ADDRESS_PATTERN.matcher(form.getAddress().trim()).matches()) {
                bindingResult.rejectValue("address", "address.invalid", "A cím formátuma érvénytelen");
            }
        }
    }

    private void validatePhone(CheckoutForm form, BindingResult bindingResult) {
        String phone = form.getPhone();
        if (phone == null || phone.isBlank()) {
            bindingResult.rejectValue("phone", "phone.required", "A telefonszám kötelező");
            return;
        }

        String normalized = phone.replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            bindingResult.rejectValue("phone", "phone.invalid", "Érvénytelen telefonszám");
        }
    }

    private void validateDeliveryRequirement(CheckoutForm form) {
        if (form.getDeliveryType() == DeliveryType.DELIVERY
                && (form.getAddress() == null || form.getAddress().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Házhozszállításnál a cím kötelező");
        }
    }
}
