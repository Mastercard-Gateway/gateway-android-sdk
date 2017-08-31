
package com.mastercard.gateway.android.sdk;

import com.mastercard.gateway.android.sdk.errorresponse.Error;

public class ErrorResponse
    extends Payload
{

    private final static long serialVersionUID = 1;
    private ErrorResponse.Result result;
    private Error error;

    @Override
    public void validate()
        throws ValidationException
    {
        if (result == null) {
            throw new ValidationException((__thisPath()+"result"));
        }
        if (error!= null) {
            error.validate();
        }
    }

    @Override
    public ErrorResponse readOnly(boolean b) {
        super.readOnly(b);
        if (error!= null) {
            error.readOnly(b);
        }
        return this;
    }

    /**
     * Gets the <code>result</code> value.
     * 
     * 
     * @return
     *     A system-generated high level overall result of the operation.
     */
    public ErrorResponse.Result result() {
        return result;
    }

    /**
     * Sets the <code>result</code> value.
     * 
     * 
     * @param result
     *     A system-generated high level overall result of the operation.
     * @return
     *     <code>this</code>, for invocation chaining.
     * @throws IllegalStateException
     *     If the object is in read-only mode
     */
    public ErrorResponse result(ErrorResponse.Result result) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.result = result;
        }
        return this;
    }

    /**
     * 
     * @return
     *     A system-generated high level overall result of the operation.
     */
    public ErrorResponse.Result getResult() {
        return this.result;
    }

    /**
     * 
     * @param result
     *     A system-generated high level overall result of the operation.
     */
    public void setResult(ErrorResponse.Result result) {
        this.result = result;
    }

    public Error error() {
        if (error == null) {
            if (readOnly()) {
                return new Error().readOnly(true);
            }
            error = new Error();
            error.__setParent(this);
        }
        return error;
    }

    public void setError(Error error) {
        this.error = error;
        this.error.__setParent(this);
    }

    public Error getError() {
        return this.error;
    }


    /**
     * A system-generated high level overall result of the operation.
     * 
     */
    public static enum Result {


        /**
         * The operation resulted in an error and hence cannot be processed.
         * 
         */
        ERROR;

    }

}
