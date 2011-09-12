package com.mig.dataaccess.example;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.mig.dataaccess.IDataAccessObjectParser;

public class GeoLocationJSONParser implements IDataAccessObjectParser<GeoLocationResult> {

	@Override
	public GeoLocationResult getDataObject(InputStream inputStream) throws Exception {
		
		// Convert to string
		String responseString = IOUtils.toString(inputStream, "UTF-8");
		
		// One line parsing FTW!
		GeoLocationResult geoLocationResult = new Gson().fromJson(responseString, GeoLocationResult.class);
		
		// Return parsed data object
		return geoLocationResult;
	}
	
}
