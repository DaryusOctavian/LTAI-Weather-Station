
package wltai.lambda;

import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.timestreamwrite.model.RejectedRecord;
import com.amazonaws.services.timestreamwrite.model.RejectedRecordsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayV2HTTPResponse> {

	@Override
	public APIGatewayV2HTTPResponse handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("EVENT TYPE: " + event.toString());
		var meth = event.getRequestContext().getHttpMethod();
		if (meth.equals("POST")) {
			var key = event.getHeaders().get("x-api-key");
			if (!System.getenv("STORE_API_KEY").equals(key)) {
				logger.log("Invalid Api key " + key);
				APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
				response.setIsBase64Encoded(false);
				response.setStatusCode(401);
				return response;
			}
			String reqBody = event.getBody();
			ObjectMapper mapper = buildMapper();
			try {
				List<TsData> dataPoints = mapper.readValue(reqBody, new TypeReference<List<TsData>>() {
				});
				return storeData(dataPoints, event.getRequestContext().getStage(), logger);
			} catch (JsonProcessingException jsonEx) {
				logger.log("Failed to parse " + reqBody + ". Reason: " + jsonEx.toString());
				APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
				response.setStatusCode(400);
				return response;
			}

		}
		if (meth.equals("GET")) {
			var stage = event.getRequestContext().getStage();
			var path = event.getRequestContext().getPath();
			String expected_latest = String.format("/%s/weather/latest", stage);
			if (expected_latest.equals(path)) {
				return readLatest(stage, logger);
			}
			String expected_hist = String.format("/%s/weather/hist", stage);
			if (expected_hist.equals(path)) {
				var params = event.getQueryStringParameters();
				int days = 10;
				try {
					days = Integer.parseInt(params.getOrDefault("days", "10"));
				} catch (Exception e) {
					APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
					response.setStatusCode(400);
					return response;
				}
				int hoursGroup = 1;
				try {
					hoursGroup = Integer.parseInt(params.getOrDefault("hgroup", "1"));
				} catch (Exception e) {
					APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
					response.setStatusCode(400);
					return response;
				}
				return readHistoricalData(stage, hoursGroup, days, logger);
			}
		}

		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		response.setStatusCode(404);
		return response;
	}

	private APIGatewayV2HTTPResponse readLatest(String stage, LambdaLogger log) {
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		TsService tss = new TsService();
		try {
			var res = tss.latestData(stage);
			response.setStatusCode(200);
			response.setIsBase64Encoded(false);
			HashMap<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			response.setHeaders(headers);
			var mapper = buildMapper();
			var jsonBody = mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(res);
			response.setBody(jsonBody);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(ex.getMessage());
			response.setStatusCode(400);
		}
		return response;
	}

	private APIGatewayV2HTTPResponse storeData(List<TsData> dataPoints, String stage, LambdaLogger log) {
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		TsService tss = new TsService();
		try {
			int res = tss.storeData(dataPoints, stage);
			log.log("Records stored " + res);
			response.setStatusCode(200);
		} catch (RejectedRecordsException rre) {
			log.log(rre.getMessage());
			for (RejectedRecord rec : rre.getRejectedRecords()) {
				log.log(String.format("%d %s", rec.getRecordIndex(), rec.getReason()));
			}
			response.setStatusCode(400);
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(ex.getMessage());
			response.setStatusCode(400);
		}

		return response;
	}

	private APIGatewayV2HTTPResponse readHistoricalData(String stage, int hoursGroup, int days, LambdaLogger log) {
		APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
		TsService tss = new TsService();
		try {
			var res = tss.historicData(stage, hoursGroup, days);
			HashMap<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			response.setHeaders(headers);
			var mapper = buildMapper();
			var jsonBody = mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(res);
			response.setBody(jsonBody);
			response.setStatusCode(200);
			log.log("Response sent");
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(ex.getMessage());
			response.setStatusCode(400);
		}

		return response;

	}

	static ObjectMapper buildMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.findAndRegisterModules();
		// this should not be here but looks like aws lambda has classpath issues
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
		mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
		mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
		return mapper;
	}
}