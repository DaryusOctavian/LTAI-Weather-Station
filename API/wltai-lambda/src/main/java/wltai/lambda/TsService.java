package wltai.lambda;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.timestreamquery.AmazonTimestreamQueryClientBuilder;
import com.amazonaws.services.timestreamquery.model.Datum;
import com.amazonaws.services.timestreamquery.model.QueryRequest;
import com.amazonaws.services.timestreamquery.model.Row;
import com.amazonaws.services.timestreamwrite.AmazonTimestreamWriteClientBuilder;
import com.amazonaws.services.timestreamwrite.model.Dimension;
import com.amazonaws.services.timestreamwrite.model.Record;
import com.amazonaws.services.timestreamwrite.model.WriteRecordsRequest;

public class TsService {
	public static final String LATEST = """
			  SELECT  measure_name, max_by(measure_value::double, time) AS val, max(to_milliseconds(time)) as latest_time
			  FROM LtaiWeather.WeatherData
			  WHERE stage = '%s'  AND time >= ago(10d)
			  GROUP BY measure_name
			""";

	public static final String HIST = """
				SELECT to_milliseconds(BIN(time, %s)) AS b_time, measure_name,  ROUND(AVG(measure_value::double), 2) AS val
				FROM LtaiWeather.WeatherData
				WHERE stage = '%s'	AND time > ago(%s)
				GROUP BY to_milliseconds(BIN(time, %s)), measure_name
				ORDER BY b_time, measure_name ASC
			""";

	public TsService() {

	}

	public int storeData(List<TsData> data, String stage) {
		WriteRecordsRequest req = new WriteRecordsRequest();
		var dim = new Dimension();
		dim.setName("stage");
		dim.setValue(stage);
		dim.setDimensionValueType("VARCHAR");
		Record commRec = new Record();
		commRec.setDimensions(Collections.singleton(dim));
		req.setDatabaseName(System.getenv("TS_DB"));
		req.setTableName(System.getenv("TS_TBL"));
		req.setCommonAttributes(commRec);
		var records = data.stream().map(TsData::asTsRecord).collect(Collectors.toList());
		req.setRecords(records);
		var wrClient = AmazonTimestreamWriteClientBuilder.defaultClient();
		var res = wrClient.writeRecords(req);
		return res.getRecordsIngested().getTotal();
	}

	public Map<String, TsData> latestData(String stage) {
		var qrClient = AmazonTimestreamQueryClientBuilder.defaultClient();
		QueryRequest pqr = new QueryRequest();
		pqr.setQueryString(String.format(LATEST, stage));
		System.out.println(pqr.getQueryString());
		var qr = qrClient.query(pqr);
		var res = new HashMap<String, TsData>();
		for (Row row : qr.getRows()) {
			List<Datum> data = row.getData();
			double value = Double.parseDouble(data.get(1).getScalarValue());
			TsDataType dataType = TsDataType.fromString(data.get(0).getScalarValue());
			long millis = Long.parseLong(data.get(2).getScalarValue());
			var ts = OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
			res.put(data.get(0).getScalarValue(), new TsData(ts, value, dataType));
		}
		return res;
	}

	public ArrayList<TsData> historicData(String stage, int hoursGroup, int days) {
		var qrClient = AmazonTimestreamQueryClientBuilder.defaultClient();
		hoursGroup = Math.max(hoursGroup, 1);
		days = Math.min(Math.max(days, 1), 60);
		QueryRequest pqr = new QueryRequest();
		pqr.setQueryString(String.format(HIST, hoursGroup + "h", stage, days + "d", hoursGroup + "h"));
		System.out.println(pqr.getQueryString());
		var qr = qrClient.query(pqr);
		var res = new ArrayList<TsData>();
		for (Row row : qr.getRows()) {
			List<Datum> data = row.getData();
			long millis = Long.parseLong(data.get(0).getScalarValue());
			var ts = OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
			double value = Double.parseDouble(data.get(2).getScalarValue());
			TsDataType dataType = TsDataType.fromString(data.get(1).getScalarValue());
			res.add(new TsData(ts, value, dataType));
		}
		Collections.sort(res, Comparator.comparing(TsData::time).thenComparing(Comparator.comparing(TsData::dataType)));
		return res;
	}

}
