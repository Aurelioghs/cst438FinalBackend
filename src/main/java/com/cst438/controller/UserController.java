package com.cst438.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import com.cst438.domain.CoordinateRepository;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.UserDTO;

@RestController
@CrossOrigin
public class UserController {
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CoordinateRepository coordinateRepository;

	// Methods:
	// add a user
	// get a user by email
	// get list of all users
	//
	
	// Adds a student
	@PostMapping("/user")
	@Transactional
	public boolean addUsesr( @RequestBody UserDTO userDto ) {
		User user = userRepository.findByEmail(userDto.email());
		if(user != null) {
			// User is already in system
			return false;
		}
		// User not found in system, make new user
		User newUser = new User();
		newUser.setName(userDto.name());
		newUser.setEmail(userDto.email());
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		newUser.setPassword(encoder.encode(userDto.password()));
		newUser.setRole(userDto.role());
		newUser.setCity(userDto.city());
		newUser.setState_code(userDto.state_code());
		newUser.setCountry_code(userDto.country_code());
		// Save new user to repository
		userRepository.save(newUser);
		
		// Save Coordinate for user's entered home
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9"; // WeatherApp API Key
		String geoCode = "http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}";
		String cityName = newUser.getCity();
		String stateCode = newUser.getState_code();
		String countryCode = newUser.getCountry_code();
		String geoCodeEndpoint = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s,%s&appid=%s";						
		String geoUrl = String.format(geoCodeEndpoint, cityName, stateCode, countryCode, apiKey);
		
		// Create instance of RestTemplate for the HTTP Request
		RestTemplate restTemplate = new RestTemplate();
		// Perform a GET request to geoUrl
		// The response should be a String
		ResponseEntity<String> response = restTemplate.getForEntity(geoUrl, String.class);
		String responseBody = response.getBody();
		System.out.println("Response Body: " + responseBody);
//		JSONObject jo = new JSONObject(responseBody);
//		double lat = jo.getDouble("lat");
//		double lon = jo.getDouble("lon");
		return true;
	}
	
	// Get User By Email
	@GetMapping("/user/{email}")
	public User getUserByEmail(@PathVariable String email) {
		User user = userRepository.findByEmail(email);
		return user;
	}
	
	// Get List of User
	@GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> users = (List<User>) userRepository.findAll();
        return users;
    }
}
