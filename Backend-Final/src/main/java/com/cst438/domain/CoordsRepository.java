package com.cst438.domain;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CoordsRepository extends CrudRepository<Coords, Integer>{
	 @Query("SELECT c FROM Coords c WHERE c.user.user_id = :userId")
	    Coords findByUserId(@Param("userId") int userId);
}
