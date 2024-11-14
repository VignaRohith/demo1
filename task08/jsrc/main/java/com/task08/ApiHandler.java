package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.JsonObject;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import org.epam.openapi.App;

import java.io.IOException;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		layers = "sdk-layer",
		aliasName = "learn",
		runtime = DeploymentRuntime.JAVA11,
		architecture = Architecture.ARM64,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "sdk-layer",
		libraries = {"lib/open-meteo-api-1.0-SNAPSHOT.jar"},
		runtime = DeploymentRuntime.JAVA11,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Object, String> {

	public String handleRequest(Object request, Context context) {
		// Define latitude and longitude (can be parameterized based on incoming request)
		App client = new App(50.4375, 30.5); // Using Kiev's coordinates
		JsonObject weatherData = null;

		try {
			// Fetch weather data
			weatherData = client.getWeatherForecast();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Return the weather data in JSON format as a string
		return weatherData.toString();
	}
}
