package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
		lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = true,
		aliasName = "${lambdas_alias_name}",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

	private static final String DYNAMO_DB_TABLE_NAME = "Events"; // DynamoDB table name
	private final AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
	private final ObjectMapper objectMapper = new ObjectMapper(); // To convert Java objects to JSON and vice versa

	@Override
	public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
		// Extract 'principalId' and 'content' from the incoming request
		Map<String, Object> requestBody = (Map<String, Object>) input.get("body");
		Integer principalId = (Integer) requestBody.get("principalId");
		Map<String, String> content = (Map<String, String>) requestBody.get("content");

		if (principalId == null || content == null) {
			return createErrorResponse(400, "Missing required fields: principalId or content");
		}

		// Create an event
		String eventId = UUID.randomUUID().toString();
		String createdAt = Instant.now().toString(); // ISO 8601 formatted date

		// Event structure to be stored in DynamoDB
		Item eventItem = new Item()
				.withPrimaryKey("id", eventId)
				.withInt("principalId", principalId)
				.withString("createdAt", createdAt)
				.withMap("body", content);

		// Save the event to DynamoDB
		try {
			putEventInDynamoDB(eventItem);
		} catch (Exception e) {
			return createErrorResponse(500, "Failed to save event: " + e.getMessage());
		}

		// Return the created event as the response (statusCode should be 201)
		return createSuccessResponse(201, eventItem);
	}

	private void putEventInDynamoDB(Item eventItem) {
		DynamoDB dynamoDb = new DynamoDB(dynamoDBClient);
		Table table = dynamoDb.getTable(DYNAMO_DB_TABLE_NAME);
		table.putItem(eventItem);
	}

	// Utility method to create error response with status code and message
	private Map<String, Object> createErrorResponse(int statusCode, String message) {
		Map<String, Object> response = new HashMap<>();
		response.put("statusCode", statusCode);
		response.put("headers", Map.of("Content-Type", "application/json"));
		response.put("body", "{\"error\": \"" + message + "\"}");
		return response;
	}

	// Utility method to create success response with status code and event data
	private Map<String, Object> createSuccessResponse(int statusCode, Item eventItem) {
		Map<String, Object> response = new HashMap<>();
		response.put("statusCode", statusCode);
		response.put("headers", Map.of("Content-Type", "application/json"));

		// Stringify the event JSON before placing it in the body
		try {
			String eventJson = objectMapper.writeValueAsString(eventItem.asMap());
			response.put("body", eventJson);
		} catch (Exception e) {
			response.put("body", "{\"error\": \"Failed to serialize event\"}");
		}
		return response;
	}
}
