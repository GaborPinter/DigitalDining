package com.DigitalDining.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.DigitalDining.demo.model.CartLine;
import com.DigitalDining.demo.model.MenuItem;
import com.DigitalDining.demo.model.WeeklyMenuOrderSummary;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class CartService {

    private static final String CART_KEY = "CART";

    @Transactional
    public void addItem(HttpSession session, MenuItem item) {
        Map<String, CartLine> cart = getCartMap(session);
        String key = buildKey(item.getName(), item.getImagePath(), item.getPrice());

        CartLine existing = cart.get(key);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cart.put(key, new CartLine(
                    item.getId(),
                    item.getName(),
                    item.getImagePath(),
                    item.getDescription(),
                    item.getCategory() != null ? item.getCategory().getName() : null,
                    item.getPrice(),
                    1
            ));
        }

        session.setAttribute(CART_KEY, cart);
    }
    
    @Transactional
    public void addWeeklyMenuOrder(HttpSession session, WeeklyMenuOrderSummary summary) {
        Map<String, CartLine> cart = getCartMap(session);

        String key = "weekly|" + summary.date().toString() + "|" + summary.cartLabel() + "|" + summary.totalPrice().toPlainString();

        CartLine existing = cart.get(key);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cart.put(key, new CartLine(
                    null,
                    summary.cartLabel(),
                    null,
                    summary.descriptionHtml(),
                    "Heti menü",
                    summary.totalPrice(),
                    1
            ));
        }

        session.setAttribute(CART_KEY, cart);
    }

    public void removeOne(HttpSession session, String lineId) {
        Map<String, CartLine> cart = getCartMap(session);
        String key = findKeyByLineId(cart, lineId);
        if (key == null) {
            return;
        }

        CartLine line = cart.get(key);
        if (line.getQuantity() <= 1) {
            cart.remove(key);
        } else {
            line.setQuantity(line.getQuantity() - 1);
        }

        session.setAttribute(CART_KEY, cart);
    }

    public void removeAll(HttpSession session, String lineId) {
        Map<String, CartLine> cart = getCartMap(session);
        String key = findKeyByLineId(cart, lineId);
        if (key != null) {
            cart.remove(key);
            session.setAttribute(CART_KEY, cart);
        }
    }

    public void clear(HttpSession session) {
        session.removeAttribute(CART_KEY);
    }

    public List<CartLine> getItems(HttpSession session) {
        return new ArrayList<>(getCartMap(session).values());
    }

    public int getCount(HttpSession session) {
        return getCartMap(session).values().stream()
                .mapToInt(CartLine::getQuantity)
                .sum();
    }
    
    public void increaseOne(HttpSession session, String lineId) {
        Map<String, CartLine> cart = getCartMap(session);
        String key = findKeyByLineId(cart, lineId);
        if (key == null) {
            return;
        }

        CartLine line = cart.get(key);
        line.setQuantity(line.getQuantity() + 1);
        session.setAttribute(CART_KEY, cart);
    }

    public BigDecimal getTotal(HttpSession session) {
        return getCartMap(session).values().stream()
                .map(CartLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @SuppressWarnings("unchecked")
    private Map<String, CartLine> getCartMap(HttpSession session) {
        Object cart = session.getAttribute(CART_KEY);

        if (cart instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                return (Map<String, CartLine>) map;
            }

            Object firstKey = map.keySet().iterator().next();
            Object firstValue = map.values().iterator().next();

            if (firstKey instanceof String && firstValue instanceof CartLine) {
                return (Map<String, CartLine>) map;
            }

            // régi session adat migrálása
            Map<String, CartLine> migrated = new LinkedHashMap<>();
            for (Object value : map.values()) {
                if (value instanceof CartLine line) {
                    migrated.put(buildKey(line.getName(), line.getImagePath(), line.getUnitPrice()), line);
                }
            }
            session.setAttribute(CART_KEY, migrated);
            return migrated;
        }

        if (cart instanceof List<?> list) {
            Map<String, CartLine> migrated = new LinkedHashMap<>();
            for (Object value : list) {
                if (value instanceof CartLine line) {
                    migrated.put(buildKey(line.getName(), line.getImagePath(), line.getUnitPrice()), line);
                }
            }
            session.setAttribute(CART_KEY, migrated);
            return migrated;
        }

        Map<String, CartLine> newCart = new LinkedHashMap<>();
        session.setAttribute(CART_KEY, newCart);
        return newCart;
    }

    private String findKeyByLineId(Map<String, CartLine> cart, String lineId) {
        for (Map.Entry<String, CartLine> entry : cart.entrySet()) {
            if (lineId.equals(entry.getValue().getLineId())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String buildKey(String name, String imagePath, BigDecimal price) {
        return normalize(name) + "|" + normalize(imagePath) + "|" + normalizePrice(price);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizePrice(BigDecimal price) {
        return price == null ? "0" : price.stripTrailingZeros().toPlainString();
    }
}