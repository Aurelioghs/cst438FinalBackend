package com.cst438.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="user_cities")
public class UserCity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int city_id;
	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "user_id")
	private User user;
	@Column(name = "city_name")
	private String city;
	private String country_code;
	private float latitude;
	private float longitude;
	
	public int getCity_id() {
		return city_id;
	}
	public void setCity_id(int city_id) {
		this.city_id = city_id;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city_name) {
		this.city = city_name;
	}
	public String getCountry_code() {
		return country_code;
	}
	public void setCountry_code(String country_code) {
		this.country_code = country_code;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	@Override
	public String toString() {
		return "UserCity [city_id=" + city_id + ", user=" + user + ", city_name=" + city + ", country_code="
				+ country_code + ", latitude=" + latitude + ", longitude=" + longitude + "]";
	}
	
	

}