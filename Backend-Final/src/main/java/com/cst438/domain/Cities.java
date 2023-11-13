package com.cst438.domain;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cities")
public class Cities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cityId;

    private String cityName;
    private String countryCode;
    private double latitude;
    private double longitude;

    // Default constructor (required by JPA)
    public Cities() {
    }

    // Constructor without cityId (useful for creating instances without an ID, e.g., when inserting new records)
    public Cities(String cityName, String countryCode, double latitude, double longitude) {
        this.cityName = cityName;
        this.countryCode = countryCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters

    public Long getCityId() {
        return cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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
		return "Cities [cityId=" + cityId + ", cityName=" + cityName + ", countryCode=" + countryCode + ", latitude="
				+ latitude + ", longitude=" + longitude + "]";
	}
    
}