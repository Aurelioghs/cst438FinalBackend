package com.cst438.domain;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long>{
	User findByName(String name);
	User findByEmail(String email);
}
