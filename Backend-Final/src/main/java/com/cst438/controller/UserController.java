package com.cst438.controller;

import java.util.List;

import org.openqa.selenium.remote.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.cst438.domain.User;
import com.cst438.domain.UserRepository;
import com.cst438.dto.UserDTO;
import com.cst438.dto.addressDTO;

@RestController
public class UserController {
	
	@Autowired
	UserRepository userRepository;
	
	@GetMapping("/get")
	@CrossOrigin 
	public void getAll() {
		Iterable<User> users = userRepository.findAll();
		for (User user :users) {
			System.out.println(user);
		}
	}
	@PostMapping("/signup")
	@CrossOrigin 
	public ResponseEntity<?> signUp(@RequestBody UserDTO user) {
		System.out.println("INISDE SIGN UP");
		User loggeduser = userRepository.findByName("user");
		if (loggeduser != null) {
			System.out.println("FOUND");
			System.out.println(loggeduser.getCity());
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
		newUser.setStateCode(user.statecode());
		newUser.setCountryCode(user.countrycode());
		System.out.println(newUser.toString());
		userRepository.save(newUser);
		User test = userRepository.findByName(user.name());
		if (test!= null) {
			System.out.println("WROKED");
		}
		else {
			System.out.println("FAIOL");
		}
		
		String apiKey = "3c68fedb4d4cc2ee43ad218fedc95ec9";//kens key?
		String geoCode = "http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}";
		String cityName = newUser.getCity();
		String stateCode = newUser.getStateCode();
		String countryCode = newUser.getCountryCode();
		String geoCodeEndpoint = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s,%s&appid=%s";						
		String url = String.format(geoCodeEndpoint, cityName, stateCode, countryCode, apiKey);
		
		//ex:http://api.openweathermap.org/geo/1.0/direct?q=Martinez,CA,US&appid=3c68fedb4d4cc2ee43ad218fedc95ec9
		String weather = "https://api.openweathermap.org/data/3.0/onecall?lat={lat}&lon={lon}&exclude={part}&appid={API key}";
		//Use geoCode's city name, state Code, country Code to get corresponing lat/lon for weather
		
	    RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        // Add headers, if needed
        // headers.set("Authorization", "Bearer your_token");

        HttpEntity<String> entity = new HttpEntity<>(headers);

       /* ResponseEntity<String> response = restTemplate.exchange(
            geoCode,
            HttpMethod.GET,
            entity,
            String.class
        );*/
		return null;
	
	}
	
}