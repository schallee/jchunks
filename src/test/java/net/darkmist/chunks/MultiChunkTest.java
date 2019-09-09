package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnnecessaryParentheses")
public class MultiChunkTest
{
	@SuppressWarnings("UnusedVariable")
	private static final Logger logger = LoggerFactory.getLogger(MultiChunkTest.class);

	@Test
	void threeByteChunkSequence()
	{
		Chunk a = Chunks.ofByte(0);
		Chunk b = Chunks.ofByte(1);
		Chunk c = Chunks.ofByte(2);
		Chunk multi = Chunks.of(a,b,c);
		Chunk sub;

		assertEquals(3,multi.getSize());
		assertEquals(a.getByte(0), multi.getByte(0));
		assertEquals(b.getByte(0), multi.getByte(1));
		assertEquals(c.getByte(0), multi.getByte(2));
		assertEquals(0x0001, multi.getShort(0, ByteOrder.BIG_ENDIAN));
		assertEquals(0x0102, multi.getShort(1, ByteOrder.BIG_ENDIAN));

		sub=multi.subChunk(0,1);
		assertEquals(a,sub);
		sub=multi.subChunk(1,1);
		assertEquals(b,sub);
		sub=multi.subChunk(2,1);
		assertEquals(c,sub);
	}

	private static Chunk threeByteMultiChunk()
	{
		Chunk a = Chunks.ofByte(0);
		Chunk b = Chunks.ofByte(1);
		Chunk c = Chunks.ofByte(2);
		Chunk multi = Chunks.of(a,b,c);

		assertEquals(Byte.BYTES * 3,multi.size());
		return multi;
	}

	private static Chunk threeShortMultiChunk()
	{
		Chunk a = Chunks.from((short)0x0001);
		Chunk b = Chunks.from((short)0x0203);
		Chunk c = Chunks.from((short)0x0405);
		Chunk multi = Chunks.of(a,b,c);

		assertEquals(Short.BYTES*3,multi.size());
		return multi;
	}

	private static Chunk threeIntMultiChunk()
	{
		Chunk a = Chunks.from(0x00010203);
		Chunk b = Chunks.from(0x04050607);
		Chunk c = Chunks.from(0x08090a0b);
		Chunk multi = Chunks.of(a,b,c);

		assertEquals(Integer.BYTES*3,multi.size());
		return multi;
	}

	private static Chunk threeLongMultiChunk()
	{
		Chunk a = Chunks.from(0x0001020304050607l);
		Chunk b = Chunks.from(0x08090a0b0c0d0e0fl);
		Chunk c = Chunks.from(0x1011121314151617l);
		Chunk multi = Chunks.of(a,b,c);

		assertEquals(Long.BYTES*3,multi.size());
		return multi;
	}

	private static Stream<Chunk> testMultiChunks()
	{
		return Stream.of(
			threeByteMultiChunk(),
			threeShortMultiChunk(),
			threeIntMultiChunk(),
			threeLongMultiChunk()
		);
	}

	@ParameterizedTest
	@MethodSource("testMultiChunks")
	public void isCoalesced(Chunk chunk)
	{
		assertEquals(chunk.getSize()>Integer.MAX_VALUE,chunk.isCoalesced());
	}

	public static Stream<Arguments> streamByteAtOffArgs()
	{
		return testMultiChunks()
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
		return testMultiChunks()
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
		return testMultiChunks()
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
		return testMultiChunks()
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
		return testMultiChunks()
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
		return testMultiChunks()
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
		return testMultiChunks()
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
		return testMultiChunks()
			.flatMap(TestSources::streamFailCopyToArg);
	}

	@ParameterizedTest
	@MethodSource("streamFailCopyToArg")
	public void failCopyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int argLen, int arrayLen)
	{
		TestSources.failCopyToAtFor(chunk, chunkOff, arrayOff, argLen, arrayLen);
	}

	@Test
	public void testChunksListEmpty()
	{
		Chunk expected = Chunks.empty();
		List<Chunk> input = Collections.emptyList();
		Chunk actual;

		actual = Chunks.of(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testChunksListNull()
	{
		Chunk expected = Chunks.empty();
		List<Chunk> input = null;
		Chunk actual;

		actual = Chunks.of(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testChunksArrayEmpty()
	{
		Chunk expected = Chunks.empty();
		Chunk[] input = new Chunk[0];
		Chunk actual;

		actual = Chunks.of(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testChunksArrayNull()
	{
		Chunk expected = Chunks.empty();
		Chunk[] input = null;
		Chunk actual;

		actual = Chunks.of(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testInstanceWithNull()
	{
		Chunk first = Chunks.ofByte((byte)0);
		Chunk last = Chunks.ofByte((byte)(0xff));
		Chunk expected = Chunks.ofBytes((byte)0, (byte)(0xff));
		Chunk actual;

		actual = Chunks.of(first, null, last);
		assertEquals(expected, actual);
	}

	@Test
	public void testInstanceWithEmptyTwoBytes()
	{
		Chunk first = Chunks.ofByte((byte)0);
		Chunk last = Chunks.ofByte((byte)(0xff));
		Chunk expected = Chunks.ofBytes((byte)0, (byte)(0xff));
		Chunk actual;

		actual = Chunks.of(first, Chunks.empty(), last);
		assertEquals(expected, actual);
	}

	@Test
	public void testInstanceWithEmptyOneByte()
	{
		Chunk mid = Chunks.ofByte((byte)0);
		Chunk expected = mid;
		Chunk actual;

		actual = Chunks.of(Chunks.empty(), mid, Chunks.empty());
		assertEquals(expected, actual);
	}

	@Test
	public void testInstanceWithEmptyTwoByte()
	{
		Chunk mid = Chunks.ofBytes((byte)0,(byte)0xff);
		Chunk expected = mid;
		Chunk actual;

		actual = Chunks.of(Chunks.empty(), mid, Chunks.empty());
		assertEquals(expected, actual);
	}

	@Test
	public void testInstanceWithEmpties()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.of(Chunks.empty(), Chunks.empty(), Chunks.empty());
		assertEquals(expected, actual);
	}

	@Test
	public void testCoalesceFailedAlloc()
	{
		Chunk a = Chunks.ofByte((byte)0);
		Chunk b = Chunks.ofByte((byte)1);
		Chunk c = Chunks.ofByte((byte)2);
		Chunk input = Chunks.of(a,b,c);
		Chunk expected = null;
		Chunk actual;
		ChunkSPI spi;
		MultiChunkSPI mcspi;

		spi=input.getSPI();
		assertTrue(spi instanceof MultiChunkSPI);
		mcspi=(MultiChunkSPI)spi;
		actual = mcspi.testableCoalesce((i)->{return null;});
		assertEquals(expected, actual);
	}

}
