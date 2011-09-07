package com.tests.sandbox;

import com.digitaljigsaw.dataaccess.DataAccess;

import android.app.Application;

public class MyApplication extends Application{
	
	
	public DataAccess<String> getDataAccess() { return _dataAccess; }
	public void setDataAccess(DataAccess<String> dataAccess) { _dataAccess = dataAccess; }
	
	private DataAccess<String> _dataAccess; 
	
	public DataAccess<String> dataAccess;

}
