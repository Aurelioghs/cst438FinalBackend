package com.cst438.domain;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository <User, Integer >{
	
	User findByEmail(String email);
	
	User[] findByNameStartsWith(String name);
	
	User findByName(String name);
	
	User findById(int user_id);
	
}
