package com.mig.dataaccess;




/**
 *  High level reason why the data access result failed.
 *  
 * @author rob
 *
 */
public enum DataAccessErrorType {

	NONE, 
	NO_CONNECTION, 
	CONNECTION_ERROR,
	TIMEOUT, 
	PARSER_FAILED,
	UNKNOWN_HOST, 
	UNKNOWN_ERROR
}
