package com.cst438.domain;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserCityRepository extends CrudRepository <UserCity, Integer >{
	 @Query("SELECT c FROM UserCity c WHERE c.user.user_id = :userId")
	 List<UserCity> findByUserId(@Param("userId") int userId);
	 List<UserCity> findByCity(String city);
}