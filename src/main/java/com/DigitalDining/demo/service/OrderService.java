package com.DigitalDining.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.DigitalDining.demo.dto.CheckoutForm;
import com.DigitalDining.demo.model.CartLine;
import com.DigitalDining.demo.model.DeliveryType;
import com.DigitalDining.demo.model.OrderEntity;
import com.DigitalDining.demo.model.OrderItemEntity;
import com.DigitalDining.demo.model.OrderStatus;
import com.DigitalDining.demo.model.PaymentMethod;
import com.DigitalDining.demo.model.User;
import com.DigitalDining.demo.repository.OrderRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

	private static final String PENDING_CHECKOUT_KEY = "PENDING_CHECKOUT_FORM";

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final OrderMailService orderMailService;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        OrderMailService orderMailService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.orderMailService = orderMailService;
    }

    public BigDecimal getCartTotal(HttpSession session) {
        return cartService.getTotal(session);
    }

    public List<CartLine> getCartItems(HttpSession session) {
        return cartService.getItems(session);
    }

    @Transactional
    public OrderEntity placeCashOrCardOrder(CheckoutForm form, HttpSession session, User user) {
        if (cartService.getCount(session) == 0) {
            throw new IllegalStateException("A kosár üres");
        }

        OrderEntity order = buildOrder(form, cartService.getItems(session), null, user);
        order.setPaymentMethod(form.getPaymentMethod());
        order.setStatus(OrderStatus.PENDING);

        OrderEntity saved = orderRepository.save(order);

        orderMailService.sendCustomerConfirmation(saved);
        cartService.clear(session);
        session.removeAttribute(PENDING_CHECKOUT_KEY);

        return saved;
    }

    @Transactional
    public OrderEntity placePaypalOrder(CheckoutForm form, String paypalOrderId, HttpSession session, User user) {
        if (cartService.getCount(session) == 0) {
            throw new IllegalStateException("A kosár üres");
        }

        OrderEntity order = buildOrder(form, cartService.getItems(session), paypalOrderId, user);
        order.setPaymentMethod(PaymentMethod.PAYPAL);
        order.setStatus(OrderStatus.PAID);

        OrderEntity saved = orderRepository.save(order);

        orderMailService.sendCustomerConfirmation(saved);
        cartService.clear(session);
        session.removeAttribute(PENDING_CHECKOUT_KEY);

        return saved;
    }

    public OrderEntity getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("A rendelés nem található"));
    }
    
    public List<OrderEntity> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void savePendingCheckoutForm(HttpSession session, CheckoutForm form) {
        session.setAttribute(PENDING_CHECKOUT_KEY, form);
    }

    public CheckoutForm getPendingCheckoutForm(HttpSession session) {
        Object form = session.getAttribute(PENDING_CHECKOUT_KEY);
        if (form instanceof CheckoutForm checkoutForm) {
            return checkoutForm;
        }
        return null;
    }

    private OrderEntity buildOrder(CheckoutForm form, List<CartLine> cartItems, String paypalOrderId, User user) {
        OrderEntity order = new OrderEntity();
        order.setOrderNumber(generateOrderNumber());
        order.setFullName(form.getFullName());
        order.setPhone(form.getPhone());
        order.setEmail(form.getEmail());
        order.setDeliveryType(form.getDeliveryType());
        order.setAddress(form.getDeliveryType() == DeliveryType.DELIVERY ? form.getAddress() : null);
        order.setNotes(form.getNotes());
        order.setDesiredTime(form.getDesiredTime());
        order.setPaypalOrderId(paypalOrderId);
        order.setUser(user);

        BigDecimal total = BigDecimal.ZERO;

        for (CartLine cartLine : cartItems) {
            OrderItemEntity item = new OrderItemEntity();
            item.setMenuItemId(cartLine.getMenuItemId());
            item.setItemName(cartLine.getName());
            item.setImagePath(cartLine.getImagePath());
            item.setUnitPrice(cartLine.getUnitPrice());
            item.setQuantity(cartLine.getQuantity());

            BigDecimal lineTotal = cartLine.getUnitPrice()
                    .multiply(BigDecimal.valueOf(cartLine.getQuantity()));
            item.setLineTotal(lineTotal);
            item.setItemDescription(cartLine.getDescription());

            order.addItem(item);
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        return order;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
