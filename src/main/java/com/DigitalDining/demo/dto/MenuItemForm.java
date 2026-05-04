package com.DigitalDining.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemForm {

	@NotBlank(message = "A név kötelező")
    private String name;

    private String description;

    private String ingredients;

    private String allergens;

    @NotNull(message = "Az ár kötelező")
    @DecimalMin(value = "0.0", inclusive = true, message = "Az ár nem lehet negatív")
    private BigDecimal price;

    @NotNull(message = "A kalória megadása kötelező")
    @Min(value = 0, message = "A kalória nem lehet negatív")
    private Integer calories;

    private Integer prepTimeMinutes;

    private boolean vegetarian;
    private boolean vegan;
    private boolean spicy;
    private boolean available = true;

    @NotNull(message = "Kategória kiválasztása kötelező")
    private Long categoryId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	public String getAllergens() {
		return allergens;
	}

	public void setAllergens(String allergens) {
		this.allergens = allergens;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Integer getCalories() {
		return calories;
	}

	public void setCalories(Integer calories) {
		this.calories = calories;
	}

	public Integer getPrepTimeMinutes() {
		return prepTimeMinutes;
	}

	public void setPrepTimeMinutes(Integer prepTimeMinutes) {
		this.prepTimeMinutes = prepTimeMinutes;
	}

	public boolean isVegetarian() {
		return vegetarian;
	}

	public void setVegetarian(boolean vegetarian) {
		this.vegetarian = vegetarian;
	}

	public boolean isVegan() {
		return vegan;
	}

	public void setVegan(boolean vegan) {
		this.vegan = vegan;
	}

	public boolean isSpicy() {
		return spicy;
	}

	public void setSpicy(boolean spicy) {
		this.spicy = spicy;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
}