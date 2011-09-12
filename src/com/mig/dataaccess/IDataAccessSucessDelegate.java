package com.mig.dataaccess;


public interface IDataAccessSucessDelegate<T> {
	
	/**
	 * This is the method deals with a sucessful result of a data access operation
	 * 
	 * The result is a parsed data object that garunteed to be not null.
	 *          
	 */
	public void onDataAccessSucess(T result);	
}
