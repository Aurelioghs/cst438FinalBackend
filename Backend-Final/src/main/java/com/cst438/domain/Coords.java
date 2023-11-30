package com.cst438.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;

@Entity
@Table(name = "coords")
public class Coords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int coord_id;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;
    private double lat;
    private double lon;
    
    
    public int getUserId() {
        return user.getId();
    }

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLon() {
		return lon;
	}
	public void setLon(double lon) {
		this.lon = lon;
	}
	@Override
	public String toString() {
		return "Coords [coord_id=" + coord_id + ", user=" + user + ", lat=" + lat + ", lon=" + lon + "]";
	}
	

	
	
    
    
}
