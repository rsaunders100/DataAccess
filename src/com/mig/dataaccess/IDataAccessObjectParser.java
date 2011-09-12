package com.mig.dataaccess;

import java.io.InputStream;


/**
 * @author rob
 * @param <T>
 * 			The data object type that is returned by parsing the input stream
 */
public interface IDataAccessObjectParser<T> {
	
	
	/**
	 *  * This interface is for parsing an input stream from a HTTP GET request into a typed data object
	 * 
	 * NOTE: this method is expected to throw exceptions if it can't parse the result.
	 * all exceptions will be caught by the calling method and re-labeled as parsing errors
	 * 
	 * NOTE: if the parser returns null it is assumed to be an error.
	 * 
	 * NOTE: this method is called on a background thread, so avoid accessing any shared
	 * mutable data.
	 * 
	 * @param inputStream
	 * 				The stream from the HTTP request 
	 * @return
	 * 				A typed data object
	 */
	public T getDataObject(InputStream inputStream) throws Exception;
	
}
