package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.*;

import org.opentest4j.AssertionFailedError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate and validate test cases for sequences of bytes in a chunk. Sequences are consecutive byte values from 0.
 *
 * This is a helper class for other tests and has no junit tests itself.
 */
final class TestSources
{
	private static final Logger logger = LoggerFactory.getLogger(TestSources.class);

	private TestSources()
	{
	}

	/**
	 * Stream of byte at offset tests with adjustment to be checked with {@link #byteValueAt(Chunk,byte,long)}.
	 * @param chunk Chunk to produce tests for.
	 * @param valueAdjust Value to add to the expected value for the test. This allows offsets like in {@link SubChunkTest#streamByteAtOffArgs()}.
	 * @return Stream of test arguments.
	 */
	static Stream<Arguments> streamByteAtOffArgs(Chunk chunk, int valueAdjust)
	{
		return LongStream.range(0l,chunk.getSize())
			.mapToObj((off)->Arguments.of(chunk, (byte)((off+valueAdjust)&0xff), off));
	}

	/**
	 * Stream of byte at offset tests to be checked with {@link #byteValueAt(Chunk,byte,long)}.
	 * @param chunk Chunk to produce tests for.
	 * @return Stream of test arguments.
	 */
	static Stream<Arguments> streamByteAtOffArgs(Chunk chunk)
	{
		return streamByteAtOffArgs(chunk, 0);
	}

	/**
	 * Validate that a byte in a chunk is as expected.
	 */
	static void byteValueAt(Chunk chunk, byte expected, long off)
	{
		byte actual;

		actual = (byte)chunk.getByte(off);
		assertEquals(expected, actual, ()->String.format("At offset %d, expected 0x%02x but got 0x%02x", off, expected, actual));
	}
	
	@SuppressWarnings("UnnecessaryParentheses")
	static Stream<Arguments> streamShortAtOffArgs(Chunk chunk, int valueAdjust)
	{
		if(chunk.getSize()<Short.BYTES)
			return Stream.empty();
		return LongStream.rangeClosed(0l,chunk.getSize()-Short.BYTES)
			.mapToObj((off)->Arguments.of(
				chunk,
				Util.shortFromBytes((byte)((off+valueAdjust)&0xff), (byte)(((off+1+valueAdjust))&0xff), ByteOrder.BIG_ENDIAN),
				off));
	}

	static Stream<Arguments> streamShortAtOffArgs(Chunk chunk)
	{
		return streamShortAtOffArgs(chunk,0);
	}

	static void shortValueAt(Chunk chunk, short expected, long off)
	{
		short actual;

		actual = chunk.getShort(off, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual, ()->String.format("At offset %d, expected 0x%04x but got 0x%04x", off, expected, actual));
	}

	static Stream<Arguments> streamIntAtOffArgs(Chunk chunk, int valueAdjust)
	{
		if(chunk.getSize()<Integer.BYTES)
			return Stream.empty();
		return LongStream.rangeClosed(0l,chunk.getSize()-Integer.BYTES)
			.mapToObj((off)->Arguments.of(
				chunk,
				Util.intFromBytes(
					(byte)((off+valueAdjust)&0xff),
					(byte)((off+valueAdjust+1)&0xff),
					(byte)((off+valueAdjust+2)&0xff),
					(byte)((off+valueAdjust+3)&0xff),
					ByteOrder.BIG_ENDIAN),
				off));
	}

	static Stream<Arguments> streamIntAtOffArgs(Chunk chunk)
	{
		return streamIntAtOffArgs(chunk, 0);
	}

	static void intValueAt(Chunk chunk, int expected, long off)
	{
		int actual;

		actual = chunk.getInt(off, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual, ()->String.format("At offset %d of %s: expected 0x%08x but got 0x%08x", off, chunk, expected, actual));
	}

	static Stream<Arguments> streamLongAtOffArgs(Chunk chunk, long valueAdjust)
	{
		if(chunk.getSize()<Long.BYTES)
			return Stream.empty();
		return LongStream.rangeClosed(0l,chunk.getSize()-Long.BYTES)
			.mapToObj((off)->Arguments.of(
				chunk,
				Util.longFromBytes(
					(byte)((off+valueAdjust)&0xff),
					(byte)((off+valueAdjust+1)&0xff),
					(byte)((off+valueAdjust+2)&0xff),
					(byte)((off+valueAdjust+3)&0xff),
					(byte)((off+valueAdjust+4)&0xff),
					(byte)((off+valueAdjust+5)&0xff),
					(byte)((off+valueAdjust+6)&0xff),
					(byte)((off+valueAdjust+7)&0xff),
					ByteOrder.BIG_ENDIAN),
				off));
	}

	static Stream<Arguments> streamLongAtOffArgs(Chunk chunk)
	{
		return streamLongAtOffArgs(chunk,0l);
	}

	static void longValueAt(Chunk chunk, long expected, long off)
	{
		long actual;

		actual = chunk.getLong(off, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual, ()->String.format("At offset %d of %s: expected 0x%08x but got 0x%08x", off, chunk, expected, actual));
	}

	static byte[] mkTestArray(int len)
	{
		byte[] bytes = new byte[len];

		for(int i=0;i<len;i++)
			bytes[i]=(byte)(i&0xff);
		return bytes;
	}

	static byte[] mkTestArray(long len)
	{
		return mkTestArray(Util.requirePosInt(len));
	}

	static Chunk mkTestChunk(int len)
	{
		return Chunks.give(mkTestArray(len));
	}

	static Chunk mkTestChunk(long len)
	{
		return Chunks.give(mkTestArray(len));
	}

	static void validateTestSubArray(int chunkOff, int copyLen, int arrayOff, byte[] bytes, int valueAdjust)
	{
		int arrayEnd = Math.addExact(arrayOff, copyLen);
		for(int i=chunkOff + valueAdjust,j=arrayOff;j<arrayEnd;i++,j++)
			assertEquals((byte)(i&0xff),bytes[j]);
	}

	static void validateTestSubArray(int chunkOff, int copyLen, int arrayOff, byte[] bytes)
	{
		validateTestSubArray(chunkOff, copyLen, arrayOff, bytes, 0);
	}

	static void validateTestSubArray(long chunkOff, int copyLen, int arrayOff, byte[] bytes, int valueAdjust)
	{
		validateTestSubArray(Util.requirePosInt(chunkOff), copyLen, arrayOff, bytes, valueAdjust);
	}

	static void validateTestSubArray(long chunkOff, int copyLen, int arrayOff, byte[] bytes)
	{
		validateTestSubArray(Util.requirePosInt(chunkOff), copyLen, arrayOff, bytes);
	}

	static byte[] mkTestSubArrayValueAdjusted(int off, int len, int valueAdjust)
	{
		byte[] bytes = new byte[len];

		for(int i=off+valueAdjust,j=0;j<len;i++,j++)
			bytes[j]=(byte)(i&0xff);
		return bytes;
	}

	static byte[] mkTestSubArray(int off, int len)
	{
		return mkTestSubArrayValueAdjusted(off, len, 0);
	}

	static byte[] mkTestSubArrayValueAdjusted(long off, long len, int valueAdjust)
	{
		return mkTestSubArrayValueAdjusted(Util.requirePosInt(off), Util.requirePosInt(len), valueAdjust);
	}

	static byte[] mkTestSubArray(long off, long len)
	{
		return mkTestSubArrayValueAdjusted(off, len, 0);
	}

	static Chunk mkTestSubChunkValueAdjusted(int off, int len, int valueAdjust)
	{
		return Chunks.give(mkTestSubArrayValueAdjusted(off,len,valueAdjust));
	}

	static Chunk mkTestSubChunk(int off, int len)
	{
		return mkTestSubChunkValueAdjusted(off, len, 0);
	}

	static Chunk mkTestSubChunkValueAdjusted(long off, long len, int valueAdjust)
	{
		return Chunks.give(mkTestSubArrayValueAdjusted(off,len,valueAdjust));
	}

	static Chunk mkTestSubChunk(long off, long len)
	{
		return mkTestSubChunkValueAdjusted(off, len, 0);
	}

	static Stream<Arguments> streamSubChunkArgLenAdjusted(Chunk chunk, long len, int valueAdjust)
	{
		if(chunk.getSize()<len)
			return Stream.empty();
		return LongStream.rangeClosed(0, chunk.getSize()-len)
			.mapToObj((off)->Arguments.of(
				chunk,
				off,
				len,
				valueAdjust
			)
		);
	}

	static Stream<Arguments> streamSubChunkArgLen(Chunk chunk, long len)
	{
		if(chunk.getSize()<len)
			return Stream.empty();
		return LongStream.rangeClosed(0, chunk.getSize()-len)
			.mapToObj((off)->Arguments.of(
				chunk,
				off,
				len
			)
		);
	}

	static Stream<Arguments> streamSubChunkArgAdjusted(Chunk chunk, int valueAdjust)
	{
		return Stream.concat(
			LongStream.rangeClosed(0l, chunk.getSize())
				.boxed()
				.flatMap((len)->streamSubChunkArgLenAdjusted(chunk,len, valueAdjust)),
			Stream.of(Arguments.of(chunk,0l,chunk.getSize(), valueAdjust))
		);
	}

	static Stream<Arguments> streamSubChunkArg(Chunk chunk)
	{
		return Stream.concat(
			LongStream.rangeClosed(0l, chunk.getSize())
				.boxed()
				.flatMap((len)->streamSubChunkArgLen(chunk,len)),
			Stream.of(Arguments.of(chunk,0l,chunk.getSize()))
		);
	}

	static void subChunkAtFor(Chunk chunk, long off, long len, int valueAdjust)
	{
		Chunk expected;
		Chunk actual;

		expected = mkTestSubChunkValueAdjusted(off, len, valueAdjust);
		actual = chunk.subChunk(off,len);
		try
		{
			assertEquals(expected, actual, ()->String.format("Off=%d len=%d expected.getSize()=%d actual.getSize()=%d\n\t\t\t   chunk=%s\n\t\t\texpected=%s\n\t\t\t  actual=%s\n\t\t\t", off, len, expected.getSize(), actual.getSize(), chunk, expected, actual));
		}
		catch(AssertionFailedError e)
		{
			logger.error("Failed: ", e);
			throw e;
		}
	}

	static void subChunkAtFor(Chunk chunk, long off, long len)
	{
		subChunkAtFor(chunk, off, len, 0);
	}

	static Stream<Arguments> streamCopyToArgLen(Chunk chunk, int len, int valueAdjust)
	{
		if(chunk.size()<len)
			return Stream.empty();
		return LongStream.rangeClosed(0, chunk.size()-len)
			.boxed()
			.flatMap((chunkOff)->
				Stream.of(
					// Array exact
					Arguments.of(
						chunk,
						chunkOff,
						0,
						len,
						len,
						valueAdjust
					),
					// array middle
					Arguments.of(
						chunk,
						chunkOff,
						1,
						len+2,
						len,
						valueAdjust
					),
					// array begin
					Arguments.of(
						chunk,
						chunkOff,
						0,
						len+1,
						len,
						valueAdjust
					),
					// array end
					Arguments.of(
						chunk,
						chunkOff,
						1,
						len+1,
						len,
						valueAdjust
					)
				)
			);
	}

	static Stream<Arguments> streamCopyToArgLen(Chunk chunk, int len)
	{
		if(logger.isDebugEnabled())
			logger.debug("streamCopyToArgLen(chunk={}, len={})", chunk, len);
		if(chunk.size()<len)
			return Stream.empty();
		return LongStream.rangeClosed(0, chunk.size()-len)
			.boxed()
			.flatMap((chunkOff)->
				Stream.of(
					// Array exact
					Arguments.of(
						chunk,
						chunkOff,
						0,
						len,
						len
					),
					// array middle
					Arguments.of(
						chunk,
						chunkOff,
						1,
						len+2,
						len
					),
					// array begin
					Arguments.of(
						chunk,
						chunkOff,
						0,
						len+1,
						len
					),
					// array end
					Arguments.of(
						chunk,
						chunkOff,
						1,
						len+1,
						len
					)
				)
			);
	}

	static Stream<Arguments> streamCopyToArg(Chunk chunk, int valueAdjust)
	{
		Util.requirePosInt(chunk.getSize());
		if(logger.isDebugEnabled())
			logger.debug("streamCopyToArg(chunk={}, valueAdjust={}): chunk.size()={}", chunk, valueAdjust, chunk.size());
		return IntStream.rangeClosed(0, chunk.size())
				.boxed()
				.flatMap((len)->streamCopyToArgLen(chunk,(int)len, valueAdjust));
	}

	static Stream<Arguments> streamCopyToArg(Chunk chunk)
	{
		Util.requirePosInt(chunk.getSize());
		if(logger.isDebugEnabled())
			logger.debug("streamCopyToArg(chunk={}): chunk.size()={}", chunk, chunk.size());
		return IntStream.rangeClosed(0, chunk.size())
				.boxed()
				.flatMap((len)->streamCopyToArgLen(chunk,(int)len));
	}

	static void copyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int arrayLen, int copyLen, int valueAdjust)
	{
		byte[] dst;
		byte[] actual;

		if(logger.isDebugEnabled())
			logger.debug("chunk={} chunkOff={} arrayOff={} copyLen={}", chunk, chunkOff, arrayOff, copyLen);
		dst = new byte[arrayLen];
		try
		{
			actual = chunk.copyTo(dst, chunkOff, arrayOff, copyLen);
			assertTrue(dst==actual);
			validateTestSubArray(chunkOff, copyLen, arrayOff, dst, valueAdjust);
		}
		catch(AssertionFailedError|IndexOutOfBoundsException|ArithmeticException e)
		{	// So the exceptions surefire is displaying aren't synced with the log output.
			logger.debug("chunk={} chunkOff={} arrayOff={} copyLen={}", chunk, chunkOff, arrayOff, copyLen, e);
			throw e;
		}
	}

	static void copyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int arrayLen, int copyLen)
	{
		copyToAtFor(chunk, chunkOff, arrayOff, arrayLen, copyLen, 0);
	}

	static Stream<Arguments> streamFailCopyToArg(Chunk chunk)
	{
		return Stream.concat(
			Stream.of(
				// arrayOff <0		chunkOff:	arrayOff:		argLen:		arrayLen:
				Arguments.of(chunk,	0l,		-1,			chunk.size(),	chunk.size()),
				Arguments.of(chunk,	0l,		Integer.MIN_VALUE,	chunk.size(),	chunk.size()),
				// arrayOff>=bytes.len
				Arguments.of(chunk,	0l,		chunk.size()-1,		chunk.size()-1,	0),
				// bytes.len < aOff+len
				Arguments.of(chunk,	0l,		0,			chunk.size()-1,	0),
				// arrayLen==0 &&
				// arrayOff > bytes.length
				Arguments.of(chunk,	0l,		2,			0,		1)
			),
			chunk.size() > 2 ?
			Stream.of(
				// bytes.len < aOff+len
				Arguments.of(chunk,	0l,		0,			chunk.size()-1,	1)
			) : Stream.empty()
		);
	}

	static void failCopyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int argLen, int arrayLen)
	{
		byte[] dst;
		byte[] actual;

		if(logger.isDebugEnabled())
			logger.debug("chunk={} chunkOff={} arrayOff={} argLen={} arrayLen={}", chunk, chunkOff, arrayOff, argLen, arrayLen);
		dst = new byte[arrayLen];
		// FIXME: We need to check non=zero array offsets!
		try
		{
			actual = chunk.copyTo(dst, chunkOff, arrayOff, argLen);
			fail(String.format("Expected chunk=%s copyTo chunkOff=%d arrayOff=%d argLen=%d arrayLen=%d to fail but it did not. Resulting dst=%s and actual=%s", chunk, chunkOff, arrayOff, argLen, arrayLen, Arrays.toString(dst), Arrays.toString(actual)));
		}
		catch(ArithmeticException | IndexOutOfBoundsException e)
		{
			logger.debug("expected exception", e);
		}
	}

}
