package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
		roleName = "uuid_generator-role",
		isPublishVersion = true,
		aliasName = "learn",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
@RuleEventSource(
		targetRule = "uuid_trigger"
)
@DependsOn(
		name = "uuid_trigger",
		resourceType = ResourceType.CLOUDWATCH_RULE
)
public class UuidGenerator implements RequestHandler<ScheduledEvent, Map<String, Object>> {

	private final AmazonS3 s3Client=AmazonS3Client.builder().withRegion("eu-central-1").build();
	private final String BUCKET_NAME = System.getenv("target_bucket");


	@Override
	public Map<String, Object> handleRequest(ScheduledEvent event, Context context) {
		String key = Instant.now().toString();
		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			uuids.add(UUID.randomUUID().toString());
		}
		String content = "{\n  \"ids\": [\n    \"" + String.join("\",\n    \"", uuids) + "\"\n  ]\n}";
		File file = new File("/tmp/AWS.txt");
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(content);
		} catch (IOException e) {
			e.printStackTrace();
			Map<String, Object> errorResult = new HashMap<>();
			errorResult.put("statusCode", 500);
			errorResult.put("body", "Error writing to file: " + e.getMessage());
			return errorResult;
		}
		s3Client.putObject(BUCKET_NAME,key,file);
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 200);
		resultMap.put("body", "UUIDs generated and stored in S3 bucket");

		return resultMap;
	}
}