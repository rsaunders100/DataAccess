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


/*
 * `NO_CONNECTION`     There is no internet connection dected on the device.  This error will come back imidiately. (it wont wait for a timout)
 * `CONNECTION_ERROR`  Somthing went wrong with the connection.
 * `TIMEOUT`           The connection timed out.
 * `PARSER_FAILED`     The parser threw an error or returned a null object.
 * `UNKNOWN_HOST`      The given URL could not be resolved.
 * `UNKNOWN_ERROR`     Catch all error.
 */