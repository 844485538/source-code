/*
 * @(#)StringValue.java	1.1 09/02/26
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.lang;

import java.util.Arrays;

/**
 * 该类完全由静态方法组成，这些方法对字符串用于存储值的字符数组进行操作
 */

class StringValue {
    private StringValue() { }

    /**
     * 返回一个char[]，该数组是给定char[]的副本.
     */
    static char[] from(char[] value) {
        return Arrays.copyOf(value, value.length);
    }   
}
