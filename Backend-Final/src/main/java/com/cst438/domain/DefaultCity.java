package com.cst438.domain;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "default_cities")
public class DefaultCity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cityId;
    @Column(name = "city_name") // Update to match the actual column name in the database
    private String city;
    private String countryCode;
    private double latitude;
    private double longitude;

    // Default constructor (required by JPA)
    public DefaultCity() {
    }

    // Constructor without cityId (useful for creating instances without an ID, e.g., when inserting new records)
    public DefaultCity(String cityName, String countryCode, double latitude, double longitude) {
        this.city = cityName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters

    public Long getCityId() {
        return cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String cityName) {
        this.city= cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

	@Override
	public String toString() {
		return "Cities [cityId=" + cityId + ", cityName=" + city + ", countryCode=" + countryCode + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
    
}