package ch.imetrica.mdfatrader.series;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Data
@ToString(includeFieldNames=false)
public class TimeSeriesEntry<V> {
	@Getter
	private final String timeStamp;
	@Getter
	private final V value;
	
	public String getDateTime() {
		return timeStamp;
	}
}