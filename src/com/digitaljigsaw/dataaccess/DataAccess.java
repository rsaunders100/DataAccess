package com.digitaljigsaw.dataaccess;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.digitaljigsaw.dataaccess.DataAccessResult.DataAccessResutErrorType;
import com.github.droidfu.cachefu.AbstractCache;
import com.github.droidfu.cachefu.HttpResponseCache;
import com.github.droidfu.http.BetterHttp;
import com.github.droidfu.http.BetterHttpRequest;
import com.github.droidfu.http.BetterHttpResponse;


/**
 * 
 * @author rob
 *
 * The data access class performs a HTTP request and parses the result on a background thread.
 * It then posts the result back on the UI thread.
 * 
 * The class takes a single URL and a delegate class to parse the result of the GET.
 * 
 * The class is capable of configuring persistent caching.
 * 
 * 
 * You may wish to enclose this class in the activity singleton so that is persists across activities 
 * 
 * Also you should wrap this class in another class.  
 * The wrapping class should be implementation specific.
 * It should have a method with each type of request with baked in URL and object parsers
 * 
 *     TODO: log warning when there is no delegate
 *
 * @param <T>
 * 		The data object that should be returned by the data access operation
 */
public class DataAccess<T> {
	
	private static boolean cacheSetUp = false;
	
	private static final String TAG = "DataAccess";
	
	// A flag to enable / disable all logging in this class
	private static final boolean LOG = true;
	
	private IDataAccessObjectParser<T> _objectParser;
	private DataAccessTask 			   _dataAccessTask;
	private IDataAccessDelegate<T>	   _delegate;
	private boolean 				   _isInProgress = false;
	
	public boolean isInProgress() { return _isInProgress; }
	
	/**
	 * Set the delegate that will deal with the result of the data access operation
	 * 
	 * @param delegate
	 */
	public void setDelegate(IDataAccessDelegate<T> delegate) { _delegate = delegate; }
	public IDataAccessDelegate<T> getDelegate() { return _delegate; }
	
	
	/**
	 * Designated constructor.
	 * Needs an object parser.
	 * The object parser should take a inputStream and return a typed data object
	 */
	public DataAccess(IDataAccessObjectParser<T> objectParser) {
		_objectParser = objectParser;
	}
	
	
	/**
	 * Set the length of time responses will stay in the cache for.  
	 * After the time the responses will be purged from the cache.
	 * 
	 * @param context
	 * 			ANY context will do (e.g. the application).
	 * 		    This is only used to get a pointer to the SD card for storage.
	 *          it is stupid that it need this, 
	 *          Android should have the application accessible via a singleton my default.
	 * @param cacheLengthMins  
	 */
	public static void enableCacheWithCacheLenght(Context context, long cacheLengthMins) {
		BetterHttp.enableResponseCache(context, 10, cacheLengthMins, 4, AbstractCache.DISK_CACHE_INTERNAL);
	}
	
	
	/**
	 * Removes the response for a given URL from the cache.
	 * Will also remove it from disk.
	 * 
	 */
	public static void removeURLFromCache(String url) {
		HttpResponseCache responseCache = BetterHttp.getResponseCache();
		if (responseCache != null) {
			
			if (responseCache.containsKey(url)) {
				if (LOG) Log.d(TAG, "Removing URL from cache: " + url);
				responseCache.remove(url);
			} else {
				if (LOG) Log.d(TAG, "Could not remove URL from cache: URL does not exist in cache. Asked to remove:" + url);
			}
		} else {
			if (LOG) Log.e(TAG, "Could not remove URL from cache: Cahce does not exist. Asked to remove:" + url);
		}
		
	}
	
	
	public static void setConnectionTimeOut(int timeoutMiliseconds) {
		BetterHttp.setSocketTimeout(timeoutMiliseconds);
	} 
	
	
	/**
	 * Will attempt to cancel the data access operation
	 */
	public void cancel() {
		
		if (_dataAccessTask != null) {
			_dataAccessTask.cancel(true);
		}
		_isInProgress = false;
	}
	
	
	/**
	 * Starts a request using the given URL.  
	 * 
	 * You must first set an object parser and a delegate
	 * 
	 * The object parser takes response of the http request and parses it 
	 * into a typed data object.
	 * 
	 * The delegate takes a response object and deals with it. 
	 * 
	 * This method does not use any cached data and will perform a fresh 
	 * request each time.  to use the cache use "startDataAccess(String url, boolean usesCache)"
	 * 
	 * 
	 * This will log the request url, the request response, and the parsed object using log.d
	 * This will log any errors in log.e the errors are then packed up in the response object and parsed to the delegate
	 * 
	 * NEEDS PERMISSIONS : android.permission.ACCESS_NETWORK_STATE
	 * 					   android.permission.INTERNET
	 * 
	 * @param context
	 * 			Any context
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
	 * The object parser takes response of the http request and parses it 
	 * into a typed data object.
	 * 
	 * The delegate takes a response object and deals with it. 
	 * 
	 * If usesCache = true it will try to fetch the from the cache first.
	 *                if that fails it will perform the request
	 * 
	 * NOTE: to use the cache you need to have set up the cache first with
	 * the "enableCacheWithCacheLenght" method.
	 * 
	 * This will log the request url, the request response, and the parsed object using log.i
	 * This will log any errors in log.e the errors are then packed up in the response object and parsed to the delegate
	 * 
	 * NEEDS PERMISSIONS : android.permission.ACCESS_NETWORK_STATE
	 * 					   android.permission.INTERNET
	 * @param context
	 * 			Any context
	 * @param url
	 * @param useCache
	 * 			If true will attempt to pull the result from the cache first.
	 *          If that fails it will perform the request.
	 */
	public void startDataAccess(Context context, String url, boolean useCache) {
		
		// Info log is given to give the message some contrast in Logcat
		if (LOG) Log.i(TAG, "Started data access with UTL: " + url);
		
		// We only allow one request to be running at once.
		if (_isInProgress) {
			Log.w(TAG,"started DataAccess but an opperation was already in progress, so ignored request. Use cancel() or create new data access object.");
			return;
		}
		_isInProgress = true;
		
		// I think there is a bug in droidfu.  If we do not call this method we get
		// a null pointer exception in  'request.send();'  (of BetterHttpRequest)
		if (!cacheSetUp) {
			cacheSetUp = true;
			BetterHttp.setupHttpClient();
		}
		
		// We first do a quick check if the Internet connection is available
		// this means that we do not incur an overhead of creating the thread and
		// waiting for the connection to timeout if there is no connection
		
		// If there is no connection hope is not lost yet...
		// we might be OK with cached data.
		// If so, check if there anything in the cache first
		// otherwise the DataAccessTask will waste time again waiting for a timeout 
		
		boolean isOnline = isOnline(context);
		boolean isCached = isCached(url);
		
		if (isOnline ||
				(useCache && isCached) )
		{
			// Provide some nice feedback to the developer
			if (!isOnline) {
				
				if (LOG) Log.d(TAG,"No internet connection, but data is cached so continuing with request");
			} else if (useCache && isCached) {
					
				if (LOG) Log.d(TAG,"We have a internet connection, but we have a cached version so we will use that");
			} else if (useCache && !isCached) {
				
				// Save checking the cache again in the DataAccessTask
				useCache = false;
				if (LOG) Log.d(TAG,"Connection is live and we have no cached version");
			} else {
				
				if (LOG) Log.d(TAG,"Connection is live and caching not requested");
			}
			
			_dataAccessTask = new DataAccessTask();
			_dataAccessTask.setUrl(url);
			_dataAccessTask.setUseseCache(useCache);
			_dataAccessTask.execute();
		} 
		else 
		{
			// Provide some nice feedback to the developer
			if (useCache) {
				if (LOG) Log.e(TAG,"No internet connection detected. Cache was requested, but url was not cached.");
			} else {
				if (LOG) Log.e(TAG,"No internet connection detected and cache not requested.");
			}
			
			_isInProgress = false;
			
			// No Internet so package up the error and give it to the delegate
			if (_delegate != null) 
			{
				_delegate.onDataAccessFailed(DataAccessResutErrorType.DATA_ACCESS_RESULT_ERROR_TYPE_CONNECTION, null);
			}
		}
	}
	
	
	/**
	 * A nice helper function I found to check if the intent connection is available
	 *   NEEDS PERMISSION : android.permission.ACCESS_NETWORK_STATE
	 */
	public boolean isOnline(Context context) {
		
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) 
	    {
	        return true;
	    }
	    return false;
	}
	
	
	/**
	 * Helper function to check if a URL is in the HTTP cache.
	 * 
	 */
	private boolean isCached(String url) {
		
		HttpResponseCache responseCache = BetterHttp.getResponseCache();
		return (responseCache != null && responseCache.containsKey(url));
	}
	

	private class DataAccessTask extends AsyncTask<Void, Void, DataAccessResult<T>> {

		public void setUseseCache(boolean useCache) {_useCache = useCache;}
		public void setUrl(String url) {_url = url;}
		
		String _url;
		boolean _useCache;
		
		@Override
		protected DataAccessResult<T> doInBackground(Void... voids) {
			
			// This code simply performs the request and tries to parse it
			// any errors are caught and packaged up in the response object
			
			DataAccessResult<T> result;
			
			try {
					
				BetterHttpRequest request = BetterHttp.get(_url, _useCache);
				
				// we state that we are only expecting 200 error codes
				// this way it will throw an exception when other codes are returned
				// rather than caching the response.
				// if other error codes are expected you need to add them here
				request.expecting(200);
				
				// This class dosen't currently support posting data
				// ill add it in when its needed.
				BetterHttpResponse response = request.send();
				
				// Check if we are canceled now that we have the response
				// if we are it will save us having to parse the response
				if (isCancelled()) return null;
				
				// We wont check HTTP status codes, we will just throw it at the parser
				// and catch any errors
				InputStream responseStream = response.getResponseBody();
				
				// Convert the steam to a string so that we can log it.
				// This is slightly inefficient since object parser might well do the same 
				// if performance is an issue comment this out for final build
				String responseString = IOUtils.toString(responseStream, "UTF-8");
				if (LOG) Log.d(TAG, "Did recieve response: " + responseString);
				
				// Since we have consumes the stream by converting it to a String
				// we should reset it back so the parser can parse it
				responseStream.reset();
				
				// parse it using the given parser (implements IDataAccessObjectParser)
				T parsedObject = _objectParser.getDataObject(responseStream);
				
				// the parser might have thrown an exception if anything went wrong
				// or it might just return null.
				if (parsedObject == null) {
					
					if (LOG) Log.e(TAG, "Parser returned null");
					
					// Create a failure object.
					result = new DataAccessResult<T>(DataAccessResutErrorType.DATA_ACCESS_RESULT_ERROR_TYPE_BAD_SERVER_RESPONSE);
				} else {
					
					if (LOG) Log.d(TAG, "Response sucessfully parsed: " + parsedObject.toString());
					
					// Create a success object.
					result = new DataAccessResult<T>(parsedObject);
				}
			} catch (ConnectException e) {
				
				if (LOG) {
					Log.e(TAG,"Connection error:");
					printStackTraceWithTag(e, TAG);
				}
				
				// Create a failure object.
				result = new DataAccessResult<T>(DataAccessResutErrorType.DATA_ACCESS_RESULT_ERROR_TYPE_CONNECTION, e);
				
			} catch (Exception e) {
				
				if (LOG) {
					Log.e(TAG,"Parsing error:");
					printStackTraceWithTag(e, TAG);
				}
				
				// we assume if something other than a connection error happened, then it is the result of 
				// a bad server response that we could not parse.
				
				// Create a failure object.
				result = new DataAccessResult<T>(DataAccessResutErrorType.DATA_ACCESS_RESULT_ERROR_TYPE_BAD_SERVER_RESPONSE, e);
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(DataAccessResult<T> dataAccessResult) {
			
			// Don't need to do anything special here, just give the result to the delegate
			// We should check if we have a delegate first.
			
			_isInProgress = false;
			
			if (_delegate != null) {
				
				if (dataAccessResult.isSucess()) {
					_delegate.onDataAccessSucess(dataAccessResult.getDataObject());
				} else {
					
					_delegate.onDataAccessFailed(dataAccessResult.getErrorType(), dataAccessResult.getException());
				}
			}
	    }
	}
	
	
	
	private static void printStackTraceWithTag(Exception e, String tag) {
		StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        Log.e(tag,stringWriter.toString());
	}
}
