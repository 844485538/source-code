package java.lang;

/**
 * 静态类Number是以下类的父类
 * 		Byte
 * 		Short
 * 		Integer
 * 		Long
 * 		Float
 * 		Double
 * 		BigDecimal
 * 		BigInteger
 * 	Number子类必须提供将表示的数值转换为字节的方法
 *
 * @author	Lee Boynton
 * @author	Arthur van Hoff
 * @version 1.30, 11/17/05
 * @see     java.lang.Byte
 * @see     java.lang.Double
 * @see     java.lang.Float
 * @see     java.lang.Integer
 * @see     java.lang.Long
 * @see     java.lang.Short
 * @since   JDK1.0
 */
public abstract class Number implements java.io.Serializable {
	/**
	 * 将指定数字的值作为int返回，可能会涉及四舍五入.
	 */
	public abstract int intValue();

	/**
	 * 将指定数字的值作为long返回。可能会涉及四舍五入.
	 */
	public abstract long longValue();

	/**
	 * 将指定数字的值作为float返回。可能会涉及四舍五入.
	 */
	public abstract float floatValue();

	/**
	 * 将指定数字的值作为double返回。可能会涉及四舍五入.
	 */
	public abstract double doubleValue();

	/**
	 * 将指定数字的值作为byte返回。可能会涉及四舍五入.
	 */
	public byte byteValue() {
		return (byte)intValue();
	}

	/**
	 * 将指定数字的值作为short返回。可能会涉及四舍五入.
	 */
	public short shortValue() {
		return (short)intValue();
	}

	/** 用于序列化 */
	private static final long serialVersionUID = -8742448824652078965L;
}
