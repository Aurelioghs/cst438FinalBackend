package com.cst438.controller;

import java.security.Principal;
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
	// add a user [done]
	// get a user by email [done]
	// get list of all users [done]
	// get list of all coordinates [done]
	// get list of all default cities [done]
	// get home city [done]
	// get list of all user cities [done]
	// add a city to the user_cities table [done]
	// delete a user [done]
	// add a city to the default city table [done]
	// delete a city from the default city table [done]
	// delete a city from user city table [done]
	// update the address of the user
	
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
		// String geoCode = "http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}";
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
	// Principal user holds the name of user and their password
	@PostMapping("/city/{user_id}")
	@Transactional
	public boolean addUserCity( @RequestBody CityDTO cityDto, Principal user) {
		// Grab the user from the user table
		// by getting the user based on the name
		User currentUser = userRepository.findByName(user.getName());
		if(currentUser == null) {
			// if for some reason the user does not exist
			return false;
		}
		
		// check the list of user cities that are linked to the user
		// to see if the desired city is in the user_cities table
		List<UserCity> userCities = userCityRepository.findAllByUser(currentUser.getUser_id());
		if(userCities.size()!= 0) {
			// if the user does have registered cities
			// check to see if there is a city that matches the cityDto
			for (UserCity city :userCities) {
				if (city.getCity().equals(cityDto.city()) ) {
					return false;
				}
			}
		}
		
		UserCity newUserCity = new UserCity();
		newUserCity.setUser(currentUser);
		newUserCity.setCity(cityDto.city());
		newUserCity.setCountry_code(cityDto.country_code());
		// cityDto only has the city name and the country code
		// we will need to obtain the latitude and longitude
		// the same way we did in the addUser method
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9"; // WeatherApp API Key
		// String geoCode = "http://api.openweathermap.org/geo/1.0/direct?q={city name},{country code}&limit={limit}&appid={API key}";
		String cityName = newUserCity.getCity();
		String countryCode = newUserCity.getCountry_code();
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
		newUserCity.setLatitude(lat);
		newUserCity.setLongitude(lon);
		
		//Finally save the newUserCity to the repository
		userCityRepository.save(newUserCity);
		return true;
	}
	
	// Deletes a user
	@DeleteMapping("/user/{user_id}")
	@Transactional
	public boolean deleteUser(@PathVariable int user_id) {
		// Before a user is deleted, we must delete:
		// The Coordinate associated to the user in the coords table
		// The UserCity[s] associated to the user in the user_cities table
		List<Coordinate> coordinates = coordinateRepository.findAllByUser(user_id);
		for (Coordinate coordinate : coordinates) {
	        if (coordinateRepository.existsById(coordinate.getCoord_id())) {
	        	coordinateRepository.deleteById(coordinate.getCoord_id());
	        }
	    }
		
		List<UserCity> userCities = userCityRepository.findAllByUser(user_id);
		for (UserCity userCity : userCities) {
	        if (userCityRepository.existsById(userCity.getCity_id())) {
	        	userCityRepository.deleteById(userCity.getCity_id());
	        }
	    }
		
		// At this point all Coordinate and UserCity associated to the user should be deleted
		if (userRepository.existsById(user_id)) {
			userRepository.deleteById(user_id);
            return true;
        }
        return false;
	}
	
	// Adds a city to the default city table
	// Request Body needs a CityDTO
	@PostMapping("/default")
	@Transactional
	public boolean addDefaultCity( @RequestBody CityDTO cityDto ) {
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
		newDefault.setCountry_code(cityDto.country_code());
		
		// we will need to obtain the latitude and longitude
		// the same way we did in the addUser method
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9"; // WeatherApp API Key
		String cityName = newDefault.getCity();
		String countryCode = newDefault.getCountry_code();
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
				if (defaultCityRepository.existsById(city.getCity_id())) {
					// here we actually delete the default city
					System.out.println("Deleting " + city.getCity());
					defaultCityRepository.deleteById(city.getCity_id());
		            return true;
		        }
			}
		}
		
		// if we reach here, that means that there are default cities
		// but the city we wanted to delete was not in the list
		return false;
	}
	
	// Deletes a city from user city table
	// Handles the same as deleteDefault()
	// Except we need to findAllByUser rather than findAll
	@DeleteMapping("/city/{city_name}")
	@Transactional
	public boolean deleteUserCity(@PathVariable String city_name, Principal user) {
		// get the user from the Principal
		User currentUser = userRepository.findByName(user.getName());
		if(currentUser == null) {
			// if for some reason the user does not exist
			return false;
		}
		
		// user_cities table can have duplicate city names
		// but a user should not have duplicate cities attached to them
		List<UserCity> userCities = userCityRepository.findAllByUser(currentUser.getUser_id());
		if(userCities.size()== 0) {
			// if there are no cities that the user has added
			return false;
		}
		
		// look through all the user cities
		// to find the city that matches
		for (UserCity city :userCities) {
			if (city.getCity().equals(city_name) ) {
				if (userCityRepository.existsById(city.getCity_id())) {
					// here we actually delete the user city
					System.out.println("Deleting " + city.getCity());
					userCityRepository.deleteById(city.getCity_id());
					return true;
				}
			}
		}
				
		// if we reach here, that means that there are user cities
		// but the city we wanted to delete was not in the list
		return false;
	}
}
