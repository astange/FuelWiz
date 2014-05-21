package com.jas.fuelwiz;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends Activity{
	//define buttons in xml
	EditText editMPG, gallonsToFill;
	static Button bCompute, bWaypointCalc;
	TextView tGallonstoFill, tSeekRadius;
	TabHost gasGradeTabs;
	SeekBar seekRadius;
	public static CheckBox checkHighways, checkTolls; 
	CheckBox checkTwoWay; 
	ToggleButton tbManual;

	public static String longitude;
	public static String latitude;

	private double gallons, mpg;
	public static int radius;
	public static GoogleDistance[] ReturnedDrivingDistances;

	public static SharedPreferences savedinputs;
	TabHost tabHost;

	//retry on fail variable cap
	public int numberOfTries = 0;
	//meters in a mile for calculations...
	private double METERS_IN_MILE = 1609.344;

	boolean isNewLocation = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		savedinputs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

		if(!LicenseCheck.valid){
			finish();
		}

		//get pointer to button in gui
		editMPG = (EditText) findViewById(R.id.editMPG);
		gallonsToFill = (EditText) findViewById(R.id.editGallons);
		bCompute = (Button) findViewById(R.id.bCompute);
		tGallonstoFill = (TextView) findViewById(R.id.tvGallons);
		tSeekRadius = (TextView) findViewById(R.id.tseekRadius);
		checkHighways = (CheckBox) findViewById(R.id.checkHighWays);
		checkTolls = (CheckBox) findViewById(R.id.checkTolls);
		checkTwoWay = (CheckBox) findViewById(R.id.checkTwoWayTrip);
		tbManual = (ToggleButton) findViewById(R.id.tBManualLocation);
		//waypoint button...
		bWaypointCalc = (Button) findViewById(R.id.bWaypoint);
		bWaypointCalc.setVisibility(View.GONE);

		//set up clicker for type of gas
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		tabHost.setup();
		tabHost.addTab(tabHost.newTabSpec("reg").setIndicator("Regular").setContent(R.id.tabReg));
		tabHost.addTab(tabHost.newTabSpec("mid").setIndicator("Midgrade").setContent(R.id.tabMid));
		tabHost.addTab(tabHost.newTabSpec("pre").setIndicator("Premium").setContent(R.id.tabPre));
		tabHost.addTab(tabHost.newTabSpec("diesel").setIndicator("Diesel").setContent(R.id.tabDiesel));
		for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.WHITE);
		}

		seekRadius = (SeekBar) findViewById(R.id.seekRadius);
		seekRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				radius = progress/10;
				if(radius == 0) radius = 1;
				tSeekRadius.setText("Radius: " + radius + " mi");
			}
		});

		//get shared prefs that were saved
		editMPG.setText(savedinputs.getString("mpg", "25"));
		gallonsToFill.setText(savedinputs.getString("gallons", "15"));
		checkHighways.setChecked(savedinputs.getBoolean("highway", false));
		checkTolls.setChecked(savedinputs.getBoolean("toll", false));
		checkTwoWay.setChecked(savedinputs.getBoolean("twoway", true));
		seekRadius.setProgress(savedinputs.getInt("radius", 3) * 10);
		tabHost.setCurrentTabByTag(savedinputs.getString("gasGrade", "reg"));
		tbManual.setChecked(savedinputs.getBoolean("isManualLocationChecked", false));

		//IF IT CONTAINS so savedinputs.contains() all 7 prefs so && operator then you go straight to the listview. (skip button click)
		//progress for location.
		tbManual.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
					final EditText eLocation = new EditText(getApplicationContext());
					eLocation.setFocusable(true);
					eLocation.setTextColor(Color.BLACK);
					eLocation.setText(savedinputs.getString("manualLocation", ""));
					build.setCancelable(false);
					build.setTitle("Enter manual location");
					final SharedPreferences.Editor editor = savedinputs.edit();
					build.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							editor.putString("manualLocation", eLocation.getText().toString());
							editor.commit();
							tbManual.setChecked(true);							
						}
					});
					build.setNegativeButton("Clear", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							editor.remove("manualLocation");
							editor.commit();
							tbManual.setChecked(false);
						}
					});
					build.setView(eLocation);
					build.create().show();
				} else {
					//recheck for location enabled, otherwise will crash.
					LocationManager locm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					if(!locm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
						locationAccessAlert();
					}
				}
			}
		});

		//check for location access via gps.
		LocationManager locm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if(!locm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			locationAccessAlert();
		} else {	
			if(!tbManual.isChecked()) { 
				ProgressDialog progressLocation = new ProgressDialog(MainActivity.this);
				progressLocation.setTitle("Executing...");
				progressLocation.setMessage("Auto-gathering last known GPS location data...");
				progressLocation.show();

				getLocationViaGPS();
				progressLocation.dismiss();
			}
		}

		//*************************************************************************************************************
		//get users location, either manually or via gps. this is in oncreate. then every time that you click use manual, it gets location again.	
		bCompute.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				parseUserInputstoDouble();
				LocationManager locm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				if(!locm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
					locationAccessAlert();
				}
				//get location, then do everything else.
				GetLocationAsync getlocat = new GetLocationAsync();
				getlocat.execute();
				
				doEverythingTask doall = new doEverythingTask();
				doall.execute();

				if(doall.isCancelled()) {
					if(numberOfTries < 3) {
						bCompute.performClick();
						numberOfTries++;
						Toast.makeText(getApplicationContext(), "Connection error, auto retry #" + numberOfTries + " of 3", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "Connection error, please try again.", Toast.LENGTH_SHORT).show();
						numberOfTries = 0;
					}
				}
				savePrefs();
			}
		});

		bWaypointCalc.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				alertWaypointDestination();
			}
		});
	}

	private void alertWaypointDestination() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Use Station as Waypoint");
		//set up views.
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);

		TextView message = new TextView(this);
		message.setText("Enter Destination Address:");
		message.setWidth(240);
		message.setTextSize(20);
		message.setPadding(10, 5, 5, 10);
		message.setGravity(Gravity.CENTER_HORIZONTAL);
		ll.addView(message);

		final EditText editDestination = new EditText(this);
		editDestination.setText(savedinputs.getString("destaddr", ""));
		editDestination.setWidth(240);
		editDestination.setGravity(Gravity.FILL_HORIZONTAL);
		editDestination.setPadding(5, 5, 5, 0);
		ll.addView(editDestination);

		alert.setView(ll);

		alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				savedinputs.edit().putString("destaddr", editDestination.getText().toString()).commit();
				//waypoint calculator stuff.
				waypointAsyncTask getwaypoints = new waypointAsyncTask();
				getwaypoints.execute(editDestination.getText().toString());
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				savedinputs.edit().putString("destaddr", editDestination.getText().toString()).commit();
				dialog.dismiss();
			}
		});

		alert.create().show();
	}


	private void parseUserInputstoDouble() {
		try {
			mpg = Double.parseDouble(editMPG.getText().toString().trim());
			gallons = Double.parseDouble(gallonsToFill.getText().toString().trim());
		} catch (NumberFormatException e) {
			mpg = 10;
			gallons = 0;
		}
	}

	private void getLocationViaGeoCode() {
		Geocoder geocoder = new Geocoder(MainActivity.this);  
		try {
			List<Address> addresses;
			addresses = geocoder.getFromLocationName(savedinputs.getString("manualLocation", "New York City, NY"), 1);
			if(addresses.size() > 0) {
				latitude = "" + addresses.get(0).getLatitude();
				longitude = "" + addresses.get(0).getLongitude();
				
				isNewLocation = true;
			}
		} catch (IOException e) {

			e.printStackTrace();
		}		
	}

	private void getLocationViaGPS() {

		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//	AlertDialog.Builder dialog;
		try{
			//if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
			if(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
				LocationListener locationListener = new MyLocationListener();  
				Looper loop = Looper.getMainLooper();

				//ask if they want to use manual location from before (if it exists)
				lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, loop);

			} else {
				return;
			}

		}catch(Exception ex){
			ex.printStackTrace();
		}

	}

	@Override
	public void onBackPressed() {
		SharedPreferences.Editor editor = savedinputs.edit();
		editor.putBoolean("highway", checkHighways.isChecked());
		editor.putBoolean("toll", checkTolls.isChecked());
		editor.putBoolean("twoway", checkTwoWay.isChecked());
		editor.putString("mpg", editMPG.getText().toString());
		editor.putString("gallons", gallonsToFill.getText().toString());
		editor.putInt("radius", radius);
		editor.putString("gasGrade", tabHost.getCurrentTabTag());
		editor.putBoolean("isManualLocationChecked", tbManual.isChecked());
		editor.commit();
		this.finish();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		return true;
	}

	public void savePrefs() {
		SharedPreferences.Editor editor = savedinputs.edit();
		editor.putBoolean("highway", checkHighways.isChecked());
		editor.putBoolean("toll", checkTolls.isChecked());
		editor.putBoolean("twoway", checkTwoWay.isChecked());
		editor.putString("mpg", editMPG.getText().toString());
		editor.putString("gallons", gallonsToFill.getText().toString());
		editor.putInt("radius", radius);
		editor.putString("gasGrade", tabHost.getCurrentTabTag());
		editor.putBoolean("isManualLocationChecked", tbManual.isChecked());
		editor.commit();

	}

	private GasStation[] getGasStations() {
		if(latitude == null || longitude == null) return null;
		GasStation[] ReturnedStations = GasStationRequest.getGasStationsDistance(latitude, longitude, ""+radius, tabHost.getCurrentTabTag(), "distance");

		return ReturnedStations;
	}

	private void getDrivingDistances(GasStation[] ReturnedStations) {
		ReturnedDrivingDistances = GoogleDistanceMatrixRequest.getGoogleDistanceRequest(latitude, longitude, ReturnedStations);
		if(ReturnedDrivingDistances == null) {
			//Toast.makeText(getApplicationContext(), R.string.overQueryLimit, Toast.LENGTH_LONG).show();
			//Toast.makeText(getApplicationContext(), "There was an error parsing data from the gas station database. Please reset your connection and try again.", Toast.LENGTH_LONG).show();
			return;
		}
	}

	private void setSavings() {
		for(int i = 0; i < ReturnedDrivingDistances.length; i++) {
			//if price is N/A then it can't be done...
			try {
				Double.parseDouble(ReturnedDrivingDistances[i].getStation().getPrice());
				//if it passes then continue...
				ReturnedDrivingDistances[i].getStation().setSavings(computeTotalCost(ReturnedDrivingDistances[i].getDistanceInMiles(), 
						gallons, 
						mpg, 
						Double.parseDouble(ReturnedDrivingDistances[i].getStation().getPrice()), 
						checkTwoWay.isChecked(),
						ReturnedDrivingDistances[i].getDurationSecs()));

			} catch (NumberFormatException e) {
				ReturnedDrivingDistances[i].getStation().setSavings(0.0);
			}

		}
	}

	private void sortGasStations() {
		//sort in ascending order by cheapest station to go to
		//	sortbySavings(ReturnedDrivingDistances);
		Arrays.sort(ReturnedDrivingDistances);
		for(int i = 0; i < ReturnedDrivingDistances.length - 1; i++) {
			double DifferenceInSavings = ReturnedDrivingDistances[i+1].getStation().getSavings() - ReturnedDrivingDistances[i].getStation().getSavings();
			ReturnedDrivingDistances[i].getStation().setDifferenceInSavings(DifferenceInSavings);
		}
		//save user inputs...
	}


	private double computeTotalCost(double distance, double gallonsToFill, double mpg, double priceOfGas, boolean twoWayTrip, int duration) {
		if(savedinputs.getBoolean("usepay", false)) {
			String wage = savedinputs.getString("pay", "20.0");
			try {
				double hourlypay = Double.parseDouble(wage);
				//duration in hours * hourly pay is how much it costs to drive there (price wise).
				double priceToDrive = (duration / 3600.0) * hourlypay;

				if(twoWayTrip) {
					distance *= 2;
				}

				return (((distance/mpg) + gallonsToFill) * priceOfGas) + priceToDrive;
			} catch (NumberFormatException n) {
				return 0.0;
			}
		}

		if(twoWayTrip) {
			distance *= 2;
		}
		return (((distance/mpg) + gallonsToFill) * priceOfGas);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
			MainActivity.this.startActivity(settings);
			return true;
		case R.id.reportBug:
			//send an email
			Intent intent = new Intent(Intent.ACTION_VIEW);
			String subject = "Report Bug";
			String body = "Thank you for helping us make our app better. Enter your phone info here to help us localize your issue: \n" 
								+ Build.MODEL + "\n" + 
								Build.MANUFACTURER + "\n" 
								+ "API level: " + android.os.Build.VERSION.SDK_INT 
								+ "\n\nWrite your error description here:\n";
			String emailAddress = "srcdevgroup@clemcode.net";
			Uri data = Uri.parse("mailto:" + emailAddress + "?subject=" + subject + "&body=" + body);
			intent.setData(data);
			startActivity(intent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			longitude = "" + loc.getLongitude();
			latitude = "" + loc.getLatitude();
			
			//location set.
			isNewLocation = true;

		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}
	}

	private class GetLocationAsync extends AsyncTask<Void, String, Void> {
		ProgressDialog progress;

		public GetLocationAsync() {
			// TODO Auto-generated constructor stub
			progress =  new ProgressDialog(MainActivity.this);
			progress.setTitle("Waiting for GPS");
			progress.setMessage("Obtaining GPS location...");
			progress.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			//progress.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			progress.setMessage(values[0]);
		}

		@Override
		protected Void doInBackground(Void... params) {

			if(tbManual.isChecked()) {
				String values = "Getting location via address...";
				publishProgress(values);
				getLocationViaGeoCode();
			} else {
				String values = "Getting location via GPS...";
				publishProgress(values);
				getLocationViaGPS();
			}
			
			int MaxTime = 5000;
			//make sure we don't wait forever.
			while((isNewLocation == false) || (MaxTime > 0)) {
				try {
					Thread.sleep(10);
					MaxTime -= 10;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progress.dismiss();

		}

	}

	private class doEverythingTask extends AsyncTask<Void, String, String> {
		ProgressDialog progress;

		public doEverythingTask() {
			// TODO Auto-generated constructor stub
			progress = new ProgressDialog(MainActivity.this);
			progress.setTitle("Executing...");
			progress.setCancelable(false);
			progress.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancel(true);
					progress.dismiss();
				}
			});
		}

		@Override
		protected String doInBackground(Void... params) {
			if(latitude == null) {
				cancel(false);
			}

			publishProgress("Getting list of gas stations in the area...");
			GasStation[] ReturnedStations = getGasStations();
			if(ReturnedStations == null) { 
				//	cancel(true);
				progress.dismiss();
				return "Unable to get list of gas stations, please try again.";
			}
			publishProgress("Computing driving distance to stations...");
			getDrivingDistances(ReturnedStations);
			if(ReturnedDrivingDistances == null) { 
				//cancel(true);
				progress.dismiss();
				return "Unable to get driving distance to gas stations, please check your location settings and try again.";
			}
			publishProgress("Calculating price to drive to each station...");
			setSavings();
			publishProgress("Sorting stations by total cost...");
			sortGasStations(); 
			return "Success!";
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			progress.setMessage(values[0]);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.setMessage("Getting location...");
			progress.setIndeterminate(true);
			progress.show();
			//get location.

		}

		@Override
		protected void onPostExecute(String result) {
			progress.dismiss();
			//if nothing to display on the list, cancel.

			if((ReturnedDrivingDistances == null)) {
				if(numberOfTries < 2) {
					bCompute.performClick();
					Toast.makeText(getApplicationContext(), "Auto retry...", Toast.LENGTH_SHORT).show();
					numberOfTries++;
				} else {
					Toast.makeText(getApplicationContext(), "No driving directions were returned from Google. The query limit may have been reached. Please try again later.", Toast.LENGTH_LONG).show();

					numberOfTries = 0;
				}
				return;
			} else if (isCancelled() || 
					!result.equals("Success!") || 
					ReturnedDrivingDistances.length == 0) {
				if(numberOfTries < 2) {
					bCompute.performClick();
					Toast.makeText(getApplicationContext(), "Auto retry...", Toast.LENGTH_SHORT).show();
					numberOfTries++;
				} else {
					Toast.makeText(getApplicationContext(), "Connection error, please try again.", Toast.LENGTH_SHORT).show();
					numberOfTries = 0;
				}
				return;
			} else {
				numberOfTries = 0;
				Intent displayActivity = new Intent(getApplicationContext(), GasStationDisplayActivity.class);
				displayActivity.putExtra("fueltype", tabHost.getCurrentTabTag());
				startActivity(displayActivity);

			}
		}

	}

	private void locationAccessAlert() {
		AlertDialog.Builder dialog;
		dialog = new AlertDialog.Builder(this);
		dialog.setMessage(this.getResources().getString(R.string.network_not_enabled));
		dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
				startActivity(myIntent);
				//get gps
			}
		});
		dialog.setNegativeButton(this.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface paramDialogInterface, int paramInt) {
				finish();
			}
		});
		dialog.setNeutralButton("Use Manual Location", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				tbManual.setChecked(true);

			}
		});
		dialog.setCancelable(false);
		dialog.show();
	} 


	private class waypointAsyncTask extends AsyncTask<String, String, String> {
		ProgressDialog progress;
		int currentProg = 0;

		public waypointAsyncTask() {
			// TODO Auto-generated constructor stub
			progress = new ProgressDialog(MainActivity.this);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setTitle("Executing...");
			progress.setCancelable(false);
			progress.setButton(ProgressDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancel(true);
					progress.dismiss();
				}
			});
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			progress.setMessage(values[0]);
			progress.setProgress(currentProg);
		}

		@Override
		protected void onPreExecute() {
			//super.onPreExecute();
			progress.setMessage("Started...");
			progress.setIndeterminate(true);
			progress.show();
			//get location.
			if(tbManual.isChecked()) {
				String values = "Getting location via address...";
				publishProgress(values);
				new Thread(new Runnable() {						
					@Override
					public void run() {
						getLocationViaGeoCode();

					}
				}).start();
			} else {
				String values = "Getting location via GPS...";
				publishProgress(values);
				new Thread(new Runnable() {						
					@Override
					public void run() {
						getLocationViaGPS();

					}
				}).start();
			}
		}

		@Override
		protected String doInBackground(String... destinationAddr) {

			String startAddr = latitude + "," + longitude;
			//getdrivingdistance...

			if(latitude == null) {
				cancel(false);
			}

			//get list of stations within radius like normal...
			publishProgress("Getting list of gas stations in the area...");
			GasStation[] ReturnedStations = getGasStations();

			if(ReturnedStations == null) { 
				progress.dismiss();
				return "Unable to get list of gas stations, please try again.";
			}			
			//max length of progressbar is the number of stations...
			progress.setMax(ReturnedStations.length);
			progress.setIndeterminate(false);

			//returned stations = new google distance.
			ReturnedDrivingDistances = new GoogleDistance[ReturnedStations.length];

			//do waypoints...
			publishProgress("Calculating best stations along your route...");
			for(int i = 0; i < ReturnedStations.length; i++) {
				int[] results = WaypointCalculator.getDrivingDistance(startAddr, ReturnedStations[i], destinationAddr[0]);
				int distance = results[0];
				int duration = results[1];
				//set up each station as googledistance...
				ReturnedDrivingDistances[i] = new GoogleDistance(distance, "", "", duration, "", ReturnedStations[i]);

				double price = 0.0;
				try {
					price = Double.parseDouble(ReturnedDrivingDistances[i].getStation().getPrice());
				} catch (NumberFormatException n) {
					price = 0.0;
				}
				//set savings for each station.
				ReturnedDrivingDistances[i].getStation().setSavings(computeTotalCost(ReturnedDrivingDistances[i].getDistanceInMiles(), 
						gallons, 
						mpg, 
						price, 
						checkTwoWay.isChecked(),
						ReturnedDrivingDistances[i].getDurationSecs()));

				publishProgress("Distance to station #" + (i+1) + ": " + Math.round((distance/METERS_IN_MILE)) + " miles; or about " + Math.round((duration / 60.0)) + " minutes");
				currentProg = i;
			}


			//publishProgress("Calculating price to drive to each station...");

			//sort stations.
			publishProgress("Sorting stations by total cost...");
			sortGasStations(); 
			return "Success!";
		}

		@Override
		protected void onPostExecute(String result) {
			progress.dismiss();

			//start display of stations...
			Intent displayActivity = new Intent(getApplicationContext(), GasStationDisplayActivity.class);
			displayActivity.putExtra("fueltype", tabHost.getCurrentTabTag());
			startActivity(displayActivity);
		}
	}


}
