/*
 * @(#)StringIndexOutOfBoundsException.java	1.23 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang;

/**
 * Thrown by <code>String</code> methods to indicate that an index
 * is either negative or greater than the size of the string.  For
 * some methods such as the charAt method, this exception also is
 * thrown when the index is equal to the size of the string.
 *
 * @author  unascribed
 * @version 1.23, 11/17/05
 * @see     java.lang.String#charAt(int)
 * @since   JDK1.0
 */
public
class StringIndexOutOfBoundsException extends IndexOutOfBoundsException {
    /**
     * Constructs a <code>StringIndexOutOfBoundsException</code> with no 
     * detail message. 
     *
     * @since   JDK1.0.
     */
    public StringIndexOutOfBoundsException() {
	super();
    }

    /**
     * Constructs a <code>StringIndexOutOfBoundsException</code> with 
     * the specified detail message. 
     *
     * @param   s   the detail message.
     */
    public StringIndexOutOfBoundsException(String s) {
	super(s);
    }

    /**
     * Constructs a new <code>StringIndexOutOfBoundsException</code> 
     * class with an argument indicating the illegal index. 
     *
     * @param   index   the illegal index.
     */
    public StringIndexOutOfBoundsException(int index) {
	super("String index out of range: " + index);
    }
}
