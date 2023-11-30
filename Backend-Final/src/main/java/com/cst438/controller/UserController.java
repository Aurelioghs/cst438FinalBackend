package com.cst438.controller;

import java.security.Principal;
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
import org.springframework.web.bind.annotation.CrossOrigin;
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
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	@Autowired 
	CoordsRepository  coordsRepository;
	@Autowired
	DefaultCityRepository defaultCityRepository;
	@Autowired
	UserCityRepository userCityRepository;
	
	// Get List of DefaultCity
	@GetMapping("/defaultcities")
	public List<DefaultCity> getAllDefaults() {
		List<DefaultCity> defaults = (List<DefaultCity>) defaultCityRepository.findAll();
	    return defaults;
	}
	
	//Get home city based on user's coords
	
	// Get List of UserCity
	@GetMapping("/usercities")
	public List<UserCity> getAllUserCities() {
		List<UserCity> userCities = (List<UserCity>) userCityRepository.findAll();
	    return userCities;
	}
	
	  
	@GetMapping("/getusers")//get users to confirm sign up
	@CrossOrigin 
	public ResponseEntity<?> getAll() {
		Iterable<User> users = userRepository.findAll();
		String message = "";
		for (User user :users) {
			System.out.println(user);
			message+=(user.toString());
			message+=("\n");
	    }
	  
	    return ResponseEntity.ok(message.toString());
	}
	
	// Get List of Coordinates
	@GetMapping("/getcoords")
    public List<Coords> getAllCoordinates() {
        List<Coords> coordinates = (List<Coords>) coordsRepository.findAll();
	    return coordinates;
    }
	
	@PostMapping("/signup")
	@CrossOrigin 
	public ResponseEntity<?> signUp(@RequestBody UserDTO user) {
		System.out.println("INIDE SIGN UP");
		User testuser = userRepository.findByEmail(user.email());
		if (testuser != null) {
			System.out.println("FOUND");
			System.out.println(testuser.getCity());
		}
		System.out.println(user);
		
		User newUser = new User();
		newUser.setName(user.name());
		newUser.setEmail(user.email());

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	    String password = user.password();
	    String encrpted_password = encoder.encode(password);
	    
		newUser.setPassword(encrpted_password);
		newUser.setRole("USER");
		newUser.setCity(user.city());
		newUser.setStateCode(user.statecode());
		newUser.setCountryCode(user.countrycode());
		//System.out.println(newUser.toString());
		UserDTO userDTO = new UserDTO(newUser.getName(), newUser.getEmail(), newUser.getPassword(), 
				newUser.getCity(), newUser.getStateCode(), newUser.getCountryCode());
		
		System.out.println(userDTO);
		try {
		    userRepository.save(newUser);
		    // other code if the save is successful
		} catch (DataIntegrityViolationException ex) {
		    String errorMessage = "User with the same name already exists.";
		    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorMessage);
		}
		newUser = userRepository.findByEmail(user.email());
		if (newUser!= null) {
			System.out.println("WORKED");
			System.out.println("USER: " + newUser.toString());
		}
		else {
			System.out.println("FAIL");
		}
		
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";
		String geoCode = "http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}";
		String cityName = newUser.getCity();
		String stateCode = newUser.getStateCode();
		String countryCode = newUser.getCountryCode();
		String geoCodeEndpoint = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s,%s&appid=%s";						
		String geoUrl = String.format(geoCodeEndpoint, cityName, stateCode, countryCode, apiKey);
		System.out.println("URL: "+geoUrl);
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(geoUrl, String.class);
		System.out.println(response.getStatusCode());
	
		
		String responseBody = response.getBody();
	    responseBody = responseBody.substring(1, responseBody.length() - 1);
		System.out.println("Response Body: " + responseBody);
		JSONObject jo =new JSONObject(responseBody);
		double lat = jo.getDouble("lat");
		double lon = jo.getDouble("lon");
		String coords = String.format("lat %f  lon %f", lat, lon); 
		System.out.println(coords);
		
		Coords location = new Coords();
		location.setUser(newUser);
		location.setLon(lon);
		location.setLat(lat);
		coordsRepository.save(location);
		Iterable<Coords> testloc = coordsRepository.findAll();
		for(Coords loc:testloc) {
			System.out.println("HERE ARE THE COORDS: "+loc.toString());
		}
		
		Coords userCoords = coordsRepository.findByUserId(newUser.getId());
		System.out.println("USERS COORDS: "+userCoords.toString());
		
	
    	String message = "User signed up successfully";
        //return ResponseEntity.ok(message);
    	 return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
        //Maybe add exception handling for when a user couldnt sign up for some reason. Email should be unique, maybe check for that somewhere here
	}
	@GetMapping("/getweather/{city}")//Weather user sees based on their search
	@CrossOrigin 
	public  ResponseEntity<?> getCityWeather(@PathVariable String city) {
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";
		String geoCodeEndpoint = "http://api.openweathermap.org/geo/1.0/direct?q=%s&appid=%s";
		String geoUrl = String.format(geoCodeEndpoint, city, apiKey);
		System.out.println("URL: "+ geoUrl);
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(geoUrl, String.class);
		String responseBody = response.getBody();
	    responseBody = responseBody.substring(1, responseBody.length() - 1);
		System.out.println("Response Body: " + responseBody);
		JSONObject jo =new JSONObject(responseBody);
		double lat = jo.getDouble("lat");
		double lon = jo.getDouble("lon");
		String coords = String.format("lat %f  lon %f", lat, lon); 
		
		System.out.println(coords);
	 	String oneCallAPIEndpoint = "https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&appid=%s";
      	String oneCallUrl = String.format(oneCallAPIEndpoint, String.valueOf(lat), String.valueOf(lon), apiKey);
    	System.out.println(oneCallUrl);
      	restTemplate = new RestTemplate();
      	response  = restTemplate.getForEntity(oneCallUrl, String.class);
     	System.out.println(response.getBody());
     	JSONObject weather = new JSONObject(response.getBody());
 
    	double Kelvin = weather.getJSONObject("current").getDouble("temp");
    	int Celsius =(int) Math.round(Kelvin - 273.15);
    	int Fahrenheit =(int) Math.round((Kelvin - 273.15) * 9/5 + 32);
    	System.out.println(String.format("F: %d.  C:%d" ,Fahrenheit,Celsius));
    	
    	String desc= weather.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
    	System.out.println("Description: " + desc);
    	
    	int windSpeed= (int) Math.round(weather.getJSONObject("current").getDouble("wind_speed"));
    	System.out.println(String.format("WindSpeed:%d ", windSpeed));
    	WeatherDTO weatherdto = new WeatherDTO(city,Fahrenheit,Celsius,desc,windSpeed);
    	String message ="";	
    	message +=String.format("Weather for %s. F: %d. C:%d." ,city,Fahrenheit,Celsius);
    	message +=String.format("Description:%s. Wind Speed:%d.\n",desc,windSpeed );
    	
		//return weatherdto;
    	return ResponseEntity.ok(weatherdto);
	}
	
	@GetMapping("/getuserweather")//Default weather user sees based on their address
	@CrossOrigin 
	public ResponseEntity<WeatherDTO> getUserWeather(Principal user) {
		double lat = 0;
		double lon = 0;
		 User currentUser = null;
		if (user != null) {
			System.out.println("USER IS AUTH");
	        String username = user.getName();
	        currentUser = userRepository.findByName(username);//username also has unique constraint like email
	        Coords coords =  coordsRepository.findByUserId(currentUser.getId());
	        lat = coords.getLat();
	        lon = coords.getLon();
	        System.out.println(coords.toString());
	        
		}
		else {
			System.out.println("USER IS NULL");
		}
		//Use the principal to get user email, in order to use in findByEmail(), however principal might be null as of now.
		//Right now this mapping works due to permitAll(), it will prob fail if we use .authenticated() or hasAnyRole()
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";
		String weatherAPI = "https://api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}";
      	String oneCallAPIEndpoint = "https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&appid=%s";
      	String oneCallUrl = String.format(oneCallAPIEndpoint, String.valueOf(lat), String.valueOf(lon), apiKey);
      	//System.out.println(oneCallUrl);
      	RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(oneCallUrl, String.class);
		String responseBody = response.getBody();
     	System.out.println(responseBody);
     	
     	JSONObject weather = new JSONObject(response.getBody());
    	//System.out.println(weather);
    	double Kelvin = weather.getJSONObject("current").getDouble("temp");
        //System.out.println(Kelvin);
    	int Celsius =(int) Math.round(Kelvin - 273.15);
	    int Fahrenheit =(int) Math.round((Kelvin - 273.15) * 9/5 + 32);
    	System.out.println(String.format("F: %d.  C:%d" ,Fahrenheit,Celsius));
    	
    	String desc= weather.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
    	System.out.println("Description: " + desc);
    	
    	int  windSpeed= (int) Math.round(weather.getJSONObject("current").getDouble("wind_speed"));
    	System.out.println(String.format("WindSpeed:%d ", windSpeed));
    	WeatherDTO currentWeather = new WeatherDTO(currentUser.getCity(),Fahrenheit, Celsius, desc, windSpeed);

		return ResponseEntity.ok(currentWeather);
	
  
	}
	
	@GetMapping("/getweathers")//default cities for user to view weathers of
	@CrossOrigin 
	public ResponseEntity<WeatherDTO[]> getWeathers(Principal user) {//return weatherDTOs array?
		/*if (user!=null) {
			System.out.println("USER IS AUTH");
	        String username = user.getName();
	        User currentUser = userRepository.findByName(username);
	    	System.out.println(currentUser);
	    	List<UserCity> usercities = userCitiesRepository.findByUserId(currentUser.getId());
	    	//List<UserCity> usercities = (List<UserCity>) userCitiesRepository.findAll();
	    	if (usercities!= null) {
	    		System.out.println(usercities);
	    	}
	    
		}
		else {
			System.out.println("USER NULL");
		}*/
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";
      	String oneCallAPIEndpoint = "https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&appid=%s";
      	
    	RestTemplate restTemplate = new RestTemplate();
    	List<DefaultCity> defaultCities = (List<DefaultCity>) defaultCityRepository.findAll();
    	
   
    	String username = user.getName();
	    User currentUser = userRepository.findByName(username);
		List<UserCity> userCities = userCityRepository.findByUserId(currentUser.getId());
    	WeatherDTO[] weathers = new WeatherDTO[defaultCities.size() + userCities.size()];
      	int i = 0;
      	String message ="";
		for (DefaultCity city :defaultCities) {
			//System.out.println(city);
			double lat = city.getLatitude();
			double lon = city.getLongitude();
			String oneCallUrl = String.format(oneCallAPIEndpoint, String.valueOf(lat), String.valueOf(lon), apiKey);
			ResponseEntity<String> response = restTemplate.getForEntity(oneCallUrl, String.class);
	      	//System.out.println(oneCallUrl);
	     	//System.out.println(response.getBody());
	     	JSONObject weather = new JSONObject(response.getBody());
	    	//System.out.println(weather);
	    	double Kelvin = weather.getJSONObject("current").getDouble("temp");
	        //System.out.println(Kelvin);
	    	int Celsius =(int) Math.round(Kelvin - 273.15);
	    	int Fahrenheit =(int) Math.round((Kelvin - 273.15) * 9/5 + 32);
	    	String desc= weather.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
	    	int windSpeed= (int) Math.round(weather.getJSONObject("current").getDouble("wind_speed"));
	    	message += String.format("Weather for %s, %s. F: %d. C:%d.", city.getCityName(), city.getCountryCode(), Fahrenheit, Celsius);
	    	message += String.format(" Description:%s. Wind Speed:%d.\n", desc, windSpeed);
	    	weathers[i++] = new WeatherDTO(city.getCityName(),Fahrenheit, Celsius, desc, windSpeed);
	        }
		 
		 for (UserCity city :userCities) {
			//System.out.println(city);
			double lat = city.getLatitude();
			double lon = city.getLongitude();
			String oneCallUrl = String.format(oneCallAPIEndpoint, String.valueOf(lat), String.valueOf(lon), apiKey);
			ResponseEntity<String> response = restTemplate.getForEntity(oneCallUrl, String.class);
	      	//System.out.println(oneCallUrl);
	     	//System.out.println(response.getBody());
	     	JSONObject weather = new JSONObject(response.getBody());
	    	//System.out.println(weather);
	    	double Kelvin = weather.getJSONObject("current").getDouble("temp");
	        //System.out.println(Kelvin);
	    	int Celsius =(int) Math.round(Kelvin - 273.15);
	    	int Fahrenheit =(int) Math.round((Kelvin - 273.15) * 9/5 + 32);
	    	String desc= weather.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
	    	int windSpeed= (int) Math.round(weather.getJSONObject("current").getDouble("wind_speed"));
	    	weathers[i++] = new WeatherDTO(city.getCity(),Fahrenheit, Celsius, desc, windSpeed);
		 }
	
		return ResponseEntity.ok(weathers);
		
	
	}
	

//Adds a city to the user_cities table
	// Request Body needs a CityDTO
	// Path Variable is a user_id
	@PostMapping("/addcity")
	public boolean addUserCity(@RequestBody CityDTO cityDTO,Principal user ) {
		// see if the desired city is in the user_cities table
		System.out.println(cityDTO);
		User currentUser = userRepository.findByName(user.getName());
		// Set the user for newUserCity
		// by getting the user based on the user_id
		if(currentUser == null) {
			// if for some reason the user does not exist
			return false;
		}
		
		List<UserCity> userCities = userCityRepository.findByUserId(currentUser.getId());//Find all cities related to user
		System.out.println(userCities);
		if(userCities.size()!= 0) {
			// if so then check if the city entry is registered to the user
			System.out.println("FOUND USER WITH CITIES");//Check if current user has added cities matching DTO
			for (UserCity city :userCities) {
			
				if (city.getCity().equals(cityDTO.city()) ) {
					return false;
				}
			}
			
		}
	
		UserCity newUserCity = new UserCity();
		newUserCity.setUser(currentUser);
		newUserCity.setCity(cityDTO.city());
		newUserCity.setCountry_code(cityDTO.country_code());
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
	
}