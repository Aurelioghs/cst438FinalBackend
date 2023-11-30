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
import org.json.JSONArray;
import org.json.JSONObject;

import com.cst438.domain.Coordinate;
import com.cst438.domain.CoordinateRepository;
import com.cst438.domain.DefaultCityRepository;
import com.cst438.domain.DefaultCity;
import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.domain.UserCityRepository;
import com.cst438.domain.UserCity;
import com.cst438.dto.CityDTO;
import com.cst438.dto.UserDTO;

@RestController
@CrossOrigin
public class UserController {
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CoordinateRepository coordinateRepository;
	
	@Autowired
	DefaultCityRepository defaultCityRepository;
	
	@Autowired
	UserCityRepository userCityRepository;

	// Methods:
	// add a user
	// get a user by email
	// get list of all users
	// get list of all coordinates
	// get list of all default cities
	// get home city
	// get list of all user cities
	// add a city to the user_cities table
	
	// Adds a user
	@PostMapping("/user")
	@Transactional
	public boolean addUser( @RequestBody UserDTO userDto ) {
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
		
		// Get lat and lon from JSON Object
		JSONArray jsonArray = new JSONArray(responseBody);
		JSONObject jo = jsonArray.getJSONObject(0);
		float lat = jo.getFloat("lat");
        float lon = jo.getFloat("lon");

        System.out.println("Latitude: " + lat);
        System.out.println("Longitude: " + lon);
        
        Coordinate location = new Coordinate();
		location.setUser(newUser);
		location.setLon(lon);
		location.setLat(lat);
		coordinateRepository.save(location);
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
	
	// Get List of Coordinates
	@GetMapping("/coords")
    public List<Coordinate> getAllCoordinates() {
        List<Coordinate> coordinates = (List<Coordinate>) coordinateRepository.findAll();
	    return coordinates;
    }
	
	// Get List of DefaultCity
	@GetMapping("/defaults")
	public List<DefaultCity> getAllDefaults() {
		List<DefaultCity> defaults = (List<DefaultCity>) defaultCityRepository.findAll();
	    return defaults;
	}
	
	//Get home city based on user's coords
	
	// Get List of UserCity
	@GetMapping("/cities")
	public List<UserCity> getAllUserCities() {
		List<UserCity> userCities = (List<UserCity>) userCityRepository.findAll();
	    return userCities;
	}
	
	// Adds a city to the user_cities table
	// Request Body needs a CityDTO
	// Path Variable is a user_id
	@PostMapping("/city/{user_id}")
	public boolean addUserCity( @RequestBody CityDTO cityDto, @PathVariable int user_id) {
		// see if the desired city is in the user_cities table
		UserCity userCity = userCityRepository.findByCity(cityDto.city());
		if(userCity != null) {
			// if so then check if the city entry is registered to the user
			if(userCity.getUser().getUser_id() == user_id) {
				// return false if the user has already entered the city
				return false;
			}
		}
		
		UserCity newUserCity = new UserCity();
		// cityDto only has the city name and the country code
		// we will need to obtain the latitude and longitude
		// the same way we did in the addUser method
		return true;
	}
}
