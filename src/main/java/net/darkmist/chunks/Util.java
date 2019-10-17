package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nullable;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.TooManyMethods","PMD.AvoidDuplicateLiterals","PMD.GodClass"})
final class Util
{
	private static final long LONG_INT_MAX_VALUE = Integer.MAX_VALUE;
	private static final long LONG_INVERTED_INT_MAX_VALUE = ~LONG_INT_MAX_VALUE;
	private static final int INT_INVERTED_INT_MAX_VALUE = ~(Integer.MAX_VALUE);
	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	private Util()
	{
	}

	static <E extends Exception> long requireValidOffset(long size, long off, Supplier<E> exceptionSupplier) throws E
	{
		if(0L<=off && off<size)
			return off;
		throw exceptionSupplier.get();
	}

	static long requireValidOffset(long size, long off)
	{
		//if(logger.isDebugEnabled())
			//logger.debug("requireValidOffset(size={} off={})", size, off);
		return requireValidOffset(size, off, IndexOutOfBoundsException::new);
	}

	static byte maskByte(long l)
	{
		return (byte)(l&0xffL);
	}

	static byte maskByte(int i)
	{
		return (byte)(i&0xff);
	}

	static boolean isExtendedByteValue(int i)
	{
		//return	
			//(i&0x00000180)==i	// 
			//||
			//(i&0x000000ff)==i;	// 0-255
		//return (i&0xff)==i	// 0-255
			//||
			//(i&0x00000070)!=0x0;	// -128-1
			/*
		|| (i&0x000000
			||
			(i&(INT_INVERTED_BYTE_MAX_VALUE<<1))==0;	// pos int unsigned byte value
			*/

		/*
		if(i|0xFFFFFF00==i)	// negitive int byte value
			return (byte)(i&0xff);
		if(i&INT_INVERTED_BYTE_MAX_VALUE==0)	// pos int unsigned byte value
			return (byte)i
		throw supplier.get();
		*/
		return (Byte.MIN_VALUE <= i && i<= 0xff);
	}

	static <E extends Exception> byte requireExtendedByteValue(int i, Supplier<E> exceptionSupplier) throws E
	{
		if(isExtendedByteValue(i))
			return (byte)(i&0xff);
		throw exceptionSupplier.get();
	}

	static byte requireExtendedByteValue(int i)
	{
		return requireExtendedByteValue(i, IllegalArgumentException::new);
	}

	static boolean isPosInt(int i)
	{
		return (i&INT_INVERTED_INT_MAX_VALUE) == 0;
	}

	static boolean isPosInt(long l)
	{	// HOT
		return (l&LONG_INVERTED_INT_MAX_VALUE)==0L;
	}

	@CanIgnoreReturnValue
	static <E extends Exception> long requirePos(long l, Supplier<E> exceptionSupplier) throws E
	{
		if(l>=0)
			return l;
		throw exceptionSupplier.get();
	}

	@CanIgnoreReturnValue
	static long requirePos(long l)
	{
		return requirePos(l, IllegalArgumentException::new);
	}

	@CanIgnoreReturnValue
	static <E extends Exception> int requirePosInt(int i, Supplier<E> exceptionSupplier) throws E
	{
		if(isPosInt(i))
			return i;
		throw exceptionSupplier.get();
	}

	@CanIgnoreReturnValue
	static <E extends Exception> int requirePosInt(int i) throws E
	{
		return requirePosInt(i, IllegalArgumentException::new);
	}

	@CanIgnoreReturnValue
	static <E extends Exception> int requirePosInt(long l, Supplier<E> exceptionSupplier) throws E
	{	// HOT
		if(isPosInt(l))
			return (int)l;
		throw exceptionSupplier.get();
	}

	@CanIgnoreReturnValue
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	static int requirePosInt(long l)
	{
		return requirePosInt(l, IllegalArgumentException::new);
	}

	static boolean isInt(long l)
	{
		return (l&LONG_INVERTED_INT_MAX_VALUE)==0L;
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
			//logger.debug("requireValidOffLenRetEnd(long,long,long): arrayLen={} off={} len={}", arrayLen, off, len);
		if(arrayLen < 0)
			 throw new IllegalArgumentException("Array length " + arrayLen + " is negative.");
		if(off < 0)
			 throw new IndexOutOfBoundsException("Array offset " + off + " is negative.");
		if(len < 0)
			 throw new IndexOutOfBoundsException("Sub array length " + len + " is negative.");
		if(arrayLen<off)
			throw new IndexOutOfBoundsException("Offset is " + off + " is larger then the array length " + arrayLen + '.');
		try
		{
			end = Math.addExact(off, len);
			//if(logger.isDebugEnabled())
				//logger.debug("end={}", end);
		}
		catch(ArithmeticException e)
		{
			IndexOutOfBoundsException ioobe = new IndexOutOfBoundsException("Offset " + off + " plus length " + len + " exceeds capacity of a long.");
			ioobe.initCause(e);
			throw ioobe;
		}
		if(arrayLen<end)
			throw new IndexOutOfBoundsException();
		return end;
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
			//logger.debug("requireValidOffLenRetEnd(int,int,int): arrayLen={} off={} len={}", arrayLen, off, len);
		if(arrayLen < 0)
			throw new IllegalArgumentException("Array length to check against cannot be negative. arrayLen=" + arrayLen + " off=" + off + " len=" + len + '.');
		if(off < 0)
			throw new IndexOutOfBoundsException("Array offset cannot be negative. arrayLen=" + arrayLen + " off=" + off + " len=" + len + '.');
		if(len < 0)
			throw new IndexOutOfBoundsException("Sub array length cannot be null. arrayLen=" + arrayLen + " off=" + off + " len=" + len + '.');
		if(arrayLen<off)
			throw new IndexOutOfBoundsException("Offset is larget then the array length. arrayLen=" + arrayLen + " off=" + off + " len=" + len + '.');
		try
		{
			end = Math.addExact(off, len);
		}
		catch(ArithmeticException e)
		{
			IndexOutOfBoundsException ioobe = new IndexOutOfBoundsException("Offset plus length exceeds capacity of a int. arrayLen=" + arrayLen + " off=" + off + " len=" + len + '.');
			ioobe.initCause(e);
			throw ioobe;
		}
		if(arrayLen<end)
			throw new IndexOutOfBoundsException("End of subarray " + end + " is greater than arrray size. arrayLen=" + arrayLen + " off=" + off + " len=" + len + '.');
		return end;
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

	@SuppressWarnings("CPD-END") 
	static void requireValidOffLen(long arrayLen, long off, long len)
	{
		requireValidOffLenRetEnd(arrayLen, off, len);
	}

	static void requireValidOffLen(long arrayLen, int off, int len)
	{
		requireValidOffLenRetEnd(arrayLen, off, len);
	}

	static void requireValidOffLen(byte[] array, int off, int len)
	{
		requireValidOffLenRetEnd(array, off, len);
	}

	@SuppressWarnings({"PMD.AvoidUsingShortType","UnnecessaryParentheses"})
	static short shortFromBytesBigEndian(int a, int b)
	{
		return	(short)
			(((a<<8)&0xff00)
			|((b   )&0x00ff));
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	static short shortFromBytesBigEndian(byte[] bytes)
	{
		return shortFromBytesBigEndian(bytes[0], bytes[1]);
	}

	@SuppressWarnings({"PMD.AvoidUsingShortType","UnnecessaryParentheses"})
	static short shortFromBytesLittleEndian(int a, int b)
	{
		return	(short)
			(((b<<8)&0xff00)
			|((a   )&0x00ff));
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	static short shortFromBytesLittleEndian(byte[] bytes)
	{
		return shortFromBytesLittleEndian(bytes[0], bytes[1]);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	static short shortFromBytes(int a, int b, ByteOrder order)
	{
		if(isBig(order))
			return shortFromBytesBigEndian(a,b);
		return shortFromBytesLittleEndian(a,b);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	static short shortFromBytes(byte[] bytes, ByteOrder order)
	{
		return shortFromBytes(bytes[0], bytes[1], order);
	}

	// INT

	@SuppressWarnings("UnnecessaryParentheses")
	static int intFromBytesBigEndian(int a, int b, int c, int d)
	{
		return	 ((a<<24)&0xff000000)
			|((b<<16)&0x00ff0000)
			|((c<<8) &0x0000ff00)
			|((d)    &0x000000ff);
	}

	static int intFromBytesBigEndian(byte[] bytes)
	{
		return intFromBytesBigEndian(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	@SuppressWarnings("UnnecessaryParentheses")
	static int intFromBytesLittleEndian(int a, int b, int c, int d)
	{
		return	 ((d<<24)&0xff000000)
			|((c<<16)&0x00ff0000)
			|((b<<8) &0x0000ff00)
			|((a)    &0x000000ff);
	}

	static int intFromBytesLittleEndian(byte[] bytes)
	{
		return intFromBytesLittleEndian(bytes[0], bytes[1], bytes[2], bytes[3]);
	}

	static int intFromBytes(int a, int b, int c, int d, ByteOrder order)
	{
		if(isBig(order))
			return intFromBytesBigEndian(a,b,c,d);
		return intFromBytesLittleEndian(a,b,c,d);
	}

	static int intFromBytes(byte[] bytes, ByteOrder order)
	{
		return intFromBytes(bytes[0], bytes[1], bytes[2], bytes[3], order);
	}

	// LONG

	@SuppressWarnings("UnnecessaryParentheses")
	static long longFromBytesBigEndian(long a, long b, long c, long d, long e, long f, long g, long h)
	{
		return	 ((a<<56)&0xff00000000000000L)
			|((b<<48)&0x00ff000000000000L)
			|((c<<40)&0x0000ff0000000000L)
			|((d<<32)&0x000000ff00000000L)
			|((e<<24)&0x00000000ff000000L)
			|((f<<16)&0x0000000000ff0000L)
			|((g<< 8)&0x000000000000ff00L)
			|((h    )&0x00000000000000ffL);
	}

	static long longFromBytesBigEndian(byte[] bytes)
	{
		return longFromBytesBigEndian(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
	}

	@SuppressWarnings("UnnecessaryParentheses")
	static long longFromBytesLittleEndian(long a, long b, long c, long d, long e, long f, long g, long h)
	{
		return 	 ((h<<56)&0xff00000000000000L)
			|((g<<48)&0x00ff000000000000L)
			|((f<<40)&0x0000ff0000000000L)
			|((e<<32)&0x000000ff00000000L)
			|((d<<24)&0x00000000ff000000L)
			|((c<<16)&0x0000000000ff0000L)
			|((b<< 8)&0x000000000000ff00L)
			|((a    )&0x00000000000000ffL);
	}

	static long longFromBytesLittleEndian(byte[] bytes)
	{
		return longFromBytesLittleEndian(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7]);
	}

	static long longFromBytes(long a, long b, long c, long d, long e, long f, long g, long h, ByteOrder order)
	{
		if(isBig(order))
			return longFromBytesBigEndian(a,b,c,d,e,f,g,h);
		return longFromBytesLittleEndian(a,b,c,d,e,f,g,h);
	}

	static long longFromBytes(byte[] bytes, ByteOrder order)
	{
		return longFromBytes(bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], order);
	}

	static boolean isBig(ByteOrder order)
	{
		return order==null || order==ByteOrder.BIG_ENDIAN;
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	static short fromBig(short s, ByteOrder order)
	{
		if(isBig(order))
			return s;
		return Short.reverseBytes(s);
	}

	static int fromBig(int i, ByteOrder order)
	{
		if(isBig(order))
			return i;
		return Integer.reverseBytes(i);
	}

	static long fromBig(long l, ByteOrder order)
	{
		if(isBig(order))
			return l;
		return Long.reverseBytes(l);
	}

	static byte[] maskedBytesFrom(long a, long b, long c, long d, long e, long f, long g, long h)
	{
		return new byte[]
		{
			maskByte(a),
			maskByte(b),
			maskByte(c),
			maskByte(d),
			maskByte(e),
			maskByte(f),
			maskByte(g),
			maskByte(h)
		};
	}

	static byte[] maskedBytesFrom(int a, int b, int c, int d)
	{
		return new byte[]
		{
			maskByte(a),
			maskByte(b),
			maskByte(c),
			maskByte(d)
		};
	}

	static byte[] maskedBytesFrom(int a, int b)
	{
		return new byte[]
		{
			maskByte(a),
			maskByte(b)
		};
	}

	// Get big endian bytes.
	static byte[] bytesFrom(long l)
	{
		return maskedBytesFrom(
			l>>56,
			l>>48,
			l>>40,
			l>>32,
			l>>24,
			l>>16,
			l>>8,
			l
		);
	}

	@SuppressWarnings("PMD.AvoidReassigningParameters")
	static byte[] bytesFrom(long l, ByteOrder bo)
	{
		if(bo==ByteOrder.LITTLE_ENDIAN)
			return bytesFrom(Long.reverseBytes(l));
		return bytesFrom(l);
	}

	// Get big endian bytes.
	static byte[] bytesFrom(int i)
	{
		return maskedBytesFrom(
			i>>24,
			i>>16,
			i>>8,
			i
		);
	}

	@SuppressWarnings("PMD.AvoidReassigningParameters")
	static byte[] bytesFrom(int i, ByteOrder bo)
	{
		if(bo==ByteOrder.LITTLE_ENDIAN)
			return bytesFrom(Integer.reverseBytes(i));
		return bytesFrom(i);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	static byte[] bytesFrom(short s)
	{
		return maskedBytesFrom(
			s>>8,
			s
		);
	}

	@SuppressWarnings({"PMD.AvoidUsingShortType","PMD.AvoidReassigningParameters"})
	static byte[] bytesFrom(short s, ByteOrder bo)
	{
		if(bo==ByteOrder.LITTLE_ENDIAN)
			return bytesFrom(Short.reverseBytes(s));
		return bytesFrom(s);
	}

	@SuppressFBWarnings(value="PZLA_PREFER_ZERO_LENGTH_ARRAYS", justification="I want null if allocation failed.")
	@Nullable
	static byte[] guardedAllocateBytes(int size)
	{	// Who knows if this would actually work but why not try.
		try
		{
			return new byte[size];
		}
		catch(OutOfMemoryError e)
		{
			logger.debug("Failed to allocate " + size + " large byte array.", e);
			return null;
		}
	}
}
