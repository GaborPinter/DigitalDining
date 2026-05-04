package com.DigitalDining.demo.dto;

public class WeeklyMenuSelectionForm {

	private String soupOption;
    private String mainOption;
    private String dessertOption;

    public String getSoupOption() { return soupOption; }
    public void setSoupOption(String soupOption) { this.soupOption = soupOption; }

    public String getMainOption() { return mainOption; }
    public void setMainOption(String mainOption) { this.mainOption = mainOption; }

    public String getDessertOption() { return dessertOption; }
    public void setDessertOption(String dessertOption) { this.dessertOption = dessertOption; }

    public boolean hasAnySelection() {
        return (soupOption != null && !soupOption.isBlank())
                || (mainOption != null && !mainOption.isBlank())
                || (dessertOption != null && !dessertOption.isBlank());
    }
}
