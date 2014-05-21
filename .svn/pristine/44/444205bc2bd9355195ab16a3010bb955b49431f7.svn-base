package com.jas.fuelwiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WaypointCalculator {

	public static String MAPS_URL = "http://maps.googleapis.com/maps/api/directions/json?origin=";
	static int[] results;

	public static int[] getDrivingDistance(String startAddr, GasStation station, String destinationAddr) {
		//uses google directions api, station address as waypoint.
		String stationAddr = station.getAddress();
		
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpPost = new HttpGet(createURL(startAddr, stationAddr, destinationAddr));

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			
			InputStream is = httpEntity.getContent();
			Scanner scan = new Scanner(is);
			String fromInternet = "";
			while(scan.hasNext()) {
				fromInternet += scan.next();
				fromInternet += " ";
			}
			is.close();
			//parsing json from google
			JSONObject json = new JSONObject(fromInternet);
			System.out.println(fromInternet);
			JSONArray routes = json.getJSONArray("routes");
			
			JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
			//from start to station
			int start_Station_Distance = legs.getJSONObject(0).getJSONObject("distance").getInt("value");
			int start_Station_Duration = legs.getJSONObject(0).getJSONObject("duration").getInt("value");
			//from station to destination
			int station_Destination_Distance = legs.getJSONObject(1).getJSONObject("distance").getInt("value");
			int station_Destination_Duration = legs.getJSONObject(0).getJSONObject("duration").getInt("value");
			
			//get total distance and duration.
			int totalDistanceMeters = start_Station_Distance + station_Destination_Distance;
			int totalDurationSecs = start_Station_Duration + station_Destination_Duration;
			
			results = new int[2];
			results[0] = totalDistanceMeters;
			results[1] = totalDurationSecs;
						
		} catch (JSONException j) {
			j.printStackTrace();
			return null;
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return results;
	}
	
	private static String createURL(String startAddr, String stationAddr, String destinationAddr) {
		destinationAddr = destinationAddr.trim().replace(" ", "+");
		stationAddr = stationAddr.trim().replace(" ", "+");
		String url = MAPS_URL + startAddr + "&destination=" + destinationAddr + "&waypoints=" + stationAddr + "&sensor=false";
		
		return url;
		
	}
	
	
}
