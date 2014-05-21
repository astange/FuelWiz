package com.jas.fuelwiz;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	
	Preference eula;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		eula = (Preference) findPreference("prefEULA");		
		eula.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				alertDialogEULA();
				return true;
			}
		});
		
	}
	
	private void alertDialogEULA() {
		AlertDialog.Builder dialog;
		dialog = new AlertDialog.Builder(this);
        dialog.setMessage(this.getResources().getString(R.string.eulatext));
        dialog.setPositiveButton("Disagree", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
            	finish();
            }
        });

        dialog.setNeutralButton("Agree", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
        dialog.setCancelable(false);
        dialog.show();
	}
	
	
}

