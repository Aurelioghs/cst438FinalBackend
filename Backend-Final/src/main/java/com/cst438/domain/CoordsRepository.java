package com.cst438.domain;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CoordsRepository extends CrudRepository<Coords, Long>{
	  Coords findByUserId(int userId);
}
