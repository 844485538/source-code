package java.lang;
import java.util.Random;

/**
* Math类包含执行基本数字运算的方法，如基本指数，对数，平方根和三角函数。
*/
public final class Math {

	/**
	 * Don't let anyone instantiate this class.
	 */
	private Math() {}

	// 定义常数e的值
	public static final double E = 2.7182818284590452354;

	// 定义Π值
	public static final double PI = 3.14159265358979323846;

	// 求指定double类型参数的正弦值
	public static double sin(double a) {
		return StrictMath.sin(a);
	}

	// 求指定double类型参数的余弦值
	public static double cos(double a) {
		return StrictMath.cos(a);
	}

	// 求指定double类型参数的正切值
	public static double tan(double a) {
		return StrictMath.tan(a);
	}

	// 求指定double类型参数的反正弦值
	public static double asin(double a) {
		return StrictMath.asin(a);h
	}

	// 求指定double类型参数的反余弦值
	public static double acos(double a) {
		return StrictMath.acos(a);
	}

	// 求指定double类型参数的反正切值
	public static double atan(double a) {
		return StrictMath.atan(a);
	}

	// 将角度转换为弧度
	public static double toRadians(double angdeg) {
		return angdeg / 180.0 * PI;
	}

	// 将参数转化为角度
	public static double toDegrees(double angrad) {
		return angrad * 180.0 / PI;
	}

	// 返回自然数底数e的参数次方
	public static double exp(double a) {
		return StrictMath.exp(a);
	}

	// 以e为底进行log计算
	public static double log(double a) {
		return StrictMath.log(a);

	public static double log10(double a) {
		return StrictMath.log10(a);
	}

	public static double sqrt(double a) {
		return StrictMath.sqrt(a);
	}


	public static double cbrt(double a) {
		return StrictMath.cbrt(a);
	}

	public static double IEEEremainder(double f1, double f2) {
		return StrictMath.IEEEremainder(f1, f2);、
	}

	public static double ceil(double a) {
		return StrictMath.ceil(a);
	}
	public static double floor(double a) {
		return StrictMath.floor(a);
	}

	public static double rint(double a) {
		return StrictMath.rint(a);
	}

	public static double atan2(double y, double x) {
		return StrictMath.atan2(y, x);
	}

	public static double pow(double a, double b) {
		return StrictMath.pow(a, b);
	}

	public static int round(float a) {
		return (int)floor(a + 0.5f);
	}

	public static long round(double a) {
		return (long)floor(a + 0.5d);
	}

	private static Random randomNumberGenerator;

	private static synchronized void initRNG() {
		if (randomNumberGenerator == null)
			randomNumberGenerator = new Random();
	}

	public static double random() {
		if (randomNumberGenerator == null) initRNG();
		return randomNumberGenerator.nextDouble();
	}

	// 返回值参数的绝对值 绝对值：absolute
	public static int abs(int a) {
		return (a < 0) ? -a : a;
	}

	// 返回值参数的绝对值
	public static long abs(long a) {
		return (a < 0) ? -a : a;
	}

	// 返回值参数的绝对值
	public static float abs(float a) {
		return (a <= 0.0F) ? 0.0F - a : a;
	}

	// 返回值参数的绝对值
	public static double abs(double a) {
		return (a <= 0.0D) ? 0.0D - a : a;
	}

	// 返回两个参数中的较大值
	public static int max(int a, int b) {
		return (a >= b) ? a : b;
	}

	// 返回两个参数中的较大值
	public static long max(long a, long b) {
		return (a >= b) ? a : b;
	}


	private static long negativeZeroFloatBits = Float.floatToIntBits(-0.0f);


	private static long negativeZeroDoubleBits = Double.doubleToLongBits(-0.0d);

	// 返回两个参数中的较大值
	public static float max(float a, float b) {
		// 当a为NaN时，a！=a （float a = Float.NaN;）
		if (a != a) return a;
		if ((a == 0.0f) && (b == 0.0f)
		        && (Float.floatToIntBits(a) == negativeZeroFloatBits)) {
			return b;
		}
		return (a >= b) ? a : b;
	}

	// 返回两个参数中的较大值
	public static double max(double a, double b) {
		// 当a为NaN时，a！=a （float a = Float.NaN;）
		if (a != a) return a;
		if ((a == 0.0d) && (b == 0.0d)
		        && (Double.doubleToLongBits(a) == negativeZeroDoubleBits)) {
			return b;
		}
		return (a >= b) ? a : b;
	}

	// 返回两个参数中的较小值
	public static int min(int a, int b) {
		return (a <= b) ? a : b;
	}

	// 返回两个参数中的较小值
	public static long min(long a, long b) {
		return (a <= b) ? a : b;
	}

	// 返回两个参数中的较小值
	public static float min(float a, float b) {
		if (a != a) return a;	// a is NaN
		if ((a == 0.0f) && (b == 0.0f)
		        && (Float.floatToIntBits(b) == negativeZeroFloatBits)) {
			return b;
		}
		return (a <= b) ? a : b;
	}

	// 返回两个参数中的较小值
	public static double min(double a, double b) {
		if (a != a) return a;
		if ((a == 0.0d) && (b == 0.0d)
		        && (Double.doubleToLongBits(b) == negativeZeroDoubleBits)) {
			return b;
		}
		return (a <= b) ? a : b;
	}

	// 当前double与参数double之间数值的个数（在计算机中两个数值间数值的个数是有限的） / 两个数的差值
	public static double ulp(double d) {
		return sun.misc.FpUtils.ulp(d);
	}

	// 当前float与参数float之间数值的个数（在计算机中两个数值间数值的个数是有限的） / 两个数的差值
	public static float ulp(float f) {
		return sun.misc.FpUtils.ulp(f);
	}

	// 如果参数为零，则为零，如果参数大于零则为1.0f，如果参数小于零，则为-1.0f
	public static double signum(double d) {
		return sun.misc.FpUtils.signum(d);
	}

	// 如果参数为零，则为零，如果参数大于零则为1.0f，如果参数小于零，则为-1.0f
	public static float signum(float f) {
		return sun.misc.FpUtils.signum(f);
	}

	// 返回的双曲正弦 double值
	public static double sinh(double x) {
		return StrictMath.sinh(x);
	}

	public static double cosh(double x) {
		return StrictMath.cosh(x);
	}

	public static double tanh(double x) {
		return StrictMath.tanh(x);
	}

	public static double hypot(double x, double y) {
		return StrictMath.hypot(x, y);
	}
	public static double expm1(double x) {
		return StrictMath.expm1(x);
	}

	public static double log1p(double x) {
		return StrictMath.log1p(x);
	}

	public static double copySign(double magnitude, double sign) {
		return sun.misc.FpUtils.rawCopySign(magnitude, sign);
	}

	public static float copySign(float magnitude, float sign) {
		return sun.misc.FpUtils.rawCopySign(magnitude, sign);
	}

	public static int getExponent(float f) {
		return sun.misc.FpUtils.getExponent(f);
	}

	public static int getExponent(double d) {
		return sun.misc.FpUtils.getExponent(d);
	}

	public static double nextAfter(double start, double direction) {
		return sun.misc.FpUtils.nextAfter(start, direction);
	}

	public static float nextAfter(float start, double direction) {
		return sun.misc.FpUtils.nextAfter(start, direction);
	}

	public static double nextUp(double d) {
		return sun.misc.FpUtils.nextUp(d);
	}

	public static float nextUp(float f) {
		return sun.misc.FpUtils.nextUp(f);
	}


	public static double scalb(double d, int scaleFactor) {
		return sun.misc.FpUtils.scalb(d, scaleFactor);
	}

	public static float scalb(float f, int scaleFactor) {
		return sun.misc.FpUtils.scalb(f, scaleFactor);
	}
}
