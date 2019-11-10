package java.lang;

public final class Short extends Number implements Comparable<Short> {

    // short 最小值
    public static final short   MIN_VALUE = -32768;

    // short 最大值
    public static final short   MAX_VALUE = 32767;

    /**
     * 表示是基本类型short对应的对象
     */
    @SuppressWarnings("unchecked")
    public static final Class<Short>    TYPE = (Class<Short>) Class.getPrimitiveClass("short");

    public static String toString(short s) {
        return Integer.toString((int)s, 10);
    }

    // 将一个radix进制的数字s 转换为10进制short值输出
    public static short parseShort(String s, int radix)
        throws NumberFormatException {
        int i = Integer.parseInt(s, radix);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                "Value out of range. Value:\"" + s + "\" Radix:" + radix);
        return (short)i;
    }

    // 将一个10进制的数字s 转换为10进制short值输出
    public static short parseShort(String s) throws NumberFormatException {
        return parseShort(s, 10);
    }

    // 返回一个Short对象，值为radix进制s转换为10进制后的值
    public static Short valueOf(String s, int radix)
        throws NumberFormatException {
        return valueOf(parseShort(s, radix));
    }

    public static Short valueOf(String s) throws NumberFormatException {
        return valueOf(s, 10);
    }

    // Short对象缓存池 -128 - 127 之间的数值，在自动装箱的时候会自动获取缓存中的对象，超出范围则创建新的对象。
    private static class ShortCache {
        private ShortCache(){}

        static final Short cache[] = new Short[-(-128) + 127 + 1];

        static {
            for(int i = 0; i < cache.length; i++)
                cache[i] = new Short((short)(i - 128));
        }
    }

    // 根据short值返回一个Short对象，若缓存池中存在对应对象则返回，不存在则创建一个新对象返回。
    public static Short valueOf(short s) {
        final int offset = 128;
        int sAsInt = s;
        if (sAsInt >= -128 && sAsInt <= 127) { // must cache
            return ShortCache.cache[sAsInt + offset];
        }
        return new Short(s);
    }

    // 根据String返回一个Short对象
    public static Short decode(String nm) throws NumberFormatException {
        int i = Integer.decode(nm);
        if (i < MIN_VALUE || i > MAX_VALUE)
            throw new NumberFormatException(
                    "Value " + i + " out of range from input " + nm);
        return valueOf((short)i);
    }

    // 用于存储Short对象的值
    private final short value;

    public Short(short value) {
        this.value = value;
    }

    public Short(String s) throws NumberFormatException {
        this.value = parseShort(s, 10);
    }

    // 将Short对象转换为byte
    public byte byteValue() {
        return (byte)value;
    }

    // 以short类型返回Short对象的值
    public short shortValue() {
        return value;
    }

    // 以int类型返回Short对象的值
    public int intValue() {
        return (int)value;
    }

    // 以long类型返回Short对象的值
    public long longValue() {
        return (long)value;
    }

    // 以float类型返回Short对象的值
    public float floatValue() {
        return (float)value;
    }

    // 以double类型返回Short对象的值
    public double doubleValue() {
        return (double)value;
    }

    public String toString() {
        return Integer.toString((int)value);
    }

    // 返回Short对象的hash值
    @Override
    public int hashCode() {
        return Short.hashCode(value);
    }

    // 返回Short对象的hash值 
    public static int hashCode(short value) {
        return (int)value;
    }

    // 判断对象的值是否相等
    public boolean equals(Object obj) {
        if (obj instanceof Short) {
            return value == ((Short)obj).shortValue();
        }
        return false;
    }

    // 比较当前Short 对象与另一Short对象的差值：当前对象-参数对象
    public int compareTo(Short anotherShort) {
        return compare(this.value, anotherShort.value);
    }

    public static int compare(short x, short y) {
        return x - y;
    }

    // Short对象的二进制位数
    public static final int SIZE = 16;

    // // Short对象的字节数
    public static final int BYTES = SIZE / Byte.SIZE;

    public static short reverseBytes(short i) {
        return (short) (((i & 0xFF00) >> 8) | (i << 8));
    }

    /**
    *   将short通过无符号转换转换为int
    *   0xff -> 255 -> 11111111
    *   & 两个数都转为二进制，然后从高位开始比较，如果两个数都为1则为1，否则为0。
    *   将一个有符号位的byte数值，转换为无符号位对应的数值
    *   运行过程：x -> x的2进制补码 与 0xff进行 &操作得出结果。
            例如  x = -123   00000000 00000000 00000000 10000101
            0xffff=         00000000 00000000 11111111 11111111
                            00000000 00000000 00000000 10000101 = 133
    */
    public static int toUnsignedInt(short x) {
        return ((int) x) & 0xffff;
    }

    // 将short通过无符号转换转换为long
    public static long toUnsignedLong(short x) {
        return ((long) x) & 0xffffL;
    }

    // 用于序列化
    private static final long serialVersionUID = 7515723908773894738L;
}

