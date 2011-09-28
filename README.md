DataAccess - Yet anouther HTTP wrapper
=========================

A simple framework to help request and parse data from a HTTP request.  Like all of these things the intention is to reduce boilerplate and to do things properly.



Usage
-----

 1. **Create a data parser**

When you fet the data over HTTP you get back a inputstream containing the HTTP response.  A data parser simply takes that input stream and converts it to a data object of your choice.  Just implement `IDataAccessObjectParser<T>` Where `T` is your output data object type.

E.g.

     		public class MyParser implements IDataAccessObjectParser<MyDataObject> 
			{
				@Override
				public MyDataObject getDataObject(InputStream inputStream) throws Exception 
				{
					// Convert to string
					String responseString = IOUtils.toString(inputStream, "UTF-8");
					
					MyDataObject dataObject = new MyDataObject();
					
					// .. Parser the data ....
					
					// Return parsed data object
					return dataObject;
				}
			}



 2. **Instantiate DataAccess with the Parser**

    DataAccess<MyDataObject> dataAccess = new DataAccess<MyDataObject>( new MyParser() );
    
 3. **Define what to do when we are sucessfull and when we fail**


 4. **(Optional) Set some paramters.**


 5. **Start the request with a URL OR a URL and some data to post.**






Errors
------

The follwiong errors can be returned to the `onDataAccessFailed` method:

 * `NO_CONNECTION`     There is no internet connection dected on the device.  This error will come back imidiately. (it wont wait for a timout)
 * `CONNECTION_ERROR`  Somthing went wrong with the connection.
 * `TIMEOUT`           The connection timed out.
 * `PARSER_FAILED`     The parser threw an error or returned a null object.
 * `UNKNOWN_HOST`      The given URL could not be resolved.
 * `UNKNOWN_ERROR`     Catch all error.


Permission used
---------------

    android.permission.ACCESS_NETWORK_STATE
    android.permission.INTERNET



Libraries used
--------------

This uses

