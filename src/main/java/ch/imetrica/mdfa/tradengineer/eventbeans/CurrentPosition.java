package ch.imetrica.mdfa.tradengineer.eventbeans;

public class CurrentPosition {

	
	public enum Position { 
		LONG,
		SHORT,
		NEUTRAL;
	}
	
	Position currentPosition;
	
	public CurrentPosition(Position current) {
		this.currentPosition = current;
	}
	
	public Position getCurrentPosition() {
		return currentPosition;
	}
	
	public void setCurrentPosition(Position current) {
		this.currentPosition = current;
	}
	
	public String toString() {
        return "Current Position: " + currentPosition.toString();
    }
	
	
	
}
