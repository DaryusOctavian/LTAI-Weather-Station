package wltai.lambda;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMarshalling {

	@Test
	public void testDataMarshaling() throws JsonProcessingException {
		OffsetDateTime now = OffsetDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
		OffsetDateTime offdata = now.truncatedTo(ChronoUnit.MILLIS);
		var tsData1 = new TsData(offdata, 20.0, TsDataType.Temperature);
		var tsData2 = new TsData(offdata, 80.0, TsDataType.Humidity);
		var tsData3 = new TsData(offdata, 10.2, TsDataType.Wind);
		var tsData4 = new TsData(offdata, 10.2, TsDataType.AirPressure);
		List<TsData> expectedData = new ArrayList<>();
		expectedData.add(tsData1);
		expectedData.add(tsData2);
		expectedData.add(tsData3);
		expectedData.add(tsData4);

		ObjectMapper mapper = Handler.buildMapper();
		var json = mapper.writer(new DefaultPrettyPrinter()).writeValueAsString(expectedData);
		System.out.println(json);
		List<TsData> actualData = mapper.readValue(json, new TypeReference<List<TsData>>() {
		});
		assertEquals(expectedData, actualData);
	}

	@Test
	public void deserialize() throws JsonMappingException, JsonProcessingException {
		var json = """
				[
				{
				    "time": "2023-05-01T09:04:11.985270+03:00",
				    "value": 22.6,
				    "dataType": "temp"
				}
				,
				{
				    "time": "2023-05-01T09:04:11.985270+03:00",
				    "value": 88.6,
				    "dataType": "humidity"
				}
				,
				{
				    "time": "2023-05-01T09:04:11.985270+03:00",
				    "value": 17.6,
				    "dataType": "wind"
				}
				,
				{
				    "time": "2023-05-01T09:04:11.985270+03:00",
				    "value": 90.6,
				    "dataType": "pressure"
				}
				]
				""";
		ObjectMapper mapper = Handler.buildMapper();
		List<TsData> dataPoints = mapper.readValue(json, new TypeReference<List<TsData>>() {
		});
		System.out.println(dataPoints);
	}
}
