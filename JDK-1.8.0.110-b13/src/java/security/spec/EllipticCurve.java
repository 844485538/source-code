/*
 * @(#)EllipticCurve.java	1.5 06/03/28
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.security.spec;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * This immutable class holds the necessary values needed to represent 
 * an elliptic curve.
 *
 * @see ECField
 * @see ECFieldFp
 * @see ECFieldF2m
 *
 * @author Valerie Peng
 * @version 1.5, 03/28/06
 *
 * @since 1.5
 */
public class EllipticCurve {
    
    private final ECField field;
    private final BigInteger a;
    private final BigInteger b;
    private final byte[] seed;

    // Check coefficient c is a valid element in ECField field.
    private static void checkValidity(ECField field, BigInteger c,
	String cName) { 
	// can only perform check if field is ECFieldFp or ECFieldF2m. 
	if (field instanceof ECFieldFp) {
	    BigInteger p = ((ECFieldFp)field).getP();
	    if (p.compareTo(c) != 1) {
		throw new IllegalArgumentException(cName + " is too large");
	    } else if (c.signum() < 0) {
		throw new IllegalArgumentException(cName + " is negative");
	    }
	} else if (field instanceof ECFieldF2m) {
	    int m = ((ECFieldF2m)field).getM();
	    if (c.bitLength() > m) {
		throw new IllegalArgumentException(cName + " is too large");
	    } 
	}
    }

    /**
     * Creates an elliptic curve with the specified elliptic field
     * <code>field</code> and the coefficients <code>a</code> and
     * <code>b</code>.
     * @param field the finite field that this elliptic curve is over.
     * @param a the first coefficient of this elliptic curve.
     * @param b the second coefficient of this elliptic curve.
     * @exception NullPointerException if <code>field</code>,
     * <code>a</code>, or <code>b</code> is null.
     * @exception IllegalArgumentException if <code>a</code>
     * or <code>b</code> is not null and not in <code>field</code>.
     */
    public EllipticCurve(ECField field, BigInteger a, 
			 BigInteger b) {
	this(field, a, b, null);
    }    

    /**
     * Creates an elliptic curve with the specified elliptic field
     * <code>field</code>, the coefficients <code>a</code> and
     * <code>b</code>, and the <code>seed</code> used for curve generation.
     * @param field the finite field that this elliptic curve is over.
     * @param a the first coefficient of this elliptic curve.
     * @param b the second coefficient of this elliptic curve.
     * @param seed the bytes used during curve generation for later
     * validation. Contents of this array are copied to protect against
     * subsequent modification.
     * @exception NullPointerException if <code>field</code>,
     * <code>a</code>, or <code>b</code> is null.
     * @exception IllegalArgumentException if <code>a</code>
     * or <code>b</code> is not null and not in <code>field</code>.
     */
    public EllipticCurve(ECField field, BigInteger a, 
			 BigInteger b, byte[] seed) {
        if (field == null) {
            throw new NullPointerException("field is null");
        }
        if (a == null) {
	    throw new NullPointerException("first coefficient is null");
	}
        if (b == null) {
            throw new NullPointerException("second coefficient is null");
        }
        checkValidity(field, a, "first coefficient");
	checkValidity(field, b, "second coefficient");
        this.field = field;
        this.a = a;
        this.b = b;
	if (seed != null) {
	    this.seed = (byte[]) seed.clone();
	} else {
	    this.seed = null;
	}
    }
 
    /**
     * Returns the finite field <code>field</code> that this 
     * elliptic curve is over.
     * @return the field <code>field</code> that this curve 
     * is over.
     */    
    public ECField getField() {
	return field;
    }

    /**
     * Returns the first coefficient <code>a</code> of the
     * elliptic curve.
     * @return the first coefficient <code>a</code>.
     */
    public BigInteger getA() {
	return a;
    }

    /**
     * Returns the second coefficient <code>b</code> of the
     * elliptic curve.
     * @return the second coefficient <code>b</code>.
     */
    public BigInteger getB() {
	return b;
    }

    /**
     * Returns the seeding bytes <code>seed</code> used 
     * during curve generation. May be null if not specified.
     * @return the seeding bytes <code>seed</code>. A new
     * array is returned each time this method is called.
     */
    public byte[] getSeed() {
	if (seed == null) return null;
	else return (byte[]) seed.clone();
    }

    /**
     * Compares this elliptic curve for equality with the
     * specified object. 
     * @param obj the object to be compared.
     * @return true if <code>obj</code> is an instance of
     * EllipticCurve and the field, A, B, and seeding bytes 
     * match, false otherwise.
     */
    public boolean equals(Object obj) {
	if (this == obj) return true;
	if (obj instanceof EllipticCurve) {
	    EllipticCurve curve = (EllipticCurve) obj;
	    if ((field.equals(curve.field)) &&
		(a.equals(curve.a)) &&
		(b.equals(curve.b)) &&
		(Arrays.equals(seed, curve.seed))) {
		return true;
	    } 
	}
	return false;
    }

    /**
     * Returns a hash code value for this elliptic curve.
     * @return a hash code value.
     */
    public int hashCode() {
	return (field.hashCode() << 6 + 
	    (a.hashCode() << 4) +
	    (b.hashCode() << 2) + 
	    (seed==null? 0:seed.length));
    }
}
