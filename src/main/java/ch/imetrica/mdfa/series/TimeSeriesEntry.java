package ch.imetrica.mdfa.series;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

/**
 * The time series entry with generic value V and
 * a string as the timeStamp which is typically in the form
 * of a standard DataTimeFormatter, for example
 * "yyyy-MM-dd HH:mm:ss"
 * "dd-MM-yyyy" 
 * 
 * @author Christian D. Blakely (clisztian@gmail.com)
 *
 * @param <V>
 */

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