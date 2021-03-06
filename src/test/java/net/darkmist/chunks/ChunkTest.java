package net.darkmist.chunks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.stream.Stream;

import static net.darkmist.chunks.TestUtil.mkByteArray;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkTest
{
	private static final Logger logger = LoggerFactory.getLogger(ChunkTest.class);

	@Test
	public void requireValidOffLen0_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(0,0,0));
	}

	@Test
	public void byte0Size()
	{
		Chunk zero = Chunks.ofByte((byte)0);

		logger.debug("zero.spi={}", zero.getSPI());
		logger.debug("zero={} zero.spi={}", zero, zero.getSPI());
		assertEquals(1L, zero.getSize());
		assertEquals(1, zero.size());
	}

	@Test
	public void byte0Get0()
	{
		Chunk zero = Chunks.ofByte(0);

		assertEquals(0, zero.getByte(0));
		assertEquals(0, zero.getByte(0L));
	}

	@Test
	public void byte0Get1()
	{
		Chunk zero = Chunks.ofByte(0);

		assertThrows(IndexOutOfBoundsException.class, ()->zero.getByte(1));
	}

	@Test
	public void byte0GetNeg1()
	{
		Chunk zero = Chunks.ofByte(0);

		assertThrows(IndexOutOfBoundsException.class, ()->zero.getByte(-1));
	}

	@SuppressWarnings("UnnecessaryParentheses")
	@Test
	public void byteIntNeg1Get0()
	{
		Chunk chunk = Chunks.ofByte(-1);

		assertEquals(1L, chunk.getSize());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@SuppressWarnings("UnnecessaryParentheses")
	@Test
	public void byteIntNeg1GetUnsigned0()
	{
		Chunk chunk = Chunks.ofByte(-1);

		assertEquals(1L, chunk.getSize());
		assertEquals(1, chunk.size());
		assertEquals(255, chunk.getByteUnsigned(0));
	}

	@SuppressWarnings("UnnecessaryParentheses")
	@Test
	public void byteInt255Get0()
	{
		Chunk chunk = Chunks.ofByte(255);

		logger.debug("chunk.spi={}", chunk, chunk.getSPI());
		logger.debug("chunk={} chunk.spi={}", chunk, chunk.getSPI());
		assertEquals(1L, chunk.getSize());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@Test
	public void testStringSerialization() throws ClassNotFoundException, IOException
	{
		Chunk input = Chunks.fromISOLatin1("toast is yummy");
		Chunk expected = input;
		Chunk actual;

		actual = TestUtil.serializeDeserialize(Chunk.class, input);
		assertEquals(expected, actual);

	}

	@Test
	public void testStringCopy() throws ClassNotFoundException, IOException
	{
		String str = "toast is yummy";
		Chunk chunk;
		byte[] expected = str.getBytes(StandardCharsets.US_ASCII);
		byte[] actual;

		chunk = Chunks.fromISOLatin1(str);
		actual = chunk.copy();
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSubChunk()
	{
		Chunk input = Chunks.ofBytes(0,1,2,3);
		Chunk expected = Chunks.ofBytes(1,2,3);
		Chunk actual;

		actual = input.subChunk(1);
		assertEquals(expected, actual);
	}

	@Test
	public void testPrepend()
	{
		Chunk input = Chunks.ofBytes(1,2,3);
		Chunk prefix = Chunks.ofByte(0);
		Chunk expected = Chunks.ofBytes(0,1,2,3);
		Chunk actual;

		actual = input.prepend(prefix);
		assertEquals(expected, actual);
	}

	@Test
	public void testAppend()
	{
		Chunk input = Chunks.ofBytes(0,1,2);
		Chunk suffix = Chunks.ofByte(3);
		Chunk expected = Chunks.ofBytes(0,1,2,3);
		Chunk actual;

		actual = input.append(suffix);
		assertEquals(expected, actual);
	}

	@Test
	public void testCopyOff1Len2()
	{
		Chunk input = Chunks.ofBytes(0,1,2,3);
		byte[] expected = new byte[]{1,2};
		byte[] actual;

		actual = input.copy(1,2);
		assertArrayEquals(expected, actual);
	}

	private static byte[] mkTmpBufSizeBasedBytes(int multiplier, int addition)
	{
		byte[] bytes = new byte[Tunables.getTmpBufSize()*multiplier + addition];

		for(int i=0;i<bytes.length;i++)
			bytes[i] = (byte)i;
		return bytes;
	}

	public static Stream<Arguments> streamWriteToTests()
	{
		return Stream.of(
			Arguments.of((Object)mkByteArray(0,1,2,3)),
			Arguments.of((Object)mkTmpBufSizeBasedBytes(1, -1)),
			Arguments.of((Object)mkTmpBufSizeBasedBytes(1, 0)),
			Arguments.of((Object)mkTmpBufSizeBasedBytes(1, 1)),
			Arguments.of((Object)mkTmpBufSizeBasedBytes(2, -1)),
			Arguments.of((Object)mkTmpBufSizeBasedBytes(2, 0)),
			Arguments.of((Object)mkTmpBufSizeBasedBytes(2, 1))
		);
	}

	@ParameterizedTest
	@MethodSource("streamWriteToTests")
	public void testWriteTo(byte[] bytes) throws IOException
	{
		Chunk input = Chunks.copyBytes(bytes);
		byte[] actual;

		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
			DataOutputStream dos = new DataOutputStream(baos);
		)
		{
			input.writeTo(dos);
			dos.flush();
			actual = baos.toByteArray();
			assertArrayEquals(bytes, actual);
		}
	}

	@ParameterizedTest
	@MethodSource("streamWriteToTests")
	public void testWriteToTrusted(byte[] bytes) throws IOException
	{
		Chunk input = Chunks.copyBytes(bytes);
		byte[] actual;

		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
			DataOutputStream dos = new DataOutputStream(baos);
		)
		{
			input.writeTo(dos, EnumSet.of(WriteFlag.TRUSTED));
			dos.flush();
			actual = baos.toByteArray();
			assertArrayEquals(bytes, actual);
		}
	}

	/*
	@Test
	public void testWriteTo() throws IOException
	{
		Chunk input = Chunks.ofBytes(0,1,2,3);
		byte[] expected = new byte[]{0,1,2,3};
		byte[] actual;

		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(4);
			DataOutputStream dos = new DataOutputStream(baos);
		)
		{
			input.writeTo(dos, EnumSet.of(WriteFlag.TRUSTED));
			dos.flush();
			actual = baos.toByteArray();
			assertArrayEquals(expected, actual);
		}
	}
	*/

	@Test
	public void testCompareToEquals()
	{
		Chunk a = Chunks.ofBytes(0,1,2,3);
		Chunk b = Chunks.ofBytes(0,1,2,3);
		int expected=0;
		int actual;
		
		actual = a.compareTo(b);
		assertEquals(expected, actual);
	}

	@Test
	public void testCompareToEqualSizeDiffValuesLess()
	{
		Chunk a = Chunks.ofBytes(0,1,2,3);
		Chunk b = Chunks.ofBytes(1,2,3,4);
		int result;
		
		result = a.compareTo(b);
		assertTrue(result < 0);
	}

	@Test
	public void testCompareToEqualSizeDiffValuesGreater()
	{
		Chunk a = Chunks.ofBytes(1,2,3,4);
		Chunk b = Chunks.ofBytes(0,1,2,3);
		int result;
		
		result = a.compareTo(b);
		assertTrue(result > 0);
	}

	@Test
	public void testCompareToEqualSizeLess()
	{
		Chunk a = Chunks.ofBytes(0,1,2);
		Chunk b = Chunks.ofBytes(0,1,2,3);
		int result;
		
		result = a.compareTo(b);
		assertTrue(result < 0);
	}

	@Test
	public void testCompareToEqualSizeMore()
	{
		Chunk a = Chunks.ofBytes(0,1,2,3);
		Chunk b = Chunks.ofBytes(0,1,2);
		int result;
		
		result = a.compareTo(b);
		assertTrue(result > 0);
	}

	@Test
	public void testCompareToSame()
	{
		Chunk a = Chunks.ofBytes(0,1,2,3);
		Chunk b = a;
		int expected = 0;
		int actual;
		
		actual = a.compareTo(b);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetShortUnsignedFFFF()
	{
		Chunk input = Chunks.ofBytes(0xff, 0xff);
		int expected = 0xffff;
		int actual;

		actual = input.getShortUnsigned(0L, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetShortUnsigned1111()
	{
		Chunk input = Chunks.ofBytes(0x11, 0x11);
		int expected = 0x1111;
		int actual;

		actual = input.getShortUnsigned(0L, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetLongUnsignedFFFFFFFF()
	{
		Chunk input = Chunks.ofBytes(0xff, 0xff, 0xff, 0xff);
		long expected = 0xffffffffL;
		long actual;

		actual = input.getIntUnsigned(0L, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetLongUnsigned11111111()
	{
		Chunk input = Chunks.ofBytes(0x11, 0x11, 0x11, 0x11);
		long expected = 0x11111111L;
		long actual;

		actual = input.getIntUnsigned(0L, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testGetIntSizeNoThrow()
	{
		assertEquals(1, Chunks.ofByte(0x0).getIntSize());
	}

	@Test
	public void testGetIntSizeThrow()
	{
		Chunk chunk = RepeatedByteChunkSPI.instance(0x0, Integer.MAX_VALUE + 1L);

		assertThrows(IndexOutOfBoundsException.class, ()->chunk.getIntSize());
	}
}
