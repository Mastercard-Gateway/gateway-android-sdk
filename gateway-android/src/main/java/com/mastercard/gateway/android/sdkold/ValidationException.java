
package com.mastercard.gateway.android.sdkold;


public class ValidationException
    extends Exception
{

    public final String path;

    public ValidationException(String path) {
        super(("Missing required value: "+ path));
        this.path = path;
    }

    public ValidationException(String path, String regex) {
        super(((("Invalid value: "+ path)+" does not match ")+ regex));
        this.path = path;
    }

}
