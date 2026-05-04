package com.DigitalDining.demo.dto;

import com.DigitalDining.demo.model.DeliveryType;
import com.DigitalDining.demo.model.PaymentMethod;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CheckoutForm {

	@NotBlank(message = "A név kötelező")
    private String fullName;

    @NotBlank(message = "A telefonszám kötelező")
    private String phone;

    @Email(message = "Érvénytelen email")
    private String email;

    @NotNull(message = "Válassz átvételi módot")
    private DeliveryType deliveryType;

    private String address;
    private String notes;
    
    private String desiredTime;

    @NotNull(message = "Válassz fizetési módot")
    private PaymentMethod paymentMethod;

	public CheckoutForm(@NotBlank(message = "A név kötelező") String fullName,
			@NotBlank(message = "A telefonszám kötelező") String phone,
			@Email(message = "Érvénytelen email") String email,
			@NotNull(message = "Válassz átvételi módot") DeliveryType deliveryType, String address, String notes,
			String desiredTime, @NotNull(message = "Válassz fizetési módot") PaymentMethod paymentMethod) {
		this.fullName = fullName;
		this.phone = phone;
		this.email = email;
		this.deliveryType = deliveryType;
		this.address = address;
		this.notes = notes;
		this.desiredTime = desiredTime;
		this.paymentMethod = paymentMethod;
	}
	
	public CheckoutForm() {
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public DeliveryType getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(DeliveryType deliveryType) {
		this.deliveryType = deliveryType;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getDesiredTime() {
		return desiredTime;
	}

	public void setDesiredTime(String desiredTime) {
		this.desiredTime = desiredTime;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
