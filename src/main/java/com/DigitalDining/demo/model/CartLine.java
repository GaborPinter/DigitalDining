package com.DigitalDining.demo.model;

import java.math.BigDecimal;
import java.util.UUID;

public class CartLine {
	private String lineId;
	private Long menuItemId;
    private String name;
    private String imagePath;
    private String description;
    private String categoryName;
    private BigDecimal unitPrice;
    private int quantity;
    
    public CartLine() {
        this.lineId = UUID.randomUUID().toString();
    }
    
	public CartLine(Long menuItemId, String name, String imagePath, String description, String categoryName,
			BigDecimal unitPrice, int quantity) {
		this.lineId = UUID.randomUUID().toString();
		this.menuItemId = menuItemId;
		this.name = name;
		this.imagePath = imagePath;
		this.description = description;
		this.categoryName = categoryName;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
	}
	
	public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

	public Long getMenuItemId() {
		return menuItemId;
	}

	public void setMenuItemId(Long menuItemId) {
		this.menuItemId = menuItemId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public BigDecimal getLineTotal() {
	    return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}
}
