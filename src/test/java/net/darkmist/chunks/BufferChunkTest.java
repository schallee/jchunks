package net.darkmist.chunks;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BufferChunkTest
{
	private static final Logger logger = LoggerFactory.getLogger(BufferChunkTest.class);

	private static Stream<Chunk> testBufferChunks()
	{
		return LongStream.of(2L,4L,6L,8L,12L,24L)
			.mapToObj(TestSources::mkTestArray)
			.map(Chunks::giveBytes);
	}

	public static Stream<Arguments> streamByteAtOffArgs()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamByteAtOffArgs);
	}

	@ParameterizedTest
	@MethodSource("streamByteAtOffArgs")
	public void byteValueAt(Chunk chunk, byte expected, long off)
	{
		TestSources.byteValueAt(chunk,expected,off);
	}

	public static Stream<Arguments> streamByteAtOffArgsCoalesced()
	{
		return testBufferChunks()
			.map(Chunk::coalesce)
			.flatMap(TestSources::streamByteAtOffArgs);
	}

	@ParameterizedTest
	@MethodSource("streamByteAtOffArgsCoalesced")
	public void byteValueAtCoalesced(Chunk chunk, byte expected, long off)
	{
		TestSources.byteValueAt(chunk,expected,off);
	}

	public static Stream<Arguments> streamShortAtOffArgs()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamShortAtOffArgs);
	}

	@ParameterizedTest
	@MethodSource("streamShortAtOffArgs")
	public void shortValueAt(Chunk chunk, short expected, long off)
	{
		TestSources.shortValueAt(chunk, expected, off);
	}

	public static Stream<Arguments> streamIntAtOffArgs()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamIntAtOffArgs);
	}

	@ParameterizedTest
	@MethodSource("streamIntAtOffArgs")
	public void intValueAt(Chunk chunk, int expected, long off)
	{
		TestSources.intValueAt(chunk, expected, off);
	}

	public static Stream<Arguments> streamLongAtOffArgs()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamLongAtOffArgs);
	}

	@ParameterizedTest
	@MethodSource("streamLongAtOffArgs")
	public void longValueAt(Chunk chunk, long expected, long off)
	{
		TestSources.longValueAt(chunk, expected, off);
	}

	public static Stream<Arguments> streamSubChunkArg()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamSubChunkArg);
	}

	@ParameterizedTest
	@MethodSource("streamSubChunkArg")
	public void subChunkAtFor(Chunk chunk, long off, long len)
	{
		TestSources.subChunkAtFor(chunk, off, len);
	}

	public static Stream<Arguments> streamCopyToArg()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamCopyToArg);
	}

	@ParameterizedTest
	@MethodSource("streamCopyToArg")
	public void copyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int arrayLen, int copyLen)
	{
		TestSources.copyToAtFor(chunk, chunkOff, arrayOff, arrayLen, copyLen);
	}

	public static Stream<Arguments> streamFailCopyToArg()
	{
		return testBufferChunks()
			.flatMap(TestSources::streamFailCopyToArg);
	}

	@ParameterizedTest
	@MethodSource("streamFailCopyToArg")
	public void failCopyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int argLen, int arrayLen)
	{
		TestSources.failCopyToAtFor(chunk, chunkOff, arrayOff, argLen, arrayLen);
	}


	@Test
	public void giveInstanceBufNull()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;
		ByteBuffer in=null;

		actual = BufferChunkSPI.giveInstance(in);
		assertEquals(expected, actual);
	}

	@Test
	public void giveInstanceBufEmpty()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;
		ByteBuffer in=ByteBuffer.wrap(new byte[0]);

		actual = BufferChunkSPI.giveInstance(in);
		assertEquals(expected, actual);
	}

	public static Stream<Arguments> streamGiveInstanceBytes()
	{
		return Stream.of(
				Arguments.of(Chunks.empty(), null),
				Arguments.of(Chunks.empty(), new byte[0]),
				Arguments.of(Chunks.ofByte(0), new byte[]{0})
		);
	}

	@ParameterizedTest
	@MethodSource("streamGiveInstanceBytes")
	public void giveInstanceBytesOffLen(Chunk expected, byte[] array)
	{
		Chunk actual;

		actual = BufferChunkSPI.giveInstance(array);
		assertEquals(expected, actual, ()->String.format("Expected giveInstance(array=%s) to return %s but recieved %s instead.", Arrays.toString(array), expected, actual));
	}

	@ParameterizedTest
	@MethodSource("streamGiveInstanceBytesOffLen")
	public void giveInstanceBytesOffLen(Chunk expected, byte[] array, int off, int len)
	{
		Chunk actual;

		actual = BufferChunkSPI.giveInstance(array,off,len);
		assertEquals(expected, actual, ()->String.format("Expected giveInstance(array=%s,off=%d,len=%d) to return %s but recieved %s instead.", Arrays.toString(array), off, len, expected, actual));
	}

	@ParameterizedTest
	@MethodSource("streamGiveInstanceBytesOffLenFail")
	public void giveInstanceBytesOffLenFail(byte[] array, int off, int len)
	{
		try
		{
			Chunk actual = BufferChunkSPI.giveInstance(array, off, len);
			fail("Expected giveInstance(array=" + (array==null?"null":Arrays.toString(array)) +" ,off=" + off + " ,len={}) to throw an exception but got " + actual + " instaed.");
		}
		catch(NullPointerException | IndexOutOfBoundsException e)
		{
			logger.debug("expected", e);
		}
	}

	public static Stream<Arguments> streamGiveInstanceBytesOffLen()
	{
		return Stream.of(
				Arguments.of(Chunks.empty(), null,0,0),
				Arguments.of(Chunks.empty(), new byte[0],0,0),
				Arguments.of(Chunks.empty(), new byte[]{0},0,0),
				Arguments.of(Chunks.empty(), new byte[]{0},1,0),
				Arguments.of(Chunks.ofByte(0), new byte[]{0},0,1),
				Arguments.of(Chunks.ofBytes(0,1), new byte[]{0,1},0,2)
		);
	}

	public static Stream<Arguments> streamGiveInstanceBytesOffLenFail()
	{
		return Stream.of(
				Arguments.of(null,0,1),
				Arguments.of(null,1,0),
				Arguments.of(null,1,1)
		);
	}

	public static Stream<Arguments> streamCopyInstanceBuf()
	{
		return Stream.of(
				Arguments.of(null, Chunks.empty()),
				Arguments.of(ByteBuffer.wrap(new byte[0]), Chunks.empty()),
				Arguments.of(ByteBuffer.wrap(new byte[]{0}), Chunks.ofByte(0)),
				// We don't want Chunks.of to just call copyInstance do we?
				Arguments.of(ByteBuffer.wrap(new byte[]{0,1}), Chunks.ofChunks(Chunks.ofByte(0), Chunks.ofByte(1)))
			);
	}

	@ParameterizedTest
	@MethodSource("streamCopyInstanceBuf")
	public void copyInstanceBuf(ByteBuffer buf, Chunk expected)
	{
		Chunk actual;

		actual = BufferChunkSPI.copyInstance(buf);
		assertEquals(expected, actual, ()->String.format("Expected copyInstance(buf=%s) to return %s but recieved %s instead.", buf, expected, actual));
	}

	public static Stream<Arguments> streamCopyInstanceBytes()
	{
		return Stream.of(
				Arguments.of(Chunks.empty(), null),
				Arguments.of(Chunks.empty(), new byte[0]),
				Arguments.of(Chunks.ofByte(0), new byte[]{0}),
				Arguments.of(Chunks.empty(), null),
				Arguments.of(Chunks.empty(), new byte[0]),
				Arguments.of(Chunks.ofByte(0), new byte[]{0})
		);
	}

	@ParameterizedTest
	@MethodSource("streamCopyInstanceBytes")
	public void copyInstanceBytesOffLen(Chunk expected, byte[] array)
	{
		Chunk actual;

		actual = BufferChunkSPI.copyInstance(array);
		assertEquals(expected, actual, ()->String.format("Expected copyInstance(array=%s) to return %s but recieved %s instead.", Arrays.toString(array), expected, actual));
	}

	@ParameterizedTest
	@MethodSource("streamCopyInstanceBytesOffLen")
	public void copyInstanceBytesOffLen(Chunk expected, byte[] array, int off, int len)
	{
		Chunk actual;

		actual = BufferChunkSPI.copyInstance(array,off,len);
		assertEquals(expected, actual, ()->String.format("Expected copyInstance(array=%s,off=%d,len=%d) to return %s but recieved %s instead.", Arrays.toString(array), off, len, expected, actual));
	}

	@ParameterizedTest
	@MethodSource("streamCopyInstanceBytesOffLenFail")
	public void copyInstanceBytesOffLenFail(byte[] array, int off, int len)
	{
		try
		{
			Chunk actual = BufferChunkSPI.copyInstance(array, off, len);
			fail("Expected copyInstance(array=" + (array==null?"null":Arrays.toString(array)) +" ,off=" + off + " ,len={}) to throw an exception but got " + actual + " instaed.");
		}
		catch(NullPointerException | IndexOutOfBoundsException e)
		{
			logger.debug("expected", e);
		}
	}

	public static Stream<Arguments> streamCopyInstanceBytesOffLen()
	{
		return Stream.of(
				Arguments.of(Chunks.empty(), null,0,0),
				Arguments.of(Chunks.empty(), new byte[0],0,0),
				Arguments.of(Chunks.empty(), new byte[]{0},0,0),
				Arguments.of(Chunks.empty(), new byte[]{0},1,0),
				Arguments.of(Chunks.ofByte(0), new byte[]{0},0,1),
			 	Arguments.of(Chunks.ofBytes(0,1), new byte[]{0,1},0,2)
		);
	}

	public static Stream<Arguments> streamCopyInstanceBytesOffLenFail()
	{
		return Stream.of(
				Arguments.of(null,0,1),
				Arguments.of(null,1,0),
				Arguments.of(null,1,1)
		);
	}

	public static Stream<Arguments> streamIsCoalesced()
	{
		return Stream.of(
			Arguments.of(BufferChunkSPI.giveInstance(new byte[]{1,2,3},0,3),true),
			Arguments.of(BufferChunkSPI.giveInstance(new byte[]{1,2,3},1,2),false),
			Arguments.of(BufferChunkSPI.giveInstance(new byte[]{1,2,3},0,2),false)
		);
	}

	@ParameterizedTest
	@MethodSource("streamIsCoalesced")
	public void testIsCoalesced(Chunk chunk, boolean expected)
	{
		boolean actual;

		actual = chunk.isCoalesced();
		assertEquals(expected, actual, ()->{return "Expected chunk=" + chunk + "'s  isCoalesced() to return " + expected + " but it returned " + actual + " instead.";});
	}

	public static Stream<Chunk> streamCoalesce()
	{
		return Stream.of(
			BufferChunkSPI.giveInstance(new byte[]{1,2,3},1,2),
			BufferChunkSPI.giveInstance(new byte[]{1,2,3},0,2)
		);
	}

	@ParameterizedTest
	@MethodSource("streamCoalesce")
	public void testCoalesce(Chunk chunk)
	{
		Chunk actual;

		actual = chunk.coalesce();
		assertEquals(chunk, actual);
		assertNotSame(chunk, actual);
	}

	@Test
	public void subBufferCopyTo()
	{
		Chunk parentChunk = BufferChunkSPI.giveInstance(new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08});
		byte[] childBytes = new byte[]{0x02,0x03,0x04,0x05,0x06,0x07};
		Chunk childChunk = BufferChunkSPI.copyInstance(childBytes);
		Chunk childSubChunk = parentChunk.subChunk(1,6);
		byte[] dst = new byte[6];


		assertEquals(childChunk, childSubChunk);
		childSubChunk.copyTo(dst,0L,0,6);
		assertArrayEquals(childBytes, dst);
	}

	@Test
	public void testToString()
	{
		assertNotNull(Chunks.ofBytes(0,1,2,3,4,5,6,7,8,9,0xa,0xb,0xc,0xd,0xe,0xf).getSPI().toString());
	}
}
