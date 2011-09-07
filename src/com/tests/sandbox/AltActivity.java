package com.tests.sandbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class AltActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("ROB","Alt onCreate");
		
		setContentView(R.layout.alt);
		
		Button button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent myIntent = new Intent(AltActivity.this, MainActivity.class);
				AltActivity.this.startActivity(myIntent);
			} 
			
		}); 
	}
	
	@Override
    protected void onStart() {
    	super.onStart();
    	

        
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	
    	Log.d("ROB","Alt onStop");
    	
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    	
    	Log.d("ROB","Alt onPause");
    }
    
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	Log.d("ROB","Alt onDestroy");
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	
    	Log.d("ROB","Alt onRestart");
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	
    	Log.d("ROB","Alt onResume");
    }
    
}
