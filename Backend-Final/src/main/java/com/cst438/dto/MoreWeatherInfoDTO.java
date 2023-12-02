package com.cst438.dto;

public record MoreWeatherInfoDTO(int tempF,int tempC,String desc,double windSpeedMph,double windSpeedKph,int feelsLikeF,int feelsLikeC, 
								 int humidity, int cloudsPercent, double visibilityMiles,double visibilityKiloM, String sunriseTime, String sunsetTime) {

}
