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
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 400);
			errorResponse.put("body", "Missing required fields: principalId or content");
			return errorResponse;
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
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("statusCode", 500);
			errorResponse.put("body", "Failed to save event: " + e.getMessage());
			return errorResponse;
		}

		// Return the created event as the response
		Map<String, Object> successResponse = new HashMap<>();
		successResponse.put("statusCode", 201);
		successResponse.put("event", eventItem.asMap()); // Return the full event object
		return successResponse;
	}

	private void putEventInDynamoDB(Item eventItem) {
		DynamoDB dynamoDb = new DynamoDB(dynamoDBClient);
		Table table = dynamoDb.getTable(DYNAMO_DB_TABLE_NAME);
		table.putItem(eventItem);
	}
}