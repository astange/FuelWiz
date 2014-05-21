package com.jas.fuelwiz;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import android.os.StrictMode;

public class GasStationRequest {
	private static String slash = "/";
	private static InputStream is;
	private static JSONObject jObj = null;
	private static String json = "";

	public static GasStation[] getGasStationsDistance(String latitude, String longitude, 
			String radius, String fuelType, String sortBy){
		GasStation[] stations = null;
		String urlString = "http://api.mygasfeed.com/stations/radius/" + latitude + slash + longitude + slash
				+ radius + slash + fuelType + slash + sortBy + slash + "ytc0ljswtd.json?callback=?";

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		try{ 
			try {
				// defaultHttpClient
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpPost = new HttpGet(urlString);

				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				is = httpEntity.getContent();

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				json = sb.toString();
				json = json.substring(2, sb.length() - 2);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// try parse the string to a JSON object
			try {
				jObj = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			JSONArray Stations = jObj.getJSONArray("stations");
			//check to see if too many stations

			int numberOfStations = Stations.length();
			if(Stations.length() > GoogleDistanceMatrixRequest.MAX_NUMBER_OF_STATIONS) {
				numberOfStations = GoogleDistanceMatrixRequest.MAX_NUMBER_OF_STATIONS;
			}
			//initialize array of gasstations
			GasStation[] gasStations = new GasStation[numberOfStations];

			for(int i = 0; i < gasStations.length; i++) {
				gasStations[i] = new GasStation(Stations.getJSONObject(i).getString("country").trim(), 
						Stations.getJSONObject(i).getString("price").trim(), 
						Stations.getJSONObject(i).getString("address").trim(),
						Stations.getJSONObject(i).getString("station").trim(),
						Stations.getJSONObject(i).getString("city").trim(),
						Stations.getJSONObject(i).getString("date").trim(),
						Stations.getJSONObject(i).getString("distance").trim(),
						Stations.getJSONObject(i).getString("lat").trim(),
						Stations.getJSONObject(i).getString("lng").trim(),
						Stations.getJSONObject(i).getString("region").trim(),
						Stations.getJSONObject(i).getString("zip").trim(),
						Stations.getJSONObject(i).getString("id").trim());
			}

			//if price doesn't exist take out
			int numberOfStationsWithPrice = 0;
			for(int i = 0; i < gasStations.length; i++) {
				try {
					Double.parseDouble(gasStations[i].getPrice());
					numberOfStationsWithPrice++;
				}
				catch(NumberFormatException n) {
					gasStations[i].setUsable(false);
				}
			}


			//if preference is checked to show all stations...
			if(MainActivity.savedinputs.getBoolean("prefShowAllStations", false)) {
				numberOfStationsWithPrice = gasStations.length;
				for(int i = 0; i < gasStations.length; i++) {
					gasStations[i].setUsable(true);
				}
			}

			//copy values into new array of smaller size, throwing out unusable stations (no price shown)
			GasStation[] copyOfStations = new GasStation[numberOfStationsWithPrice];
			int j = 0;
			for(int i = 0; i < gasStations.length; i++) {
				if(gasStations[i].getUseable()) {
					copyOfStations[j] = gasStations[i];
					j++;
				}
			}

			stations = copyOfStations;

		}
		catch(JSONException je){
			je.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return stations;
	}
}
