package com.mig.dataaccess.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mig.dataaccess.DataAccess;
import com.mig.dataaccess.DataAccessErrorType;
import com.mig.dataaccess.IDataAccessFailureDelegate;
import com.mig.dataaccess.IDataAccessSucessDelegate;
import com.mig.dataaccess.R;
 


/**
 * @author rob
 * 
 */
public class MainActivity extends Activity  {
    
	private static final String TAG = "MainActivity";
	
	private DataAccess<GeoLocationResult> _dataAccess;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Button next = (Button) findViewById(R.id.Button01);
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(MainActivity.this, SecondaryActivity.class);
				startActivity(intent);
				
			}
		});
		
		_dataAccess = new DataAccess<GeoLocationResult>(new GeoLocationJSONParser());
		
        _dataAccess.setSucessDelegate(new IDataAccessSucessDelegate<GeoLocationResult>() {

			@Override
			public void onDataAccessSucess(GeoLocationResult result) {
				
    			Log.i(TAG, "Sucess, Latitude: " + result.getResults().get(0).getGeometry().getLocation().getLat());
			}
		});
        
        _dataAccess.setFailureDelegate(new IDataAccessFailureDelegate() {

        	@Override
			public void onDataAccessFailed(DataAccessErrorType failReason, Exception exception) {
				
				Log.i(TAG, "Failed, reason: " + failReason.toString());
			}
		});
        
        _dataAccess.setCacheLength(this, 15);
		
		
		((Button)findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				GeoLocationResult existingResult = _dataAccess.getDataAccessResult(); 
				
				if (existingResult == null) 
				{
					Log.i(TAG, "Result not cahced, requesting new");
					
					_dataAccess.startDataAccess(getApplication(),
							"http://maps.googleapis.com/maps/api/geocode/json?address=11yorkRoad,Waterloo,London&sensor=false",false);
					
				} else {
					
					Log.i(TAG, "Result already cahced, Latitude: " + existingResult.getResults().get(0).getGeometry().getLocation().getLat());
				}
			}
		});

	}
	


    
}



