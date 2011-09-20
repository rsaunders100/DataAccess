package com.mig.dataaccess;



/**
 * This is a simple class to store the result of a 
 * http request + parse operation.
 * 
 *   This class is package private, 
 *   is is only used by DataAccess to pass the result of the AsyncTask to the main thread
 *   
 * @author rob
 *
 * @param <T> 
 * 			The data object class that should be returned by the data access operation
 */ 
class DataAccessResult<T> {
	
	private T 						_dataObject;
	private boolean 				_sucess;
	private DataAccessErrorType 	_errorType;
	private Exception				_exception;
	
	/**
	 * Creates a result object with a successfully parsed data object  
	 * success = true is implied.
	 * 
	 * @param dataObject 
	 * 			The resultant parsed data object 
	 */
	public DataAccessResult (T dataObject) {
		
		_sucess = true;
		_errorType = DataAccessErrorType.NONE;
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
	public DataAccessResult (DataAccessErrorType errorType) {
		
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
	public DataAccessResult (DataAccessErrorType errorType, Exception exception) {
		
		_sucess = false;
		_errorType = errorType;
		_dataObject = null;
		_exception = exception;
	}
	
	public T getDataObject() { return _dataObject; }
	public Exception getException() { return _exception; }
	public boolean isSucess() { return _sucess; }
	public DataAccessErrorType getErrorType() { return _errorType; }
	
}
