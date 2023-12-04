package com.cst438.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer>{
	User findByName(String name);
	User findByEmail(String email);
	List<User> findAllByRole(String role);
}
