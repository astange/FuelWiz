package com.jas.fuelwiz;

import java.text.NumberFormat;

public class GasStation {
	private String price, distanceAway;
	private String lastUpdated;
	private String latitude, longitude;
	private String name, address, city, country, region, zipcode;
	private boolean useable;
	private double savings;
	private double difference;
	private String stationID;
	
	public GasStation(String country, String price, String address, String name, String city, String lastUpdated, String distanceAway, String latitude, String longitude, String state, String zip, String stationID){
		this.country = country;
		this.price = price;
		this.address = address;
		this.name = name;
		this.city = city;
		this.lastUpdated = lastUpdated;
		this.distanceAway = distanceAway;
		this.latitude = latitude;
		this.longitude = longitude;
		this.region = state;
		this.zipcode = zip;
		this.useable = true;
		this.stationID = stationID;
	}
	
	public String getStationID() {
		return stationID;
	}
	
	public String getDifferenceInSavingsFormatted() {
		NumberFormat asPrice = NumberFormat.getCurrencyInstance();
		return asPrice.format(getDifferenceInSavings());
	}
	
	public void setDifferenceInSavings(double diff) {
		difference = diff;
	}
	
	public double getDifferenceInSavings() {
		return difference;
	}
	
	public double getPriceAsDouble() {
		double notstring = 0;
		try {
			notstring = Double.parseDouble(price);
		} catch (NumberFormatException n) {
			notstring = 0;
		}
		return notstring;
	}
	
	public String getSavingsFormatted() {
		NumberFormat asprice = NumberFormat.getCurrencyInstance();
		//check to see if savings is not able to be calculated...
		if(savings == 0) {
			return "N/A";
		} else return asprice.format(savings);
		
	}
	
	public double getSavings() {
		return savings;
	}
	
	public void setSavings(double save) {
		savings = save;
	}
	
	public void setUsable(boolean use) {
		useable = use;
	}
	
	public boolean getUseable() {
		return useable;
	}
	
	public String getRegion() {
		return region;
	}
	
	public String getPrice(){
		return price;
	}
	
	public String getDistanceAway(){
		return distanceAway;
	}
	
	public String getLastUpdated(){
		return lastUpdated;
	}
	
	public String getName(){
		return name;
	}
	
	public String getAddress(){
		return address;
	}
	
	
	public String getCity(){
		return city;
	}
	
	public String getCountry(){
		return country;
	}
	
	public String getLatitude(){
		return latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}
	
	public String toDestination() {
		return address +", " + city + ", " + region + " " + zipcode;
	}
	
	public String toString() {
		return country + " " + city + " " + distanceAway + " " + price+ " " + lastUpdated+ " " + name + " "+ address;
	}
}
