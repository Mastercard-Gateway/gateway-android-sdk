
package com.mastercard.gateway.android.sdkold;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

public abstract class Payload
    implements Serializable
{

    private transient boolean readOnly;
    private transient Payload __parent;

    public abstract void validate()
        throws ValidationException
    ;

    /**
     * 
     * @param b
     *     <code>true</code> to enable read-only mode
     */
    public Payload readOnly(boolean b) {
        this.readOnly = b;
        return this;
    }

    /**
     * 
     * @return
     *     <code>true</code> if this object is in read-only mode
     */
    public boolean readOnly() {
        return this.readOnly;
    }

    protected String __thisPath() {
        return (__parent == null ? "" : __parent.__fieldPath( this ) + ".");
    }

    protected String __fieldPath(Object value) {
        String path = __thisPath();
        try {
            for (Field field: this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(this);
                if (fieldValue == value) {
                    return (path + field.getName());
                }
                if (fieldValue instanceof List<?> ) {
                    List<?> list = ((List<?> ) fieldValue);
                    int index = list.indexOf(value);
                    if (index >= 0) {
                        return (path + field.getName() + "[" + index + "]");
                    }
                }
            }
        } catch (Exception e) {
            return e.getMessage();
        }
        return (path +"unknown");
    }

    public void __setParent(Payload __parent) {
        this.__parent = __parent;
    }

}
