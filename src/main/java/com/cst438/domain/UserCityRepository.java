package com.cst438.domain;

import org.springframework.data.repository.CrudRepository;

public interface UserCityRepository extends CrudRepository <UserCity, Integer >{

	UserCity findByCity(String city);
}
