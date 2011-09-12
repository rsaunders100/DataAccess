package com.mig.dataaccess;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.github.ignition.support.cache.AbstractCache;
import com.github.ignition.support.http.IgnitedHttp;
import com.github.ignition.support.http.IgnitedHttpRequest;
import com.github.ignition.support.http.IgnitedHttpResponse;
import com.github.ignition.support.http.cache.HttpResponseCache;

/**
 * 
 * @author rob
 * 
 *         The data access class performs a HTTP request and parses the result
 *         on a background thread. It then posts the result back on the main
 *         thread.
 * 
 *         The class takes a single URL and a delegate class to parse the result
 *         of the GET.
 * 
 *         The class can cahce persistenty to disk.
 * 
 * 		   The class stores the result of the GET.
 *         Check that the object dosn't exist first before requesting it.
 *            use: getDataAccessResult() and check for null.  
 *          
 *         If you have set up a cache time, getDataAccessResult() will return null 
 *         after the cache lenght has expired.
 *         		   
 *         You may wish to enclose this class in the activity singleton so that
 *         is persists across activities.
 * 
 *         TODO: Add support for posting a payload.
 * 
 * @param <T>
 *            The data object that should be returned by the data access
 *            operation.
 *            This is the same object type that the given parser should retun
 */
public class DataAccess<T> {

	
	private static boolean cacheSetUp = false;

	private static final String TAG = "DataAccess";

	// A flag to enable / disable all logging in this class
	private static final boolean LOG = true;

	private IDataAccessObjectParser<T> 	_objectParser;
	private DataAccessTask 				_dataAccessTask;
	private IDataAccessSucessDelegate<T> _sucessDelegate;
	private IDataAccessFailureDelegate 	_failDelegate;
	private boolean 					_isInProgress = false;
	private T 							_dataAccessResult;
	private SimpleTimerThreshold 		_cachedDataTimer;
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
	 * Reutrns the result of the latest sucessfull data access operation.
	 * 
	 *  If a chack length has been set up and the cache has expired
	 *    then this will return null
	 * 
	 */
	public T getDataAccessResult() {

		// If the cahce time has expired clear the cahced data object
		if (_cachedDataTimer != null
				&& _cachedDataTimer.hasTimerPassedThreshold()) 
		{
			_dataAccessResult = null;
		}

		return _dataAccessResult;
	}
	

	/**
	 * Set up the cache for the request
	 * 
	 *   If a reuqest is made with the cache length of the previous request,
	 *      then the previuous result will be used.
	 *   
	 *   Will also save the results presistantly to the device
	 *       
	 */
	public void setCacheLength(Context context, int cacheLengthSeonds) 
	{
		_cachedDataTimer = new SimpleTimerThreshold(cacheLengthSeonds);
		
		long cacheLengthMins = Math.round( ((double)cacheLengthSeonds) / 60.0);
		
		if (cacheLengthMins > 0) 
		{
			if (_ignitedHttp == null) {
				_ignitedHttp = new IgnitedHttp(context);
			}
			_ignitedHttp.enableResponseCache(context, 10, cacheLengthMins, 3, AbstractCache.DISK_CACHE_INTERNAL);
		}
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
	 * Starts a request using the given URL.
	 * 
	 * You must first set an object parser and a delegate
	 * 
	 * The object parser takes response of the http request and parses it into a
	 * typed data object.
	 * 
	 * The delegate takes a response object and deals with it.
	 * 
	 * If usesCache = true it will try to fetch the from the cache first. if
	 * that fails it will perform the request
	 * 
	 * NOTE: to use the cache you need to have set up the cache first with the
	 * "enableCacheWithCacheLenght" method.
	 * 
	 * This will log the request url, the request response, and the parsed
	 * object using log.i This will log any errors in log.e the errors are then
	 * packed up in the response object and parsed to the delegate
	 * 
	 * NEEDS PERMISSIONS : android.permission.ACCESS_NETWORK_STATE
	 * android.permission.INTERNET
	 * 
	 * @param context
	 *            Any context
	 * @param url
	 * @param useCache
	 *            If true will attempt to pull the result from the cache first.
	 *            If that fails it will perform the request.
	 */
	public void startDataAccess(Context context, String url, boolean useCache) {

		// Info log is given to give the message some contrast in Logcat
		if (LOG)
			Log.i(TAG, "Started data access with UTL: " + url);

		// We only allow one request to be running at once.
		if (_isInProgress) {
			Log.w(TAG,
					"started DataAccess but an opperation was already in progress, so ignored request. Use cancel() or create new data access object.");
			return;
		}
		_isInProgress = true;


		// We first do a quick check if the Internet connection is available
		// this means that we do not incur an overhead of creating the thread
		// and
		// waiting for the connection to timeout if there is no connection

		// If there is no connection hope is not lost yet...
		// we might be OK with cached data.
		// If so, check if there anything in the cache first
		// otherwise the DataAccessTask will waste time again waiting for a
		// timeout

		if (_ignitedHttp == null) 
		{
			_ignitedHttp = new IgnitedHttp(context);
		}
		
		boolean isOnline = DataAccessHelpers.isOnline(context);
		boolean isCached = isCached(url);

		if (isOnline || (useCache && isCached)) {
			// Provide some nice feedback to the developer
			if (!isOnline) {

				if (LOG)
					Log.d(TAG,
							"No internet connection, but data is cached so continuing with request");
			} else if (useCache && isCached) {

				if (LOG)
					Log.d(TAG,
							"We have a internet connection, but we have a cached version so we will use that");
			} else if (useCache && !isCached) {

				// Save checking the cache again in the DataAccessTask
				useCache = false;
				if (LOG)
					Log.d(TAG,
							"Connection is live and we have no cached version");
			} else {

				if (LOG)
					Log.d(TAG, "Connection is live and caching not requested");
			}

			
			
			_ignitedHttp.setConnectionTimeout(_connectionTimeOutSeconds * 1000);

			IgnitedHttpRequest ignitedHttpRequest = _ignitedHttp.get(url,useCache);
			// _ignitedHttp.enableResponseCache(context, 10,
			// expirationInMinutes, 2, AbstractCache.DISK_CACHE_INTERNAL);

			// We state that we are only expecting 200 error codes
			// this way it will throw an exception when other codes are returned
			// rather than caching the response.
			// if other error codes are expected you need to add them here
			ignitedHttpRequest.expecting(200);

			_dataAccessTask = new DataAccessTask();

			_dataAccessTask.setIgnitedHttp(ignitedHttpRequest);

			// _dataAccessTask.setUrl(url);
			// _dataAccessTask.setUseseCache(useCache);
			// _dataAccessTask.setContext(context);

			_dataAccessTask.execute();
		} else {

			// Provide some nice feedback to the developer
			if (useCache) {
				if (LOG)
					Log.e(TAG,
							"No internet connection detected. Cache was requested, but url was not cached.");
			} else {
				if (LOG)
					Log.e(TAG,
							"No internet connection detected and cache not requested.");
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
				InputStream responseStream = ignitedHttpResponse.getResponseBody();

				// Convert the steam to a string so that we can log it.
				// This is slightly inefficient since object parser might well do the same.
				// Ensure loggins is disabled for the final build
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
		protected void onPostExecute(DataAccessResult<T> dataAccessResult) {

			// Don't need to do anything special here, just give the result to the delegate
			// We should check if we have a delegate first.

			_isInProgress = false;

			if (dataAccessResult.isSucess()) {

				// Restart the cahce timer.
				_cachedDataTimer.restartTimer();

				// Store the data in an internal feed.
				_dataAccessResult = dataAccessResult.getDataObject();

				// Notify the delegate
				if (_sucessDelegate != null)  {
					_sucessDelegate.onDataAccessSucess(_dataAccessResult);
				}

			} else {

				// Notify the delegate
				if (_failDelegate != null) {
					_failDelegate.onDataAccessFailed(dataAccessResult.getErrorType(),
													 dataAccessResult.getException());
				}
			}
			
		}
	}
}
