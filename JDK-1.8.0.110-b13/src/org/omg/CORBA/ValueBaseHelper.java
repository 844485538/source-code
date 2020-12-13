/*
 * @(#)ValueBaseHelper.java	1.13 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
 /**
 * The Helper for <tt>ValueBase</tt>.  For more information on 
 * Helper files, see <a href="doc-files/generatedfiles.html#helper">
 * "Generated Files: Helper Files"</a>.<P>
 */
 
/*
 * @(#)ValueBaseHelper.java	1.13 05/11/17
 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package org.omg.CORBA;

abstract public class ValueBaseHelper
{
    private static String  _id = "IDL:omg.org/CORBA/ValueBase:1.0";

    public static void insert (org.omg.CORBA.Any a, java.io.Serializable that)
    {
	org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
	a.type (type ());
	write (out, that);
	a.read_value (out.create_input_stream (), type ());
    }

    public static java.io.Serializable extract (org.omg.CORBA.Any a)
    {
	return read (a.create_input_stream ());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
	if (__typeCode == null)
	    {
		__typeCode = org.omg.CORBA.ORB.init ().get_primitive_tc (TCKind.tk_value);
	    }
	return __typeCode;
    }

    public static String id ()
    {
	return _id;
    }

    public static java.io.Serializable read (org.omg.CORBA.portable.InputStream istream)
    {
	return ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value ();
    }

    public static void write (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable value)
    {
	((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value (value);
    }


}