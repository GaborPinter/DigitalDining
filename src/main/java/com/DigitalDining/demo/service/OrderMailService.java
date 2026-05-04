package com.DigitalDining.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.DigitalDining.demo.model.OrderEntity;
import com.DigitalDining.demo.model.OrderItemEntity;

import org.springframework.beans.factory.annotation.Value;

@Service
public class OrderMailService {

	private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@restaurant.local}")
    private String from;

    public OrderMailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendCustomerConfirmation(OrderEntity order) {
        if (order.getEmail() == null || order.getEmail().isBlank()) {
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(order.getEmail());
        message.setSubject("Rendelés visszaigazolás - " + order.getOrderNumber());
        message.setText(buildMessage(order));

        mailSender.send(message);
    }

    private String buildMessage(OrderEntity order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Köszönjük a rendelésed!\n\n");
        sb.append("Rendelésszám: ").append(order.getOrderNumber()).append("\n");
        sb.append("Név: ").append(order.getFullName()).append("\n");
        sb.append("Telefonszám: ").append(order.getPhone()).append("\n");
        sb.append("Átvétel: ").append(order.getDeliveryType().getDisplayName()).append("\n");
        if (order.getAddress() != null) {
            sb.append("Cím: ").append(order.getAddress()).append("\n");
        }
        if (order.getDesiredTime() != null) {
            sb.append("Kért időpont: ").append(order.getDesiredTime()).append("\n");
        }
        sb.append("Fizetés: ").append(order.getPaymentMethod().getDisplayName()).append("\n");
        sb.append("Összeg: ").append(order.getTotalAmount()).append(" Ft\n\n");
        sb.append("Tételek:\n");

        for (OrderItemEntity item : order.getItems()) {
            sb.append("- ").append(item.getItemName())
              .append(" x").append(item.getQuantity())
              .append(" = ").append(item.getLineTotal()).append(" Ft\n");

            if (item.getItemDescription() != null && !item.getItemDescription().isBlank()) {
                sb.append("  ").append(stripHtml(item.getItemDescription())).append("\n");
            }
        }

        sb.append("\nMegjegyzés: ").append(order.getNotes() != null ? order.getNotes() : "-");
        return sb.toString();
    }

    private String stripHtml(String input) {
        return input
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("<[^>]+>", "")
                .trim();
    }
}
