DataAccess - Yet anouther HTTP wrapper
=========================

A simple framework to help request and parse data from a HTTP request.  Like all of these things its intention is too doing things properly and reduce boilerplate.



Usage
-----

 1. Instantiate DataAccess

....


Errors
------

The follwiong errors can be returned to the `onDataAccessFailed` method:

 * `NO_CONNECTION`     There is no internet connection dected on the device.  This error will come back imidiately. (it wont wait for a timout)
 * `CONNECTION_ERROR`  Somthing went wrong with the connection.
 * `TIMEOUT`           The connection timed out.
 * `PARSER_FAILED`     The parser threw an error or returned a null object.
 * `UNKNOWN_HOST`      The given URL could not be resolved.
 * `UNKNOWN_ERROR`     Catch all error.