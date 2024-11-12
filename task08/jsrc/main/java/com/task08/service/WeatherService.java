package com.task08.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private static final String BASE_URL = "https://api.open-meteo.com/v1";
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WeatherService() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> getWeatherForecast(double latitude, double longitude) throws IOException {
        String url = String.format("%s/forecast?latitude=%f&longitude=%f&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m&current=temperature_2m,wind_speed_10m",
                BASE_URL, latitude, longitude);

        HttpGet request = new HttpGet(url);

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());

            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("Error fetching weather data: {}", jsonResponse);
                throw new IOException("Failed to fetch weather data: " + jsonResponse);
            }

            return objectMapper.readValue(jsonResponse, Map.class);
        } catch (Exception e) {
            logger.error("Error in weather service: ", e);
            throw new IOException("Weather service error: " + e.getMessage(), e);
        }
    }
}