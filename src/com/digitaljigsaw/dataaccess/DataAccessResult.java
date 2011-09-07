package com.digitaljigsaw.dataaccess;


/**
 * @author rob
 * 
 * This is a simple class to store the result of a 
 * http request + parse operation.
 *
 * @param <T> 
 * 			The data object that should be returned by the data access operation
 */ 
public class DataAccessResult<T> {
	
	public enum DataAccessResutErrorType {
		DATA_ACCESS_RESULT_ERROR_TYPE_NONE,
		DATA_ACCESS_RESULT_ERROR_TYPE_CONNECTION,
		DATA_ACCESS_RESULT_ERROR_TYPE_TIMEOUT,
		DATA_ACCESS_RESULT_ERROR_TYPE_BAD_SERVER_RESPONSE	
	}
	
	private T 						 	_dataObject;
	private boolean 					_sucess;
	private DataAccessResutErrorType 	_errorType;
	private Exception					_exception;
	
	/**
	 * Creates a result object with a successfully parsed data object  
	 * success = true is implied.
	 * 
	 * @param dataObject 
	 * 			The resultant parsed data object 
	 */
	public DataAccessResult (T dataObject) {
		
		_sucess = true;
		_errorType = DataAccessResutErrorType.DATA_ACCESS_RESULT_ERROR_TYPE_NONE;
		_dataObject = dataObject;
		_exception = null;
	}
	
	
	/**
	 * Creates a result object with a error type.
	 * success = false is implied
	 * 
	 * @param errorType
	 * 			The type of error that prevented the data object from being created
	 */
	public DataAccessResult (DataAccessResutErrorType errorType) {
		
		this(errorType,null);
	}

	/**
	 * Creates a result object with a error type.
	 * success = false is implied
	 * 
	 * @param errorType
	 * 			The type of error that prevented the data object from being created
	 * @param exception
	 * 			The exception that caused the error
	 */
	public DataAccessResult (DataAccessResutErrorType errorType, Exception exception) {
		
		_sucess = false;
		_errorType = errorType;
		_dataObject = null;
		_exception = exception;
	}
	
	public T getDataObject() { return _dataObject; }
	public Exception getException() { return _exception; }
	public boolean isSucess() { return _sucess; }
	public DataAccessResutErrorType getErrorType() { return _errorType; }
	
}
