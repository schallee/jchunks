package net.darkmist.chunks;

import java.nio.ByteOrder;

import static java.util.Objects.requireNonNull;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
final class Util
{
	//private static final Logger logger = LoggerFactory.getLogger(Util.class);

	private Util()
	{
	}

	/**
	 * Validate an offset and length against an array length.
	 * @param arrayLen Length of the array
	 * @param off Offset of the initial position in the array.
	 * @param len Length of the proposed sub-array.
	 * @return The index of the last entry in the array plus
	 * 	<code>1</code>.
	 * @throws IllegalArgumentException if arrayLen is negative.
	 * @throws IndexOutOfBoundsException if the sub-array would be
	 * 	outside the bounds of the array length.
	 */
	@SuppressWarnings("CPD-START")
	// CPD doesn't like that this and the int version are the same.
	// PMD is complaining about end being undefined after the catch. The catch always throws so this isn't an issue.
	static long requireValidOffLenRetEnd(long arrayLen, long off, long len)
	{
		long end;

		//if(logger.isDebugEnabled())
			//logger.debug("arrayLen={} off={} len={}", arrayLen, off, len);
		if(arrayLen < 0)
			 throw new IllegalArgumentException("Array length to check against cannot be negative.");
		if(off < 0)
			 throw new IndexOutOfBoundsException("Array offset cannot be negative.");
		if(len < 0)
			 throw new IndexOutOfBoundsException("Sub array length cannot be null.");
		if(arrayLen<off)
			throw new IndexOutOfBoundsException("Offset is larget then the array length.");
		try
		{
			end = Math.addExact(off, len);
			//if(logger.isDebugEnabled())
				//logger.debug("end={}", end);
		}
		catch(ArithmeticException e)
		{
			IndexOutOfBoundsException ioobe = new IndexOutOfBoundsException("Offset plus length exceeds capacity of a long.");
			ioobe.initCause(e);
			throw ioobe;
		}
		if(end<0||arrayLen<end)
			throw new IndexOutOfBoundsException();
		return end;
	}

	@SuppressWarnings("CPD-END") 
	static void requireValidOffLen(long arrayLen, long off, long len)
	{
		requireValidOffLenRetEnd(arrayLen, off, len);
	}

	/**
	 * Validate an offset and length against an array length.
	 * @param arrayLen Length of the array
	 * @param off Offset of the initial position in the array.
	 * @param len Length of the proposed sub-array.
	 * @return The index of the last entry in the array plus
	 * 	<code>1</code>.
	 * @throws IllegalArgumentException if arrayLen is negative.
	 * @throws IndexOutOfBoundsException if the sub-array would be
	 * 	outside the bounds of the array length.
	 */
	static int requireValidOffLenRetEnd(int arrayLen, int off, int len)
	{
		int end;

		//if(logger.isDebugEnabled())
			//logger.debug("arrayLen={} off={} len={}", arrayLen, off, len);
		if(arrayLen < 0)
			 throw new IllegalArgumentException("Array length to check against cannot be negative.");
		if(off < 0)
			 throw new IndexOutOfBoundsException("Array offset cannot be negative.");
		if(len < 0)
			 throw new IndexOutOfBoundsException("Sub array length cannot be null.");
		if(arrayLen<off)
			throw new IndexOutOfBoundsException("Offset is larget then the array length.");
		try
		{
			end = Math.addExact(off, len);
			//if(logger.isDebugEnabled())
				//logger.debug("end={}", end);
		}
		catch(ArithmeticException e)
		{
			IndexOutOfBoundsException ioobe = new IndexOutOfBoundsException("Offset plus length exceeds capacity of a int.");
			ioobe.initCause(e);
			throw ioobe;
		}
		if(end<0||arrayLen<end)
			throw new IndexOutOfBoundsException();
		return end;
	}

	static void requireValidOffLen(long arrayLen, int off, int len)
	{
		requireValidOffLenRetEnd(arrayLen, off, len);
	}

	/**
	 * Validate an offset and length against an array.
	 * @param array Array to validate against.
	 * @param off Offset of the initial position in the array.
	 * @param len Length of the proposed sub-array.
	 * @return The index of the last byte in the array plus
	 * 	<code>1</code>.
	 * @throws NullPointerException if array is <code>null</code>.
	 * @throws IndexOutOfBoundsException if the sub-array would be
	 * 	outside the bounds of the array length.
	 */
	static int requireValidOffLenRetEnd(byte[] array, int off, int len)
	{
		return requireValidOffLenRetEnd(requireNonNull(array).length, off, len);
	}

	static void requireValidOffLen(byte[] array, int off, int len)
	{
		requireValidOffLenRetEnd(array, off, len);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static short shortFromBytesBigEndian(int a, int b)
	{
		return	(short)
			(((a<<8)&0xff00)
			|((b   )&0x00ff));
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static short shortFromBytesLittleEndian(int a, int b)
	{
		return	(short)
			(((b<<8)&0xff00)
			|((a   )&0x00ff));
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static short shortFromBytes(int a, int b, ByteOrder order)
	{
		short  ret;

		if(order == null || order.equals(ByteOrder.BIG_ENDIAN))
			ret = shortFromBytesBigEndian(a,b);
		else
			return shortFromBytesLittleEndian(a,b);
		//if(logger.isDebugEnabled())
			//logger.debug("{}", String.format("SHort %s: a=%02x, b=%02x, ret=%04x=%d", order, a, b, ret, ret));
		return ret;
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static short shortFromBytesBigEndian(byte[] bytes)
	{
		return shortFromBytesBigEndian(bytes[0], bytes[1]);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static short shortFromBytesLittleEndian(byte[] bytes)
	{
		return shortFromBytesLittleEndian(bytes[0], bytes[1]);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	public static short shortFromBytes(byte[] bytes, ByteOrder order)
	{
		return shortFromBytes(bytes[0], bytes[1], order);
	}

	// INT

	public static int intFromBytesBigEndian(int a, int b, int c, int d)
	{
		return	 ((a<<24)&0xff000000)
			|((b<<16)&0x00ff0000)
			|((c<<8) &0x0000ff00)
			|((d)    &0x000000ff);
	}

	public static int intFromBytesLittleEndian(int a, int b, int c, int d)
	{
		return	 ((d<<24)&0xff000000)
			|((c<<16)&0x00ff0000)
			|((b<<8) &0x0000ff00)
			|((a)    &0x000000ff);
	}

	public static int intFromBytes(int a, int b, int c, int d, ByteOrder order)
	{
		int ret;
		if(order == null || order.equals(ByteOrder.BIG_ENDIAN))
			ret = intFromBytesBigEndian(a,b,c,d);
		else
			ret = intFromBytesLittleEndian(a,b,c,d);
		//if(logger.isDebugEnabled())
			//logger.debug("{}", String.format("a=%02x, b=%02x, c=%02x, d=%02x, order=%s, ret=%08x=%d", a, b, c, d, order, ret, ret));
		return ret;
	}

	public static int intFromBytesBigEndian(byte[] bytes)
	{
		return intFromBytesBigEndian(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	public static int intFromBytesLittleEndian(byte[] bytes)
	{
		return intFromBytesLittleEndian(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	public static int intFromBytes(byte[] bytes, ByteOrder order)
	{
		return intFromBytes(bytes[0], bytes[1], bytes[2], bytes[3], order);
	}

	// LONG

	public static long longFromBytesBigEndian(long a, long b, long c, long d, long e, long f, long g, long h)
	{
		return	 ((a<<56)&0xff00000000000000l)
			|((b<<48)&0x00ff000000000000l)
			|((c<<40)&0x0000ff0000000000l)
			|((d<<32)&0x000000ff00000000l)
			|((e<<24)&0x00000000ff000000l)
			|((f<<16)&0x0000000000ff0000l)
			|((g<< 8)&0x000000000000ff00l)
			|((h    )&0x00000000000000ffl);
	}

	public static long longFromBytesLittleEndian(long a, long b, long c, long d, long e, long f, long g, long h)
	{
		long ret;

		ret =	 ((h<<56)&0xff00000000000000l)
			|((g<<48)&0x00ff000000000000l)
			|((f<<40)&0x0000ff0000000000l)
			|((e<<32)&0x000000ff00000000l)
			|((d<<24)&0x00000000ff000000l)
			|((c<<16)&0x0000000000ff0000l)
			|((b<< 8)&0x000000000000ff00l)
			|((a    )&0x00000000000000ffl);
		//if(logger.isDebugEnabled())
			//logger.debug("{}", String.format("a=%02x, b=%02x, c=%02x, d=%02x, e=%02x, f=%02x, g=%02x, h=%02x ret=%016x", a, b, c, d, e, f, g, h, ret));
		return ret;
	}

	public static long longFromBytes(long a, long b, long c, long d, long e, long f, long g, long h, ByteOrder order)
	{
		long ret;

		if(order == null || order.equals(ByteOrder.BIG_ENDIAN))
			ret = longFromBytesBigEndian(a,b,c,d,e,f,g,h);
		else
			ret = longFromBytesLittleEndian(a,b,c,d,e,f,g,h);
		//if(logger.isDebugEnabled())
			//logger.debug("{}", String.format("a=%02x, b=%02x, c=%02x, d=%02x, e=%02x, f=%02x, g=%02x, h=%02x order=%s ret=%016x", a, b, c, d, e, f, g, h, order, ret));
		return ret;
	}

	public static long longFromBytesBigEndian(byte[] bytes)
	{
		return longFromBytesBigEndian(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
	}

	public static long longFromBytesLittleEndian(byte[] bytes)
	{
		return longFromBytesLittleEndian(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
	}

	public static long longFromBytes(byte[] bytes, ByteOrder order)
	{
		return longFromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], order);
	}
}
