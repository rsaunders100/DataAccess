package com.mig.dataaccess.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.mig.dataaccess.R;


public class SecondaryActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.alt);
		
		Button button = (Button) findViewById(R.id.button2);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent myIntent = new Intent(SecondaryActivity.this, MainActivity.class);
				SecondaryActivity.this.startActivity(myIntent);
			} 
			
		}); 
	}
	
	@Override
    protected void onStart() {
    	super.onStart();
    	

        
    }
}
