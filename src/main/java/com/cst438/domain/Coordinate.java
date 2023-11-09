package com.cst438.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="coords")
public class Coordinate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int coord_id;
	private float lat;
	private float lon;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="user_id")
	private User user;
	
	public Coordinate() {
		super();
	}

	public int getCoord_id() {
		return coord_id;
	}

	public void setCoord_id(int coord_id) {
		this.coord_id = coord_id;
	}

	public float getLat() {
		return lat;
	}

	public void setLat(float lat) {
		this.lat = lat;
	}

	public float getLon() {
		return lon;
	}

	public void setLon(float lon) {
		this.lon = lon;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Coordinate [coord_id=" + coord_id + ", lat=" + lat + ", lon=" + lon + ", user=" + user + "]";
	}
	
}
