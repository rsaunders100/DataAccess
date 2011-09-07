package com.tests.sandbox;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import android.util.Log;

import com.digitaljigsaw.dataaccess.IDataAccessObjectParser;
import com.google.gson.Gson;

public class GeoLocationJSONParser implements IDataAccessObjectParser<GeoLocationResult> {

	@Override
	public GeoLocationResult getDataObject(InputStream inputStream) throws Exception {
		
		
		// Convert to string
		String responseString = IOUtils.toString(inputStream, "UTF-8");
		Log.i("TEST", "Input string: " + responseString);
		
		
		// One line parsing FTW!
		GeoLocationResult geoLocationResult = new Gson().fromJson(responseString, GeoLocationResult.class);
		
		Log.i("TEST", "Parsed response: " + responseString);
		
		return geoLocationResult;
	}
	
}
