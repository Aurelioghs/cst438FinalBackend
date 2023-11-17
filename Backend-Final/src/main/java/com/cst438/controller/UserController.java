package com.cst438.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asynchttpclient.util.Assertions;
import org.openqa.selenium.remote.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.JsonObjectSerializer;
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

import com.cst438.domain.Cities;
import com.cst438.domain.CitiesRepository;
import com.cst438.domain.Coords;
import com.cst438.domain.User;
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
	CitiesRepository citiesRepository;
	
	@GetMapping("/getcities")
	public ResponseEntity<?> getAllCities() {
	      Iterable<Cities> cities = citiesRepository.findAll();
	      String message ="";
	      for (Cities city :cities) {
	    		message+=(city.toString());
				message+=("\n");
	      }
	      return ResponseEntity.ok(message.toString());
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
		userRepository.save(newUser);
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
		
		List<Coords> userCoords = coordsRepository.findByUserId(newUser.getId());
		for(Coords coordsOfUser:userCoords) {
			System.out.println("USERS COORDS: "+coordsOfUser.toString());
		}
	
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
    	WeatherDTO weatherdto = new WeatherDTO(Fahrenheit,Celsius,desc,windSpeed);
    	String message ="";	
    	message +=String.format("Weather for %s. F: %d. C:%d." ,city,Fahrenheit,Celsius);
    	message +=String.format("Description:%s. Wind Speed:%d.\n",desc,windSpeed );
    	
		//return weatherdto;
    	return ResponseEntity.ok(message);
	}
	
	@GetMapping("/getuserweather")//Default weather user sees based on their address
	@CrossOrigin 
	public  ResponseEntity<?> getUserWeather(Principal user) {
		//Use the principal to get user email, in order to use in findByEmail(), however principal might be null as of now.
		//Right now this mapping works due to permitAll(), it will prob fail if we use .authenticated() or hasAnyRole()
		
		
	/*	//The coords are saved, the below call should be made in getWeather. This was testing to see if it works
		String weatherAPI = "https://api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}";
      	String oneCallAPIEndpoint = "https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&appid=%s";
      	String oneCallUrl = String.format(oneCallAPIEndpoint, String.valueOf(lat), String.valueOf(lon), apiKey);
      	System.out.println(oneCallUrl);
      	restTemplate = new RestTemplate();
      	response  = restTemplate.getForEntity(oneCallUrl, String.class);
     	System.out.println(response.getBody());
     	
     	JSONObject weather = new JSONObject(response.getBody());
    	//System.out.println(weather);
    	double Kelvin = weather.getJSONObject("current").getDouble("temp");
        //System.out.println(Kelvin);
    	int Celsius =(int) Math.round(Kelvin - 273.15);
	    int Fahrenheit =(int) Math.round((Kelvin - 273.15) * 9/5 + 32);
    	System.out.println(String.format("F: %f.  C:%s" ,Fahrenheit,Celsius));
    	
    	String desc= weather.getJSONObject("current").getJSONArray("weather").getJSONObject(0).getString("description");
    	System.out.println("Description: " + desc);
    	
    	int  windSpeed= (int) Math.round(weather.getJSONObject("current").getDouble("wind_speed"));
    	System.out.println(String.format("WindSpeed:%d ", windSpeed));
    	*/
		
		 /*Map<String, Object> response = new LinkedHashMap<>();
		    response.put("tempF",Farhrenheit);
		    response.put("tempC", Celsius);
		    response.put("desc", desc);
		    response.put("windSpeed",windSpeed);
		return ResponseEntity.ok(response);*///Not sure if we should return response entity or just DTO
		return null;
  
	}
	@GetMapping("/getweathers")//default cities for user to view weathers of
	@CrossOrigin 
	public ResponseEntity<WeatherDTO[]> getWeathers() {//return weatherDTOs array?
		List<Cities> cities = (List<Cities>) citiesRepository.findAll();
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";
      	String oneCallAPIEndpoint = "https://api.openweathermap.org/data/3.0/onecall?lat=%s&lon=%s&appid=%s";
    	RestTemplate restTemplate = new RestTemplate();
    	WeatherDTO[] weathers = new WeatherDTO[cities.size()];
      	int i = 0;
      	String message ="";
		 for (Cities city :cities) {
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
	    	weathers[i++] = new WeatherDTO(Fahrenheit, Celsius, desc, windSpeed);
	        }
	    	
		return ResponseEntity.ok(weathers);
		
		
		//Below code used to get lat/lon for default cities
		/*String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";//kens key?
		String geoCode = "http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}";
		String geoCodeEndpoint = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s&appid=%s";		
		Map<String, String> cityMap = new HashMap<>();
	        cityMap.put("Paris", "FR");
	        cityMap.put("New York", "US");
	        cityMap.put("London", "GB");
	        cityMap.put("Tokyo", "JP");
	        cityMap.put("Sydney", "AU");
	        cityMap.put("Rome", "IT");
	        cityMap.put("Cairo", "EG");
	        cityMap.put("Moscow", "RU");
	        int k =0;
	        for (Map.Entry<String, String> entry : cityMap.entrySet()) {
	           // System.out.println("City: " + entry.getKey() + ", Country Code: " + entry.getValue());
	            String cityName = entry.getKey();
	    		String countryCode= entry.getValue();
	    
	    		String geoUrl = String.format(geoCodeEndpoint, cityName,  countryCode, apiKey);
	    		//System.out.println("URL: "+geoUrl);
	    		RestTemplate restTemplate = new RestTemplate();
	    		ResponseEntity<String> response = restTemplate.getForEntity(geoUrl, String.class);
	    		JSONArray data =new JSONArray(response.getBody());
	    		double lat =data.getJSONObject(0).getDouble("lat");
	    		double lon = data.getJSONObject(0).getDouble("lon");
	    		String coords = String.format("%s, %s,:lat %f  lon %f",entry.getKey(),entry.getValue(), lat, lon); 
	    		System.out.println(coords);
	    		//maybe use the coords here to store in a new table, which will then be used instead inside the weather api to halve the api calls
	    		
	        }*/
	     
		//System.out.println(response.getStatusCode());
	}
	
}