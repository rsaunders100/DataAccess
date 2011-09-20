package com.mig.dataaccess.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.mig.dataaccess.DataAccess;
import com.mig.dataaccess.DataAccessErrorType;
import com.mig.dataaccess.IDataAccessFailureDelegate;
import com.mig.dataaccess.IDataAccessSucessDelegate;
import com.mig.dataaccess.R;
 


/**
 * Example code to demonstrate the DataAccess Class
 * 
 * @author rob
 */
public class MainActivity extends Activity  {
    
	private static final String TAG = "MainActivity";
	
	private DataAccess<GeoLocationResult> _dataAccess;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//
		// Initilise the Data acess object with a parser
		//
		
		_dataAccess = new DataAccess<GeoLocationResult>(new GeoLocationJSONParser());
		
		
		//
		// Set some paramters
		//
		
		_dataAccess.setCacheLength(this, 10);
		_dataAccess.setConnectionTimeOut(20);
		
		
		//
		// Set the code to be run when the request is sucessful or fails.
		//
		
        _dataAccess.setSucessDelegate(new IDataAccessSucessDelegate<GeoLocationResult>() {

			@Override
			public void onDataAccessSucess(GeoLocationResult result) 
			{
				double lat = result.getResults().get(0).getGeometry().getLocation().getLat();
				
    			Log.i(TAG, "Sucess, Latitude: " + lat);
    			Toast toast = Toast.makeText(MainActivity.this, "Lat:" + lat, 1000);
    			toast.show();
			}
		});
        _dataAccess.setFailureDelegate(new IDataAccessFailureDelegate() {

        	@Override
			public void onDataAccessFailed(DataAccessErrorType failReason, Exception exception) 
        	{
				Log.i(TAG, "Failed, reason: " + failReason.toString());
			}
		});
        
        
        //
        // When the user taps the button make the request to an example url (google's geo coder)
        //
        
		((Button)findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
				_dataAccess.startDataAccess(MainActivity.this,
							"http://maps.googleapis.com/maps/api/geocode/json?address=11yorkRoad,Waterloo,London&sensor=false",true);		
			}
		});
	}
}



