package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task08.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
	private static final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
	private final WeatherService weatherService;
	private final ObjectMapper objectMapper;

	public ApiHandler() {
		this.weatherService = new WeatherService();
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("Access-Control-Allow-Origin", "*");

		try {
			// Default coordinates for Kiev
			double latitude = 50.4375;
			double longitude = 30.5;

			// Check for query parameters
			if (request.containsKey("queryStringParameters") && request.get("queryStringParameters") != null) {
				@SuppressWarnings("unchecked")
				Map<String, String> queryParams = (Map<String, String>) request.get("queryStringParameters");

				if (queryParams.containsKey("latitude")) {
					latitude = Double.parseDouble(queryParams.get("latitude"));
				}
				if (queryParams.containsKey("longitude")) {
					longitude = Double.parseDouble(queryParams.get("longitude"));
				}
			}

			Map<String, Object> weatherData = weatherService.getWeatherForecast(latitude, longitude);

			response.put("statusCode", 200);
			response.put("headers", headers);
			response.put("body", objectMapper.writeValueAsString(weatherData));

		} catch (Exception e) {
			logger.error("Error processing request: ", e);

			Map<String, String> errorBody = new HashMap<>();
			errorBody.put("error", "Failed to fetch weather data");
			errorBody.put("message", e.getMessage());

			response.put("statusCode", 500);
			response.put("headers", headers);
			try {
				response.put("body", objectMapper.writeValueAsString(errorBody));
			} catch (Exception ex) {
				response.put("body", "{\"error\":\"Internal server error\"}");
			}
		}

		return response;
	}
}