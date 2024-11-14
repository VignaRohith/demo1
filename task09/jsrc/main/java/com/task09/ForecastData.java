package com.task09;




import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)

public class ForecastData {
    private Number elevation;
    private Number generationtime_ms;
    private HourlyUnits hourly_units;
    private HourlyData hourly;
    private Number latitude;
    private Number longitude;
    private String timezone;
    private String timezone_abbreviation;
    private String utc_offset_seconds;
}
