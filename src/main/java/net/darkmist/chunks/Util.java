package net.darkmist.chunks;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Util
{
	private static final Logger logger = LoggerFactory.getLogger(Util.class);

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
	static long requireValidOffLenRetEnd(long arrayLen, long off, long len)
	{
		long end;

		if(logger.isDebugEnabled())
			logger.debug("arrayLen={} off={} len={}", arrayLen, off, len);
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
			if(logger.isDebugEnabled())
				logger.debug("end={}", end);
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

		if(logger.isDebugEnabled())
			logger.debug("arrayLen={} off={} len={}", arrayLen, off, len);
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
			if(logger.isDebugEnabled())
				logger.debug("end={}", end);
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
}
