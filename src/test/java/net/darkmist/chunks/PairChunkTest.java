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

public class PairChunkTest
{
	private static final Logger logger = LoggerFactory.getLogger(PairChunkTest.class);

	private static Chunk twoBytePairChunk()
	{
		Chunk a = Chunks.ofByte(0);
		Chunk b = Chunks.ofByte(1);
		Chunk pair = Chunks.of(a,b);

		assertEquals(Byte.BYTES * 2,pair.size());
		return pair;
	}

	private static Chunk twoShortPairChunk()
	{
		Chunk a = Chunks.from((short)0x0001);
		Chunk b = Chunks.from((short)0x0203);
		Chunk pair = Chunks.of(a,b);

		assertEquals(Short.BYTES*2,pair.size());
		return pair;
	}

	private static Chunk twoIntPairChunk()
	{
		Chunk a = Chunks.from(0x00010203);
		Chunk b = Chunks.from(0x04050607);
		Chunk pair = Chunks.of(a,b);

		assertEquals(Integer.BYTES*2,pair.size());
		return pair;
	}

	private static Chunk twoLongPairChunk()
	{
		Chunk a = Chunks.from(0x0001020304050607l);
		Chunk b = Chunks.from(0x08090a0b0c0d0e0fl);
		Chunk pair = Chunks.of(a,b);

		assertEquals(Long.BYTES*2,pair.size());
		return pair;
	}

	private static Chunk oneFivePairChunk()
	{
		Chunk a = Chunks.ofByte(0x00);
		Chunk b = Chunks.ofBytes(0x01, 0x02, 0x03, 0x04, 0x05);
		Chunk pair = Chunks.of(a,b);

		assertEquals(1, a.size());
		assertEquals(5, b.size());
		assertEquals(6, pair.size());
		return pair;
	}

	private static Stream<Chunk> testPairChunks()
	{
		return Stream.of(
			twoBytePairChunk(),
			twoShortPairChunk(),
			twoIntPairChunk(),
			twoLongPairChunk(),
			oneFivePairChunk()
		);
	}

	@ParameterizedTest
	@MethodSource("testPairChunks")
	public void isCoalesced(Chunk chunk)
	{
		assertEquals(chunk.getSize()>Integer.MAX_VALUE,chunk.isCoalesced());
	}

	public static Stream<Arguments> streamByteAtOffArgs()
	{
		return testPairChunks()
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
		return testPairChunks()
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
		return testPairChunks()
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
		return testPairChunks()
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
		return testPairChunks()
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
		return testPairChunks()
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
		return testPairChunks()
			.flatMap(TestSources::streamCopyToArg);
	}

	@ParameterizedTest
	@MethodSource("streamCopyToArg")
	public void copyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int arrayLen, int copyLen)
	{
		if(logger.isDebugEnabled())
			logger.debug("copyToAtFor(chunk={}, chunkOff={} arrayOff={} arrayLen={} copyLen={})", chunk, chunkOff, arrayOff, arrayLen, copyLen);
		TestSources.copyToAtFor(chunk, chunkOff, arrayOff, arrayLen, copyLen);
	}

	public static Stream<Arguments> streamFailCopyToArg()
	{
		return testPairChunks()
			.flatMap(TestSources::streamFailCopyToArg);
	}

	@ParameterizedTest
	@MethodSource("streamFailCopyToArg")
	public void failCopyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int argLen, int arrayLen)
	{
		TestSources.failCopyToAtFor(chunk, chunkOff, arrayOff, argLen, arrayLen);
	}

	public static Stream<Arguments> streamInstanceChecks()
	{
		return Stream.of(
			Arguments.of(null,null,Chunks.empty()),
			Arguments.of(null,Chunks.empty(),Chunks.empty()),
			Arguments.of(Chunks.empty(),null,Chunks.empty()),
			Arguments.of(Chunks.empty(),Chunks.empty(),Chunks.empty()),
			Arguments.of(Chunks.ofByte(0),null,Chunks.ofByte(0)),
			Arguments.of(Chunks.ofByte(0),Chunks.empty(),Chunks.ofByte(0)),
			Arguments.of(null,Chunks.ofByte(0),Chunks.ofByte(0)),
			Arguments.of(Chunks.empty(),Chunks.ofByte(0),Chunks.ofByte(0))
		);
	}

	@ParameterizedTest
	@MethodSource("streamInstanceChecks")
	public void instanceCheck(Chunk first, Chunk second, Chunk expected)
	{
		Chunk actual;

		actual = PairChunkSPI.instance(first,second);
		assertEquals(expected,actual,()->String.format("Expected instance(first=%s,second=%s) to be equal to %s but it was %s instead.", first, second, expected, actual));
		assertSame(expected,actual,()->String.format("Expected instance(first=%s,second=%s) to be the same object as 0x%08x but it was 0x%08x instead.", first, second, System.identityHashCode(expected), System.identityHashCode(actual)));
	}

	@Test
	public void longInSecondChunk()
	{
		long expected = 0x0102030405060708l;
		Chunk a = Chunks.ofByte(0);
		Chunk b = Chunks.from(expected);
		Chunk chunk = Chunks.of(a,b);
		long actual;

		actual = chunk.getLong(1, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}
}
