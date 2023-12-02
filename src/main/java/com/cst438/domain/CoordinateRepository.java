package com.cst438.domain;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CoordinateRepository extends CrudRepository <Coordinate, Integer>{
	@Query("SELECT c FROM Coordinate c WHERE c.user.user_id = :userId")
	List<Coordinate> findAllByUser(@Param("userId") int userId);
}
