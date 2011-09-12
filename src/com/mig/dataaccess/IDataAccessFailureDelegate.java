package com.mig.dataaccess;

public interface IDataAccessFailureDelegate {

	/**
	 * This is the method when the data access operation fails for any kind of reason.
	 * 
	 * @param failReason
	 * 			High level reason why the opertation failed.
	 * @param exception
	 * 			The actual exception that caused the error, can be null.
	 */
	public void onDataAccessFailed(DataAccessErrorType failReason, Exception exception);
}

