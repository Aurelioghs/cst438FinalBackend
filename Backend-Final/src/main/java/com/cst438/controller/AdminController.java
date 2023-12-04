package com.cst438.controller;
import java.security.Principal;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asynchttpclient.util.Assertions;
import org.openqa.selenium.remote.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.json.*;

import com.cst438.domain.DefaultCity;
import com.cst438.domain.DefaultCityRepository;
import com.cst438.domain.Coords;
import com.cst438.domain.User;
import com.cst438.domain.UserCity;
import com.cst438.domain.UserCityRepository;
import com.cst438.domain.CoordsRepository;
import com.cst438.domain.UserRepository;
import com.cst438.dto.UserDTO;
import com.cst438.dto.*;
@RestController
public class AdminController {
	@Autowired
	UserRepository userRepository;
	@Autowired 
	CoordsRepository  coordinateRepository;
	@Autowired
	DefaultCityRepository defaultCityRepository;
	@Autowired
	UserCityRepository userCityRepository;
	
	@GetMapping("/defaultcities")
	public List<DefaultCity> getAllDefaults() {
		List<DefaultCity> defaults = (List<DefaultCity>) defaultCityRepository.findAll();
	    return defaults;
	}
	@GetMapping("/getusers")//get users to confirm sign up
	@CrossOrigin 
	public List<User> getUserByRole() {
		List<User> users = (List<User>) userRepository.findAllByRole("USER");
		String message = "";
		for (User user :users) {
			System.out.println(user);
			message+=(user.toString());
			message+=("\n");
	    }
	  
	    return users;
	}
	
	@GetMapping("/getALLusers")//get users to confirm sign up
	@CrossOrigin 
	public List<User> getAllUsers() {
		List<User> users = (List<User>) userRepository.findAll();
		String message = "";
		for (User user :users) {
			System.out.println(user);
			message+=(user.toString());
			message+=("\n");
	    }
	    return users;
	}
	
	// Get List of Coordinates
	@GetMapping("/getcoords")
    public List<Coords> getAllCoordinates() {
        List<Coords> coordinates = (List<Coords>) coordinateRepository.findAll();
	    return coordinates;
    }
	
		// Deletes a user
		@DeleteMapping("/user/{user_id}")
		@Transactional
		public boolean deleteUser(@PathVariable int user_id, Principal admin) {
			// Before a user is deleted, we must delete:
			// The Coordinate associated to the user in the coords table
			// The UserCity[s] associated to the user in the user_cities table
			Coords coordinates = coordinateRepository.findByUserId(user_id);

		       if (coordinates != null) {
		    	   coordinateRepository.deleteById(coordinates.getCoord_id());
		       }
			
			List<UserCity> userCities = userCityRepository.findAllByUserId(user_id);
			for (UserCity userCity : userCities) {
		        if (userCityRepository.existsById(userCity.getCity_id())) {
		        	userCityRepository.deleteById(userCity.getCity_id());
		        }
		    }
			
			// At this point all Coordinate and UserCity associated to the user should be deleted
			if (userRepository.existsById(( user_id) )) {
				userRepository.deleteById(user_id);
	            return true;
	        }
	        return false;
		}
		
		// Adds a city to the default city table
		// Request Body needs a CityDTO
		@PostMapping("/default")
		@Transactional
		public boolean addDefaultCity( @RequestBody CityDTO cityDto, Principal admin) {
			// check the list of default cities
			// to see if the desired city is in the default_cities table
			List<DefaultCity> defaults = (List<DefaultCity>) defaultCityRepository.findAll();
			if(defaults.size()!= 0) {
				// if there are default cities
				// check to see if there is a city that matches the cityDto
				for (DefaultCity city :defaults) {
					if (city.getCity().equals(cityDto.city()) ) {
						return false;
					}
				}
			}
			
			// Set city and country_code from cityDto
			DefaultCity newDefault = new DefaultCity();
			newDefault.setCity(cityDto.city());
			newDefault.setCountryCode(cityDto.country_code());
			
			// we will need to obtain the latitude and longitude
			// the same way we did in the addUser method
			String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9"; // WeatherApp API Key
			String cityName = newDefault.getCity();
			String countryCode = newDefault.getCountryCode();
			String geoCodeEndpoint = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s&appid=%s";						
			String geoUrl = String.format(geoCodeEndpoint, cityName, countryCode, apiKey);
					
			// Create instance of RestTemplate for the HTTP Request
			RestTemplate restTemplate = new RestTemplate();
			// Perform a GET request to geoUrl
			// The response should be a String
			ResponseEntity<String> response = restTemplate.getForEntity(geoUrl, String.class);
			String responseBody = response.getBody();
			System.out.println("Response Body: " + responseBody);
							
			// Get lat and lon from JSON Object
			JSONArray jsonArray = new JSONArray(responseBody);
			JSONObject jo = jsonArray.getJSONObject(0);
			float lat = jo.getFloat("lat");
			float lon = jo.getFloat("lon");		
			
			// Add lat and lon to the newUserCity
			newDefault.setLatitude(lat);
			newDefault.setLongitude(lon);
					
			//Finally save the newUserCity to the repository
			defaultCityRepository.save(newDefault);
			return true;
		}
		
		
		// Deletes a city from the default city table
		@DeleteMapping("/default/{city_name}")
		@Transactional
		public boolean deleteDefault(@PathVariable String city_name) {
			// default_cities table should not have any duplicate city names
			List<DefaultCity> defaults = (List<DefaultCity>) defaultCityRepository.findAll();
			if(defaults.size()== 0) {
				// if there are no default cities
				return false;
			}
			// look through all the default cities
			// to find the city that matches
			for (DefaultCity city :defaults) {
				if (city.getCity().equals(city_name) ) {
					if (defaultCityRepository.existsById(city.getCityId())) {
						// here we actually delete the default city
						System.out.println("Deleting " + city.getCity());
						defaultCityRepository.deleteById(city.getCityId());
			            return true;
			        }
				}
			}
			
			// if we reach here, that means that there are default cities
			// but the city we wanted to delete was not in the list
			return false;
		}
	

}
