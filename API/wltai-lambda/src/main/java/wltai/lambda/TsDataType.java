package wltai.lambda;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TsDataType {
	@JsonProperty("temp")
	Temperature,
	@JsonProperty("humidity")
	Humidity,
	@JsonProperty("wind")
	Wind,
	@JsonProperty("pressure")
	AirPressure;

	@Override
	public String toString() {
		return switch (this) {
		case AirPressure -> "pressure";
		case Humidity -> "humidity";
		case Temperature -> "temp";
		case Wind -> "wind";
		default -> throw new AssertionError();
		};
	}

	public static TsDataType fromString(String desc) {
		if (desc.equalsIgnoreCase("pressure")) {
			return AirPressure;
		}
		if (desc.equalsIgnoreCase("humidity")) {
			return Humidity;
		}
		if (desc.equalsIgnoreCase("temp")) {
			return Temperature;
		}
		if (desc.equalsIgnoreCase("wind")) {
			return Wind;
		}
		throw new IllegalArgumentException(desc);
	}
}
