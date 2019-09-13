package net.darkmist.chunks;

import java.nio.ByteBuffer;
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

public class SubChunkTest
{
	@SuppressWarnings("UnusedVariable")
	private static final Logger logger = LoggerFactory.getLogger(SubChunkTest.class);

	private static Stream<Chunk> testSubChunks()
	{
		return LongStream.of(4l,6l,8l,12l,24l)
			.mapToObj(TestSources::mkTestArray)
			.map(Chunks::giveBytes)
			.map((chunk)->SubChunkSPI.instance(chunk, 1, chunk.size()-1));
	}

	public static Stream<Arguments> streamByteAtOffArgs()
	{
		return testSubChunks()
			.flatMap((chunk)->TestSources.streamByteAtOffArgs(chunk, 1));
	}

	@ParameterizedTest
	@MethodSource("streamByteAtOffArgs")
	public void byteValueAt(Chunk chunk, byte expected, long off)
	{
		TestSources.byteValueAt(chunk,expected,Util.requirePosInt(off));
	}

	public static Stream<Arguments> streamByteAtOffArgsCoalesced()
	{
		return testSubChunks()
			.map(Chunk::coalesce)
			.flatMap((chunk)->TestSources.streamByteAtOffArgs(chunk, 1));
	}

	@ParameterizedTest
	@MethodSource("streamByteAtOffArgsCoalesced")
	public void byteValueAtCoalesced(Chunk chunk, byte expected, long off)
	{
		TestSources.byteValueAt(chunk,expected,Util.requirePosInt(off));
	}

	public static Stream<Arguments> streamShortAtOffArgs()
	{
		return testSubChunks()
			.flatMap((chunk)->TestSources.streamShortAtOffArgs(chunk,1));
	}

	@ParameterizedTest
	@MethodSource("streamShortAtOffArgs")
	public void shortValueAt(Chunk chunk, short expected, long off)
	{
		TestSources.shortValueAt(chunk, expected, Util.requirePosInt(off));
	}

	public static Stream<Arguments> streamIntAtOffArgs()
	{
		return testSubChunks()
			.flatMap((chunk)->TestSources.streamIntAtOffArgs(chunk,1));
	}

	@ParameterizedTest
	@MethodSource("streamIntAtOffArgs")
	public void intValueAt(Chunk chunk, int expected, long off)
	{
		TestSources.intValueAt(chunk, expected, off);
	}

	public static Stream<Arguments> streamLongAtOffArgs()
	{
		return testSubChunks()
			.flatMap((chunk)->TestSources.streamLongAtOffArgs(chunk,1l));
	}

	@ParameterizedTest
	@MethodSource("streamLongAtOffArgs")
	public void longValueAt(Chunk chunk, long expected, long off)
	{
		TestSources.longValueAt(chunk, expected, off);
	}

	public static Stream<Arguments> streamSubChunkArg()
	{
		return testSubChunks()
			.flatMap((chunk)->TestSources.streamSubChunkArgAdjusted(chunk,1));
	}

	@ParameterizedTest
	@MethodSource("streamSubChunkArg")
	public void subChunkAtFor(Chunk chunk, long off, long len, int valueAdjust)
	{
		TestSources.subChunkAtFor(chunk, off, len, valueAdjust);
	}

	public static Stream<Arguments> streamCopyToArg()
	{
		return testSubChunks()
			.flatMap((chunk)->TestSources.streamCopyToArg(chunk, 1));
	}

	@ParameterizedTest
	@MethodSource("streamCopyToArg")
	public void copyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int arrayLen, int copyLen, int valueAdjust)
	{
		TestSources.copyToAtFor(chunk, chunkOff, arrayOff, arrayLen, copyLen, valueAdjust);
	}

	public static Stream<Arguments> streamFailCopyToArg()
	{
		return testSubChunks()
			.flatMap(TestSources::streamFailCopyToArg);
	}

	@ParameterizedTest
	@MethodSource("streamFailCopyToArg")
	public void failCopyToAtFor(Chunk chunk, long chunkOff, int arrayOff, int argLen, int arrayLen)
	{
		TestSources.failCopyToAtFor(chunk, chunkOff, arrayOff, argLen, arrayLen);
	}

	@Test
	public void instanceSelf()
	{
		Chunk input = Chunks.fromISOLatin1("toast is yummy");
		Chunk expected = input;
		Chunk actual;

		actual = SubChunkSPI.instance(input, 0l, input.getSize());
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void instanceOff0Len2()
	{
		Chunk input = Chunks.fromISOLatin1("toast is yummy");
		Chunk expected = Chunks.fromISOLatin1("to");
		Chunk actual;

		actual = SubChunkSPI.instance(input, 0l, 2l);
		assertEquals(expected, actual);
	}
}
