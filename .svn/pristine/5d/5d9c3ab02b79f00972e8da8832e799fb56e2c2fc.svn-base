package com.jas.fuelwiz;



public class GoogleDistance implements Comparable<GoogleDistance>{
	
	private double distance;
	private String duration;
	private String destination;
	private String gDistance;
	private int durationSecs;
	private GasStation station;
	private double METERS_IN_MILE = 1609.344;

	public GoogleDistance(double distanceMeters, String gDistance, String duration, int durationSecs, String destination, GasStation station) {
		
		this.gDistance = gDistance;
		this.distance = distanceMeters;
		this.duration = duration;
		this.durationSecs = durationSecs;
		this.destination = destination;
		this.station = station;
	}
	
	public int getDurationSecs() {
		return durationSecs;
	}
	
	public GasStation getStation() {
		return station;
	}
	//google returns distance in meters
	public double getDistanceInMiles() {
		return (distance / METERS_IN_MILE);
	}
	
	public double getDistance() {
		return distance;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public String getGDistance() {
		return gDistance;
	}
		
	public String toString() {
		return "To go to the station at: " + destination + " you must drive: " + gDistance + " and it will take you: " + duration;
	}

	@Override
	public int compareTo(GoogleDistance another) {
		//check to see if total cost cannot be calculated because price is N/A
		if(this.getStation().getPrice().equals("N/A")) {
			return 1;
		} else {
		
		if(this.getStation().getSavings() > another.getStation().getSavings()) {
			return 1;
		} else if (this.getStation().getSavings() < another.getStation().getSavings()) {
			return -1;
		} else return 0;
		}
	}
}
