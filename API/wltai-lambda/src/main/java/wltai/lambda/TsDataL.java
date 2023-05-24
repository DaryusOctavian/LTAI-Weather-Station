package wltai.lambda;

import com.amazonaws.services.timestreamwrite.model.Record;

public record TsDataL(long timeStamp, double value, TsDataType dataType) {

	public Record asTsRecord() {
		Record reco = new Record();
		reco.setMeasureValue(Double.toString(value));
		reco.setMeasureValueType("DOUBLE");
		reco.setMeasureName(dataType.toString());
		reco.setTime(Long.toString(timeStamp));
		reco.setTimeUnit("MILLISECONDS");
		return reco;
	}

}
