package ch.imetrica.mdfa.market;

public class Trade {

	private int signal_number;
	private String timestamp;
	private double volume;
	private double price;
	
	public Trade(String timestamp, int signal_number, double volume, double price) {
		
		this.signal_number = signal_number;
		this.timestamp = timestamp;
		this.volume = volume;
		this.price = price;
	}
	
	public int getSignal_number() {
		return signal_number;
	}
	public void setSignal_number(int signal_number) {
		this.signal_number = signal_number;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
	@Override
	public String toString() {		
		return "Signal-" + signal_number + ", vol: " + volume + ", price " + price;
	}
	
	
}
