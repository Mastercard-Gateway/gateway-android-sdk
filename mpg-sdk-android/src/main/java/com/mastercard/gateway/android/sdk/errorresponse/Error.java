
package com.mastercard.gateway.android.sdk.errorresponse;

import com.mastercard.gateway.android.sdk.Payload;
import com.mastercard.gateway.android.sdk.ValidationException;

public class Error
    extends Payload
{

    private final static long serialVersionUID = 1;
    private Error.Cause cause;
    private String explanation;
    private String field;
    private String supportCode;
    private Error.ValidationType validationType;

    @Override
    public void validate()
        throws ValidationException
    {
        if (cause == null) {
            throw new ValidationException((__thisPath()+"cause"));
        }
    }

    @Override
    public Error readOnly(boolean b) {
        super.readOnly(b);
        return this;
    }

    /**
     * Gets the <code>cause</code> value.
     * 
     * 
     * @return
     *     Broadly categorizes the cause of the error.
     *     For example, errors may occur due to invalid requests or internal system failures.
     */
    public Error.Cause cause() {
        return cause;
    }

    /**
     * Sets the <code>cause</code> value.
     * 
     * 
     * @param cause
     *     Broadly categorizes the cause of the error.
     *     For example, errors may occur due to invalid requests or internal system failures.
     * @return
     *     <code>this</code>, for invocation chaining.
     * @throws IllegalStateException
     *     If the object is in read-only mode
     */
    public Error cause(Error.Cause cause) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.cause = cause;
        }
        return this;
    }

    /**
     * 
     * @return
     *     Broadly categorizes the cause of the error.
     *     For example, errors may occur due to invalid requests or internal system failures.
     */
    public Error.Cause getCause() {
        return this.cause;
    }

    /**
     * 
     * @param cause
     *     Broadly categorizes the cause of the error.
     *     For example, errors may occur due to invalid requests or internal system failures.
     */
    public void setCause(Error.Cause cause) {
        this.cause = cause;
    }

    /**
     * Gets the <code>explanation</code> value.
     * 
     * 
     * @return
     *     Textual description of the error based on the cause.
     *     This field is returned only if the cause is INVALID_REQUEST or SERVER_BUSY
     */
    public String explanation() {
        return explanation;
    }

    /**
     * Sets the <code>explanation</code> value.
     * 
     * 
     * @param explanation
     *     Textual description of the error based on the cause.
     *     This field is returned only if the cause is INVALID_REQUEST or SERVER_BUSY
     * @return
     *     <code>this</code>, for invocation chaining.
     * @throws IllegalStateException
     *     If the object is in read-only mode
     */
    public Error explanation(String explanation) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.explanation = explanation;
        }
        return this;
    }

    /**
     * 
     * @return
     *     Textual description of the error based on the cause.
     *     This field is returned only if the cause is INVALID_REQUEST or SERVER_BUSY
     */
    public String getExplanation() {
        return this.explanation;
    }

    /**
     * 
     * @param explanation
     *     Textual description of the error based on the cause.
     *     This field is returned only if the cause is INVALID_REQUEST or SERVER_BUSY
     */
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    /**
     * Gets the <code>field</code> value.
     * 
     * 
     * @return
     *     Indicates the name of the field that failed validation.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public String field() {
        return field;
    }

    /**
     * Sets the <code>field</code> value.
     * 
     * 
     * @param field
     *     Indicates the name of the field that failed validation.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     * @return
     *     <code>this</code>, for invocation chaining.
     * @throws IllegalStateException
     *     If the object is in read-only mode
     */
    public Error field(String field) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.field = field;
        }
        return this;
    }

    /**
     * 
     * @return
     *     Indicates the name of the field that failed validation.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public String getField() {
        return this.field;
    }

    /**
     * 
     * @param field
     *     Indicates the name of the field that failed validation.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Gets the <code>supportCode</code> value.
     * 
     * 
     * @return
     *     Indicates the code that helps the support team to quickly identify the exact cause of the error.
     *     This field is returned only if the cause is SERVER_FAILED or REQUEST_REJECTED.
     */
    public String supportCode() {
        return supportCode;
    }

    /**
     * Sets the <code>supportCode</code> value.
     * 
     * 
     * @param supportCode
     *     Indicates the code that helps the support team to quickly identify the exact cause of the error.
     *     This field is returned only if the cause is SERVER_FAILED or REQUEST_REJECTED.
     * @return
     *     <code>this</code>, for invocation chaining.
     * @throws IllegalStateException
     *     If the object is in read-only mode
     */
    public Error supportCode(String supportCode) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.supportCode = supportCode;
        }
        return this;
    }

    /**
     * 
     * @return
     *     Indicates the code that helps the support team to quickly identify the exact cause of the error.
     *     This field is returned only if the cause is SERVER_FAILED or REQUEST_REJECTED.
     */
    public String getSupportCode() {
        return this.supportCode;
    }

    /**
     * 
     * @param supportCode
     *     Indicates the code that helps the support team to quickly identify the exact cause of the error.
     *     This field is returned only if the cause is SERVER_FAILED or REQUEST_REJECTED.
     */
    public void setSupportCode(String supportCode) {
        this.supportCode = supportCode;
    }

    /**
     * Gets the <code>validationType</code> value.
     * 
     * 
     * @return
     *     Indicates the type of field validation error.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public Error.ValidationType validationType() {
        return validationType;
    }

    /**
     * Sets the <code>validationType</code> value.
     * 
     * 
     * @param validationType
     *     Indicates the type of field validation error.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     * @return
     *     <code>this</code>, for invocation chaining.
     * @throws IllegalStateException
     *     If the object is in read-only mode
     */
    public Error validationType(Error.ValidationType validationType) {
        if (readOnly()) {
            throw new IllegalStateException("Cannot set values on readonly object");
        } else {
            this.validationType = validationType;
        }
        return this;
    }

    /**
     * 
     * @return
     *     Indicates the type of field validation error.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public Error.ValidationType getValidationType() {
        return this.validationType;
    }

    /**
     * 
     * @param validationType
     *     Indicates the type of field validation error.
     *     This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     */
    public void setValidationType(Error.ValidationType validationType) {
        this.validationType = validationType;
    }


    /**
     * Broadly categorizes the cause of the error.
     * For example, errors may occur due to invalid requests or internal system failures.
     * 
     */
    public static enum Cause {


        /**
         * The request was rejected due to security reasons such as firewall rules, expired certificate, etc.
         * 
         */
        REQUEST_REJECTED,

        /**
         * The request was rejected because it did not conform to the API protocol.
         * 
         */
        INVALID_REQUEST,

        /**
         * There was an internal system failure.
         * 
         */
        SERVER_FAILED,

        /**
         * The server did not have enough resources to process the request at the moment.
         * 
         */
        SERVER_BUSY;

    }


    /**
     * Indicates the type of field validation error.
     * This field is returned only if the cause is INVALID_REQUEST and a field level validation error was encountered.
     * 
     */
    public static enum ValidationType {


        /**
         * The request contained a field with a value that did not pass validation.
         * 
         */
        INVALID,

        /**
         * The request was missing a mandatory field.
         * 
         */
        MISSING,

        /**
         * The request contained a field that is unsupported.
         * 
         */
        UNSUPPORTED;

    }

}
