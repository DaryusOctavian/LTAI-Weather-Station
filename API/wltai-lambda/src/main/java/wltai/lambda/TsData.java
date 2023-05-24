package wltai.lambda;

import java.time.OffsetDateTime;

import com.amazonaws.services.timestreamwrite.model.Record;

public record TsData(OffsetDateTime time, double value, TsDataType dataType) {

	public Record asTsRecord() {
		Record reco = new Record();
		reco.setMeasureValue(Double.toString(value));
		reco.setMeasureValueType("DOUBLE");
		reco.setMeasureName(dataType.toString());
		reco.setTime(Long.toString(time.toInstant().toEpochMilli()));
		reco.setTimeUnit("MILLISECONDS");
		return reco;
	}

}
