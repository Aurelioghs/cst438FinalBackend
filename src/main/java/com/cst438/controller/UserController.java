package com.cst438.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

	// Methods to add a user and get a user by email
	
	// Adds a student
		@PostMapping("/user")
		@Transactional
		public User addUsesr( @RequestBody UserDTO userDto ) {
			User newUser = new User();
			newUser.setName(userDto.name());
			newUser.setEmail(userDto.email());
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			newUser.setPassword(encoder.encode(userDto.password()));
			newUser.setRole(userDto.role());
			newUser.setCity(userDto.city());
			newUser.setState_code(userDto.state_code());
			newUser.setCountry_code(userDto.country_code());
			return userRepository.save(newUser);
		}
	
	// Get User By Email
		@GetMapping("/user/{email}")
		public User getUserByEmail(@PathVariable String email) {
			User user = userRepository.findByEmail(email);
		    return user;
		}
}
