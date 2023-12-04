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
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	@Autowired 
	CoordsRepository  coordsRepository;
	@Autowired
	DefaultCityRepository defaultCityRepository;
	@Autowired
	UserCityRepository userCityRepository;
	
	@GetMapping("/getuser")
	public ResponseEntity<?> getUser(Principal user) {
		System.out.println("USER IS AUTH");
        String username = user.getName();
        User currentUser = userRepository.findByName(username);
    	System.out.println(currentUser);
    	return new ResponseEntity<>(currentUser, HttpStatus.OK);
	}
	
	
	//Get home city based on user's coords
	// Get List of UserCity
	@GetMapping("/usercities")
	public List<UserCity> getAllUserCities( Principal user) {
		 String username = user.getName();
	     User currentUser = userRepository.findByName(username);
		List<UserCity> userCities = (List<UserCity>) userCityRepository.findAllByUserId(currentUser.getId());
	    return userCities;
	}
	
	  
	@PostMapping("/signup")//same as addUser
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
    	double windSpeedKph = Math.round(windSpeed * 3.60000288 * 10.0) / 10.0;
		double windSpeedMph = Math.round(windSpeed *2.2369380816 * 10.0) / 10.0;
    	System.out.println(String.format("WindSpeed:%d ", windSpeed));
    	WeatherDTO weatherdto = new WeatherDTO(city,Fahrenheit,Celsius,desc,windSpeedMph,windSpeedKph);
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
    	double windSpeedKph = Math.round(windSpeed * 3.60000288 * 10.0) / 10.0;
		double windSpeedMph = Math.round(windSpeed *2.2369380816 * 10.0) / 10.0;
    	System.out.println(String.format("WindSpeed:%d ", windSpeed));
    	WeatherDTO currentWeather = new WeatherDTO(currentUser.getCity(),Fahrenheit, Celsius, desc, windSpeedMph,windSpeedKph);

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
		List<UserCity> userCities = userCityRepository.findAllByUserId(currentUser.getId());
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
	    	double windSpeedKph = Math.round(windSpeed * 3.60000288 * 10.0) / 10.0;
			double windSpeedMph = Math.round(windSpeed *2.2369380816 * 10.0) / 10.0;
	    	message += String.format("Weather for %s, %s. F: %d. C:%d.", city.getCity(), city.getCountryCode(), Fahrenheit, Celsius);
	    	message += String.format(" Description:%s. Wind Speed:%d.\n", desc, windSpeed);
	    	weathers[i++] = new WeatherDTO(city.getCity(),Fahrenheit, Celsius, desc, windSpeedMph,windSpeedKph);
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
	    	double windSpeedMph = Math.round(windSpeed * 3.60000288 * 10.0) / 10.0;
			double windSpeedKph = Math.round(windSpeed *2.2369380816 * 10.0) / 10.0;
	    	weathers[i++] = new WeatherDTO(city.getCity(),Fahrenheit, Celsius, desc, windSpeedMph,windSpeedKph);
		 }
		 
	
	
		return ResponseEntity.ok(weathers);
		
	
	}
	

//Adds a city to the user_cities table
	// Request Body needs a CityDTO
	// Path Variable is a user_id
	@PostMapping("/addcity")
	public ResponseEntity<?> addUserCity(@RequestBody CityDTO cityDTO,Principal user ) {
		// see if the desired city is in the user_cities table
		System.out.println(cityDTO);
		User currentUser = userRepository.findByName(user.getName());
		// Set the user for newUserCity
		// by getting the user based on the user_id
		if(currentUser == null) {
			// if for some reason the user does not exist
			  return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
		
		List<DefaultCity> defaultcities = (List<DefaultCity>) defaultCityRepository.findAll(); 
		for (DefaultCity defaultCity:defaultcities) {
			if (defaultCity.getCountryCode().equals(cityDTO.country_code()) ) {
				if (defaultCity.getCity().equals(cityDTO.city())) {
					return new ResponseEntity<>("City already exists in the table", HttpStatus.BAD_REQUEST);
				}
			}
		}
		
		List<UserCity> userCities = userCityRepository.findAllByUserId(currentUser.getId());//Find all cities related to user
		System.out.println(userCities);
		if(userCities.size()!= 0) {
			// if so then check if the city entry is registered to the user
			System.out.println("FOUND USER WITH CITIES");//Check if current user has added cities matching DTO
			for (UserCity city :userCities) {
			
				if (city.getCity().equals(cityDTO.city()) ) {
					  return new ResponseEntity<>("City already exists in the table", HttpStatus.BAD_REQUEST);
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
	    return new ResponseEntity<>("City added successfully", HttpStatus.OK);
		}
	
	@GetMapping("/getmoredata/{city}")//Weather user sees based on their search
	@CrossOrigin 
	public  ResponseEntity<?> getMoreData(@PathVariable String city) {
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
	 	//System.out.println(response.getBody());
	 	JSONObject weather = new JSONObject(response.getBody());

		double Kelvin = weather.getJSONObject("current").getDouble("temp");
		int Celsius =(int) Math.round(Kelvin - 273.15);
		int Fahrenheit =(int) Math.round((Kelvin - 273.15) * 9/5 + 32);

		
		//System.out.println(String.format("F: %d.  C:%d" ,Fahrenheit,Celsius));
		
		String desc= weather.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
		//System.out.println("Description: " + desc);
		
		int windSpeed= (int) Math.round(weather.getJSONObject("current").getDouble("wind_speed"));
		double windSpeedKph = Math.round(windSpeed * 3.60000288 * 10.0) / 10.0;
		double windSpeedMph = Math.round(windSpeed *2.2369380816 * 10.0) / 10.0;
		System.out.println(String.format("WindSpeed:%d  WindSpeedMph:%f   WindSpeedKph:%f ", windSpeed,windSpeedMph,windSpeedKph));
		double feelsLikeK= weather.getJSONObject("current").getDouble("feels_like");
		int feelsLikeC =(int) Math.round(feelsLikeK - 273.15);
		int feelsLikeF =(int) Math.round((feelsLikeK - 273.15) * 9/5 + 32);
		int humidity = weather.getJSONObject("current").getInt("humidity");
		int cloudsPercent = weather.getJSONObject("current").getInt("clouds");
		int visibilityM= weather.getJSONObject("current").getInt("visibility");
		double visibilityKiloM = Math.round(visibilityM / 1000.0 * 10.0) / 10.0;
		double visibilityMiles = Math.round(visibilityKiloM * 0.621371 * 10.0) / 10.0;
		int sunrise= weather.getJSONObject("current").getInt("sunrise");
		int sunset= weather.getJSONObject("current").getInt("sunset");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		Date dateSunrise = new Date(sunrise);
		  System.out.println("Current Date Sunrise: " + dateSunrise);
		String timeSunrise = timeFormat.format(dateSunrise);
		Date dateSunset = new Date(sunset);
		String timeSunset= timeFormat.format(dateSunset);
	    System.out.println("Current Time Sunrise: " + timeSunrise);
		//System.out.println(String.format("WindSpeed:%d ", windSpeed));
		
		System.out.println(String.format("Feels Like Temperature: %d", feelsLikeC));
		System.out.println(String.format("Humidity: %d", humidity));
		System.out.println(String.format("Clouds Percentage: %d", cloudsPercent));
		System.out.println(String.format("Visibility kilometers: %f", visibilityKiloM));
		System.out.println(String.format("Visibility miles: %f", visibilityMiles));
		//WeatherDTO weatherdto = new WeatherDTO(city,Fahrenheit,Celsius,desc,windSpeed);
		MoreWeatherInfoDTO weatherdto = new MoreWeatherInfoDTO(
				Fahrenheit,
				Celsius,
				desc,
				windSpeedMph,
				windSpeedKph,
			    feelsLikeF,
			    feelsLikeC,
			    humidity,
			    cloudsPercent,
			    visibilityMiles,
			    visibilityKiloM,
			    timeSunrise,
			    timeSunset
			);
		String message ="";	
		message +=String.format("Weather for %s. F: %d. C:%d." ,city,Fahrenheit,Celsius);
		message +=String.format("Description:%s. Wind Speed:%d.\n",desc,windSpeed );
		
		//return weatherdto;
		return ResponseEntity.ok(weatherdto);
	}

	// Deletes a city from user city table
		// Handles the same as deleteDefault()
		// Except we need to findAllByUser rather than findAll
		@DeleteMapping("/city/{city_name}")
		@CrossOrigin
		public boolean deleteUserCity(@PathVariable String city_name, Principal user) {
			// get the user from the Principal
			User currentUser = userRepository.findByName(user.getName());
			if(currentUser == null) {
				// if for some reason the user does not exist
				return false;
			}
			
			// user_cities table can have duplicate city names
			// but a user should not have duplicate cities attached to them
			List<UserCity> userCities = userCityRepository.findAllByUserId(currentUser.getId());
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




