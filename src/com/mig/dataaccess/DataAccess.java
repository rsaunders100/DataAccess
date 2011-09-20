package com.mig.dataaccess;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;
import org.apache.http.entity.StringEntity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.ignition.support.cache.AbstractCache;
import com.github.ignition.support.http.IgnitedHttp;
import com.github.ignition.support.http.IgnitedHttpRequest;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.github.ignition.support.http.cache.HttpResponseCache;


/**
 *    Yet anouther HTTP wrapper in the name of reducing boilier plate code.
 *    This acutally wraps Ignition (formally DriudFu) which in turn wrapps apache.<p>
 * 
 *    The data access class performs a HTTP request and parses the result
 *    on a background thread. It then posts the result back on the main
 *    thread.<p>
 * 		
 *    You must specifiy a parser and a sucess handler, 
 *    optionaly you may specify a fail handeler.<p>
 *    
 *    The parser must turn an imput stream into a typed data object of type T (you specify T) <p>
 *             
 *    The class takes a single URL and a delegate class to parse the result
 *    of the GET.<p>
 * 
 *    The class can cahce persistenty to disk - set up with "setCacheLength"<p>          
 *         		   
 *    You may wish to enclose this class in the activity singleton so that
 *    is persists across activities.<p>
 * 
 * @author rob
 * @param <T>
 *            The data object that should be returned by the data access operation.<br>
 *            This is the same object type that the given parser should retun
 */
public class DataAccess<T> {
	
	private static final String TAG = "DataAccess";

	// A flag to enable / disable all logging in this class
	private static boolean LOG = true;
	public static void setLogingEnabled(boolean isLoggingEnabled) { LOG = isLoggingEnabled; }
	
	private IDataAccessObjectParser<T> 	_objectParser;
	private DataAccessTask 				_dataAccessTask;
	private IDataAccessSucessDelegate<T> _sucessDelegate;
	private IDataAccessFailureDelegate 	_failDelegate;
	private boolean 					_isInProgress = false;
	private int 						_connectionTimeOutSeconds;
	private IgnitedHttp 				_ignitedHttp;
	
	// Hides the no-parameter consturctor
	@SuppressWarnings("unused")
	private DataAccess() {}
	
	/**
	 * Designated constructor. Needs an object parser. The object parser should
	 * take a inputStream and return a typed data object
	 */
	public DataAccess(IDataAccessObjectParser<T> objectParser) {
		_objectParser = objectParser;
	}
	
	
	/**
	 *  Checks to see if there is an existing data access opperation in progress
	 *  
	 *   Use cancel if you need a fresh reuqest, otherwise calls to 
	 *     startDataAccess will be ignored.
	 */
	public boolean isInProgress() {
		return _isInProgress;
	}
	
	/**
	 * Canceles any existing data access operation
	 */
	public void cancel() {

		if (_dataAccessTask != null) {
			_dataAccessTask.cancel(true);
		}
		_isInProgress = false;
	}

	/**
	 * Set the delegate that will deal with the sucessful result of the data access
	 * operation
	 */
	public void setSucessDelegate(IDataAccessSucessDelegate<T> delegate) {
		_sucessDelegate = delegate;
	}
	public IDataAccessSucessDelegate<T> getSucessDelegate() { return _sucessDelegate; }
	
	
	/**
	 * Set the delegate that will deal with the failure of the data access
	 * operation
	 */
	public void setFailureDelegate(IDataAccessFailureDelegate delegate) {
		_failDelegate = delegate;
	}
	public IDataAccessFailureDelegate getFailDelegate() { return _failDelegate; }
	
	

	/**
	 * Set up the cache for the request
	 * 
	 *   If a reuqest is made with the cache length of the previous request,
	 *      then the previuous result will be used.
	 *   
	 *   Will also save the results presistantly to the device
	 *       
	 */
	public void setCacheLength(Context context, int cacheLengthMins) 
	{	
		if (cacheLengthMins > 0) 
		{
			if (_ignitedHttp == null) {
				_ignitedHttp = new IgnitedHttp(context);
			}
			_ignitedHttp.enableResponseCache(context, 10, cacheLengthMins, 3, AbstractCache.DISK_CACHE_INTERNAL);
		}
	}
	
	
	public void setHTTPParameter(Context context, String name, Object value) 
	{
		if (_ignitedHttp == null) {
			_ignitedHttp = new IgnitedHttp(context);
		}
		
		_ignitedHttp.getHttpClient().getParams().setParameter(name, value);
	}


	/**
	 * Clears the cache linked to this instance of DataAccess.
	 */
	public void clearCache(String url) {
		
		HttpResponseCache responseCache = _ignitedHttp.getResponseCache();
		
		if (responseCache != null) {
			responseCache.clear();
		}
	}

	
	/**
	 *  Sets the connection timeout.
	 */
	public void setConnectionTimeOut(int timeoutSeconds) {
		_connectionTimeOutSeconds = timeoutSeconds;
	}
	

	/**
	 * Starts a request using the given URL.
	 * 
	 * You must first set an object parser and a delegate
	 * 
	 * The object parser takes response of the http request and parses it into a
	 * typed data object.
	 * 
	 * The delegate takes a response object and deals with it.
	 * 
	 * This method does not use any cached data and will perform a fresh request each time. 
	 * to use the cache use
	 * 		startDataAccess(String url, boolean usesCache)
	 * 
	 * This will log the request url, the request response, and the parsed
	 * object using log.d This will log any errors in log.e the errors are then
	 * packed up in the response object and parsed to the delegate
	 * 
	 * NEEDS PERMISSIONS : android.permission.ACCESS_NETWORK_STATE
	 * android.permission.INTERNET
	 * 
	 * @param context
	 *            Any context
	 * @param url
	 * 
	 */
	public void startDataAccess(Context context, String url) {

		startDataAccess(context, url, false);
	}

	/**
	 * Starts a request using the given URL.<p>
	 * 
	 * You must first set an object parser and a delegate.<p>
	 * 
	 * The object parser takes response of the http request and parses it into a
	 * typed data object.<p>
	 * 
	 * The delegate takes a response object and deals with it.<p>
	 * 
	 * If usesCache = true it will try to fetch the from the cache first. if
	 * that fails it will perform the request<p>
	 * 
	 * NOTE: to use the cache you need to have set up the cache first with the
	 * "enableCacheWithCacheLenght" method.<p>
	 * 
	 * This will log the request url, the request response, and the parsed
	 * object using log.i This will log any errors in log.e the errors are then
	 * packed up in the response object and parsed to the delegate<p>
	 * 
	 * NEEDS PERMISSIONS :<br> 
	 * android.permission.ACCESS_NETWORK_STATE<br>
	 * android.permission.INTERNET<p>
	 * 
	 * @param context
	 *            Any context
	 * @param url
	 * @param useCache
	 *            If true will attempt to pull the result from the cache first.
	 *            If that fails it will perform the request (providing there is a connection)
	 */
	public void startDataAccess(Context context, String url, boolean useCache) 
	{
		startDataAccess(context, url, useCache, null);
	}

	
	/**
	 * Starts a request using the given URL.<p>
	 * 
	 * You must first set an object parser and a delegate<p>
	 * 
	 * The object parser takes response of the http request and parses it into a
	 * typed data object.<p>
	 * 
	 * The delegate takes a response object and deals with it.<p>
	 * 
	 * If usesCache = true it will try to fetch the from the cache first. if
	 * that fails it will perform the request<p>
	 * 
	 * NOTE: to use the cache you need to have set up the cache first with the
	 * "enableCacheWithCacheLenght" method.<p>
	 * 
	 * This will log the request url, the request response, and the parsed
	 * object using log.i This will log any errors in log.e the errors are then
	 * packed up in the response object and parsed to the delegate<p>
	 * 
	 * NEEDS PERMISSIONS :<br> 
	 * android.permission.ACCESS_NETWORK_STATE<br>
	 * android.permission.INTERNET<p>
	 * 
	 * @param context
	 *            Any context
	 * @param url
	 * @param useCache
	 *            If true will attempt to pull the result from the cache first.<br>
	 *            If that fails it will perform the request (providing there is a connection)
	 * @param httpBody
	 * 			  If not null, the httpBody string will be posted with the request.
	 */
	public void startDataAccess(Context context, String url, boolean useCache, String httpBody) {

		// Info log is given to give the message some contrast in Logcat
		if (LOG)
			Log.i(TAG, "Started data access with UTL: " + url);

		// We only allow one request to be running at once.
		if (_isInProgress) 
		{
			Log.w(TAG, "started DataAccess but an opperation was already in progress, so ignored request. Use cancel() or create new data access object.");
			return;
		}
		_isInProgress = true;
		
		
		// Ensure the http object is initilised with a context
		// it is neeeded to check th cache
		
		if (_ignitedHttp == null) 
		{
			_ignitedHttp = new IgnitedHttp(context);
		}
		
		// We first do a quick check if the Internet connection is available
		// this means that we do not incur an overhead of creating the thread and
		// waiting for the connection to timeout if there is no connection.

		// If there is no connection hope is not lost yet...
		// we might be OK with cached data.
		// If so, check if there anything in the cache first
		// otherwise the DataAccessTask will waste time again waiting for a timeout
		
		boolean isOnline = DataAccessHelpers.isOnline(context);
		boolean isCached = isCached(url);

		if (isOnline || (useCache && isCached)) {
			// Provide some nice feedback to the developer
			if (!isOnline) {

				if (LOG)
					Log.d(TAG, "No internet connection, but data is cached so continuing with request");
			} else if (useCache && isCached) {

				if (LOG)
					Log.d(TAG, "We have a internet connection, but we have a cached version so we will use that");
			} else if (useCache && !isCached) {

				// Save checking the cache again in the DataAccessTask
				useCache = false;
				if (LOG)
					Log.d(TAG, "Connection is live and we have no cached version");
			} else {

				if (LOG)
					Log.d(TAG, "Connection is live and caching not requested");
			}

			_ignitedHttp.setConnectionTimeout(_connectionTimeOutSeconds * 1000);
			
			IgnitedHttpRequest ignitedHttpRequest = null;
			if (httpBody != null) 
			{
				try 
				{
					ignitedHttpRequest = _ignitedHttp.post(url, new StringEntity(httpBody));
				}
				catch (UnsupportedEncodingException exception) 
				{
					DataAccessHelpers.printStackTraceWithTag(exception, TAG);
					
					if (_failDelegate != null) {
						_failDelegate.onDataAccessFailed(DataAccessErrorType.UNKNOWN_ERROR, exception);
					}
				}
			}
			else 
			{
				_ignitedHttp.setConnectionTimeout(_connectionTimeOutSeconds * 1000);
				ignitedHttpRequest = _ignitedHttp.get(url, useCache);
			}

		
			if (ignitedHttpRequest != null) 
			{
				// We state that we are only expecting 200 error codes
				// this way it will throw an exception when other codes are returned
				// rather than caching the response.
				// if other error codes are expected you need to add them here
				ignitedHttpRequest.expecting(200);
	
				_dataAccessTask = new DataAccessTask();
				_dataAccessTask.setIgnitedHttp(ignitedHttpRequest);
				_dataAccessTask.execute();
			}
			
		} else {

			// Provide some nice feedback to the developer
			if (useCache) 
			{
				if (LOG) Log.e(TAG, "No internet connection detected. Cache was requested, but url was not cached.");
			} 
			else 
			{
				if (LOG) Log.e(TAG, "No internet connection detected and cache not requested.");
			}

			_isInProgress = false;

			// No Internet so package up the error and give it to the delegate
			if (_failDelegate != null) {
				_failDelegate.onDataAccessFailed(DataAccessErrorType.NO_CONNECTION, null);
			}
		}
	}
	


	/**
	 * Helper function to check if a URL is in the HTTP cache.
	 */
	private boolean isCached(String url)
	{
		if (_ignitedHttp == null) return false;
		HttpResponseCache responseCache = _ignitedHttp.getResponseCache();
		return (responseCache != null && responseCache.containsKey(url));
	}
	
	
	
	
	/**
	 *  Preforms the heavy lifting (request + parse + catch errors) 
	 *
	 */
	private class DataAccessTask extends AsyncTask<Void, Void, DataAccessResult<T>> {

		private static final String TAG = "DataAccess";
		
		
		public void setIgnitedHttp(IgnitedHttpRequest ignitedHttpRequest) {
			_ignitedHttpRequest = ignitedHttpRequest;
		}

		private IgnitedHttpRequest _ignitedHttpRequest;

		@Override
		protected DataAccessResult<T> doInBackground(Void... voids) {

			//
			// This code performs the request and tries to parse it
			// any errors are caught and packaged up in the response object
			//
			
			// The object that is to be returned
			DataAccessResult<T> result = null;

			try {

				// Send the request
				IgnitedHttpResponse ignitedHttpResponse = _ignitedHttpRequest.send();
				
				
				
				// Check if we are canceled now that we have the response
				// if we are it will save us having to parse the response
				if (isCancelled())
					return null;
				
				// Get the input stream of the response
				InputStream responseStream;
				try {
					
					responseStream = ignitedHttpResponse.getResponseBody();
				} catch (NullPointerException exception) {
					
					Log.w(TAG, "Caught null pointer exception, trying again with no cache");
					
					// On rare instances the cache might have been removed before we access it.
					// If this happens we should re-request once with no cache.					
					IgnitedHttpRequest newRequest = _ignitedHttp.get(_ignitedHttpRequest.getRequestUrl(), false);
					IgnitedHttpResponse newResponse = newRequest.send();
					responseStream = newResponse.getResponseBody();
				}

				// Convert the steam to a string so that we can log it.
				// This is slightly inefficient since object parser might well do the same.
				// Ensure logging is disabled for the final build
				if (LOG) {
					
					String responseString = IOUtils.toString(responseStream,"UTF-8");
					Log.d(TAG, "Did recieve response: " + responseString);
					
					// Since we have consumed the stream by converting it to a String
					// we should reset it back so the parser can parse it
					responseStream.reset();
				}
					
				// Parse it using the given parser (implements IDataAccessObjectParser)
				// if the parser fails, we will catch the error and package it up in a result object
				T parsedObject = null;
				try {
					parsedObject = _objectParser.getDataObject(responseStream);

				} catch (Exception exception) {

					if (LOG) {
						Log.e(TAG, "Parser error:");
						DataAccessHelpers.printStackTraceWithTag(exception, TAG);
					}

					// Create a failure object.
					result = new DataAccessResult<T>(
							DataAccessErrorType.PARSER_FAILED, exception);
				}

				// If we have not yet genreate a result object.
				if (result == null) {

					// The parser might have thrown an exception if anything
					// went wrong
					// or it might just have returned null.
					if (parsedObject == null) {

						if (LOG)
							Log.e(TAG, "Parser returned null");

						// Create a failure object.
						result = new DataAccessResult<T>(
								DataAccessErrorType.PARSER_FAILED);

					} else {

						if (LOG)
							Log.d(TAG, "Response sucessfully parsed: "
									+ parsedObject.toString());

						// Create a success object.
						result = new DataAccessResult<T>(parsedObject);
					}
				}

			} catch (ConnectException exception) {

				if (LOG) {
					Log.e(TAG, "Connection error:");
					DataAccessHelpers.printStackTraceWithTag(exception, TAG);
				}

				// Determine the type of connection exception error
				DataAccessErrorType cause = DataAccessErrorType.CONNECTION_ERROR;
				if (exception.getCause() != null) {
					if (exception.getCause() instanceof SocketTimeoutException) {
						cause = DataAccessErrorType.TIMEOUT;
					} else if (exception.getCause() instanceof ConnectException) {
						cause = DataAccessErrorType.TIMEOUT;
					} else if (exception.getCause() instanceof UnknownHostException) {
						cause = DataAccessErrorType.UNKNOWN_HOST;
					}
				}

				// Create a failure object.
				result = new DataAccessResult<T>(cause, exception);

			} catch (Exception exception) {

				if (LOG) {
					Log.e(TAG, "Unexpected error:");
					DataAccessHelpers.printStackTraceWithTag(exception, TAG);
				}

				// Create a failure object.
				result = new DataAccessResult<T>(DataAccessErrorType.UNKNOWN_ERROR, exception);
			}

			return result;
		}

		@Override
		protected void onPostExecute(DataAccessResult<T> dataAccessResult) 
		{

			// Don't need to do anything special here, just give the result to the delegate
			// We should check if we have a delegate first.

			_isInProgress = false;

			if (dataAccessResult.isSucess()) 
			{

				// Notify the delegate
				if (_sucessDelegate != null)  
				{
					_sucessDelegate.onDataAccessSucess(dataAccessResult.getDataObject());
				}

			} else {

				// Notify the delegate
				if (_failDelegate != null) 
				{
					_failDelegate.onDataAccessFailed(dataAccessResult.getErrorType(),
													 dataAccessResult.getException());
				}
			}
			
		}
	}
}
