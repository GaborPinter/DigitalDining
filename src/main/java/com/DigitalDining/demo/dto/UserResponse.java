package com.DigitalDining.demo.dto;

public class UserResponse {

	private Long id;
	private String username;
	private String email;
	private String role;
	private String roleDisplayName;
	private boolean weeklyMenuSubscribed;

	public UserResponse() {
	}

	public UserResponse(Long id, String username, String email, String role, String roleDisplayName,
			boolean weeklyMenuSubscribed) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.role = role;
		this.roleDisplayName = roleDisplayName;
		this.weeklyMenuSubscribed = weeklyMenuSubscribed;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRoleDisplayName() {
		return roleDisplayName;
	}

	public void setRoleDisplayName(String roleDisplayName) {
		this.roleDisplayName = roleDisplayName;
	}

	public boolean isWeeklyMenuSubscribed() {
		return weeklyMenuSubscribed;
	}

	public void setWeeklyMenuSubscribed(boolean weeklyMenuSubscribed) {
		this.weeklyMenuSubscribed = weeklyMenuSubscribed;
	}
}
