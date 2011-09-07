package com.tests.sandbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.digitaljigsaw.dataaccess.DataAccess;
import com.digitaljigsaw.dataaccess.DataAccessResult.DataAccessResutErrorType;
import com.digitaljigsaw.dataaccess.IDataAccessDelegate;
 
// For testing
//http://maps.googleapis.com/maps/api/geocode/json?address=11yorkRoad,Waterloo,London&sensor=false

/**
 * @author rob
 * 
 */
public class MainActivity extends Activity  {
    
	Button next;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Log.d("ROB","Main onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		next = (Button) findViewById(R.id.Button01);
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(MainActivity.this, AltActivity.class);
				startActivity(intent);
			}
		});
	}
	

    @Override
    protected void onStart() {
    	super.onStart();
    	
    	Log.d("ROB","Main onStart");

    	DataAccess<GeoLocationResult> dataAccess = new DataAccess<GeoLocationResult>(new GeoLocationJSONParser());
    	
        dataAccess.setDelegate( new IDataAccessDelegate<GeoLocationResult>() {

			@Override
			public void onDataAccessSucess(GeoLocationResult result) {
				
    			Log.i("ROB","WORKED LAT: " + result.getResults().get(0).getGeometry().getLocation().getLat());
			}

			@Override
			public void onDataAccessFailed(DataAccessResutErrorType failReason, Exception exception) {
				
				Log.i("ROB","Failed");
			}
		});
        dataAccess.startDataAccess(getApplication(),
				"http://maps.googleapis.com/maps/api/geocode/json?address=11yorkRoad,Waterloo,London&sensor=false",false);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	Log.d("ROB","Main onStop");
    	
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	
    	Log.d("ROB","Main onPause");
    }
    
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	Log.d("ROB","Main onDestroy");
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	
    	Log.d("ROB","Main onRestart");
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    	Log.d("ROB","Main onResume");
    }
    



}



