package com.cst438.domain;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Table(name="user_table")
public class User {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
    private String name;
    private String email;
    private String password;
    private String role;
    private String city;
    private String statecode;
    private String countrycode;

	public User() {
		
	}
	

	public User(String username, String password, String role) {
		setPassword(password);
		setRole(role);
	}
	
	
	
	public User(String name, String email, String password, String role, String city) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = role;
		this.city = city;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (password.startsWith("$2a$10") && password.length()>10) {
			// assume password already encrypted
			this.password=password;
		} else {
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			this.password = encoder.encode("password");
		}
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getStateCode() {
		return statecode;
	}

	public void setStateCode(String stateCode) {
		this.statecode = stateCode;
	}
	
	


	public String getCountryCode() {
		return countrycode;
	}


	public void setCountryCode(String countryCode) {
		this.countrycode = countryCode;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", role=" + role
				+ ", city=" + city + ", stateCode=" + statecode + ", countryCode=" + countrycode + "]";
	}




	
}
