package com.cst438.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="default_cities")
public class DefaultCity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int city_id;
	private String city;
	private String coutry_code;
	private float latitude;
	private float longitude;
	
	public DefaultCity() {
		super();
	}

	public int getCity_id() {
		return city_id;
	}

	public void setCity_id(int city_id) {
		this.city_id = city_id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCoutry_code() {
		return coutry_code;
	}

	public void setCoutry_code(String coutry_code) {
		this.coutry_code = coutry_code;
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
		return "DefaultCity [city_id=" + city_id + ", city=" + city + ", coutry_code=" + coutry_code + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
	
}
