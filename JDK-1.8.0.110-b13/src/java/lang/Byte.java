package java.lang;

public final class Byte extends Number implements Comparable<Byte> {

	// 定义Byte对象的最小值：-128
	public static final byte   MIN_VALUE = -128;

	// 定义Byte对象的最大值：127
	public static final byte   MAX_VALUE = 127;

	/**
	 * 表示是基本类型byte对应的对象
	 */
	@SuppressWarnings("unchecked")
	public static final Class<Byte>  TYPE = (Class<Byte>) Class.getPrimitiveClass("byte");

	public static String toString(byte b) {
		return Integer.toString((int)b, 10);
	}

	// Byte对象的缓存池 -128 - 127 之间的数值，在自动装箱的时候会自动获取缓存中的对象，超出范围则创建新的对象。
	private static class ByteCache {
		private ByteCache(){}

		// 建立缓存数组，因为byte最多128*2个值（-(-128)表示负数个数，127表示正数个数，1表示0的个数）
		static final Byte cache[] = new Byte[-(-128) + 127 + 1];

		static {
			// 缓存初始化，范围是-128到127
			for(int i = 0; i < cache.length; i++)
				cache[i] = new Byte((byte)(i - 128));
		}
	}

	// 根据基本类型byte值从缓存数组中获取对应Byte对象的值（因为缓存数组中有负数，所以要加上128）
	public static Byte valueOf(byte b) {
		final int offset = 128;
		return ByteCache.cache[(int)b + offset];
	}

	// 将radix进制的String类型数字s，转换为10进制byte数字
	public static byte parseByte(String s, int radix)
			throws NumberFormatException {
		int i = Integer.parseInt(s, radix);
		if (i < MIN_VALUE || i > MAX_VALUE)
			throw new NumberFormatException(
					"Value out of range. Value:\"" + s + "\" Radix:" + radix);
		return (byte)i;
	}

	// String -> byte 默认String为10进制数字
	public static byte parseByte(String s) throws NumberFormatException {
		return parseByte(s, 10);
	}

	// 返回一个十进制Byte对象，值为radix进制的s转化为10进制
	public static Byte valueOf(String s, int radix)
			throws NumberFormatException {
		return valueOf(parseByte(s, radix));
	}

	// String -> Byte  默认String为10进制数字
	public static Byte valueOf(String s) throws NumberFormatException {
		return valueOf(s, 10);
	}

	// 根据String返回一个Byte对象
	public static Byte decode(String nm) throws NumberFormatException {
		int i = Integer.decode(nm);
		if (i < MIN_VALUE || i > MAX_VALUE)
			throw new NumberFormatException(
					"Value " + i + " out of range from input " + nm);
		return valueOf((byte)i);
	}

	// 用byte存储数据
	private final byte value;

	// byte -> Byte
	public Byte(byte value) {
		this.value = value;
	}

	// String -> Byte
	public Byte(String s) throws NumberFormatException {
		this.value = parseByte(s, 10);
	}

	// 以byte类型返回Byte的值
	public byte byteValue() {
		return value;
	}

	// 以short类型返回Byte的值
	public short shortValue() {
		return (short)value;
	}

	// 以int类型返回Byte的值
	public int intValue() {
		return (int)value;
	}

	// 以long类型返回Byte的值
	public long longValue() {
		return (long)value;
	}

	// 以float类型返回Byte的值
	public float floatValue() {
		return (float)value;
	}

	// 以double类型返回Byte的值
	public double doubleValue() {
		return (double)value;
	}

	// 以String类型返回Byte的值
	public String toString() {
		return Integer.toString((int)value);
	}

	// 返回hash值
	@Override
	public int hashCode() {
		return Byte.hashCode(value);
	}

	// 返回hash值
	public static int hashCode(byte value) {
		return (int)value;
	}

	// 判断两个Byte对象的值是否相等
	public boolean equals(Object obj) {
		if (obj instanceof Byte) {
			return value == ((Byte)obj).byteValue();
		}
		return false;
	}

	// 比较当前Byte对象与另一Byte对象的差值，当前对象-参数对象
	public int compareTo(Byte anotherByte) {
		return compare(this.value, anotherByte.value);
	}

	public static int compare(byte x, byte y) {
		return x - y;
	}

	/**
	 *	将byte通过无符号转换转换为int
	 *	0xff -> 255 -> 11111111
	 *	& 两个数都转为二进制，然后从高位开始比较，如果两个数都为1则为1，否则为0。
	 * 	将一个有符号位的byte数值，转换为无符号位对应的数值
	 *	运行过程：x -> x的2进制补码 与 0xff进行 &操作得出结果。
			例如  x = -123 	00000000 00000000 00000000 10000101
			0xff=		   	00000000 00000000 00000000 11111111
							00000000 00000000 00000000 10000101 = 133
	 */
	public static int toUnsignedInt(byte x) {
		return ((int) x) & 0xff;
	}


	// 将byte通过无符号转换转换为long
	public static long toUnsignedLong(byte x) {
		return ((long) x) & 0xffL;
	}


	// 表示一个byte值的二进制位数（补码）
	public static final int SIZE = 8;

	// 表示一个byte值的二进制字节数（补码）
	public static final int BYTES = SIZE / Byte.SIZE;

	// 用于序列化
	private static final long serialVersionUID = -7183698231559129828L;
}
