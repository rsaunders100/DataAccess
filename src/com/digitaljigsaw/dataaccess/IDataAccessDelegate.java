package com.digitaljigsaw.dataaccess;

import com.digitaljigsaw.dataaccess.DataAccessResult.DataAccessResutErrorType;

public interface IDataAccessDelegate<T> {
	
	/**
	 * This is the method deals with the result of a data access operation
	 * 
	 * The result can either contain an error or a parsed data object
	 * Check result.isSucess() then if its an error then check result.getErrorType() to get the 
	 * error type enumeration (no Internet error, timeout error or parse error)
	 * 
	 * @param result
	 * 			This object can either contain a parsed data object or an error code
	 *          
	 */
	
	public void onDataAccessSucess(T result);	
	public void onDataAccessFailed(DataAccessResutErrorType failReason, Exception exception);
	
}
