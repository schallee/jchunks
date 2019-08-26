package net.darkmist.chunks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunksTest
{
	private static final Logger logger = LoggerFactory.getLogger(ChunkTest.class);

	@Test
	public void testFromStrNull()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.from(null);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testFromStrEmpty()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.from("");
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testFromStrNullCharset()
	{
		Chunk expected = Chunks.ofByte((byte)'A');
		Chunk actual;

		actual = Chunks.from("A", null);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testFromLongBO()
	{
		long input = 0x0001020304050607l;
		Chunk expected = Chunks.ofBytes(0,1,2,3,4,5,6,7);
		Chunk actual;

		actual = Chunks.from(input, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testFromIntBO()
	{
		int input = 0x00010203;
		Chunk expected = Chunks.ofBytes(0,1,2,3);
		Chunk actual;

		actual = Chunks.from(input, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testFromShortBO()
	{
		short input = 0x0001;
		Chunk expected = Chunks.ofBytes(0,1);
		Chunk actual;

		actual = Chunks.from(input, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testOfIntsNull()
	{
		int[] input = null;
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.ofBytes(input);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testOfIntsEmpty()
	{
		int[] input = new int[0];
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.ofBytes(input);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testOfIntsSingle()
	{
		int[] input = new int[]{0x55};
		Chunk expected = Chunks.ofByte(0x55);
		Chunk actual;

		actual = Chunks.ofBytes(input);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testCopyBytesOffLen()
	{
		byte[] input = new byte[]{0,1,2,3};
		Chunk expected = Chunks.ofBytes(1,2);
		Chunk actual;

		actual = Chunks.copy(input, 1, 2);
		assertEquals(expected, actual);
	}

	@Test
	public void testCopyByteBuffer()
	{
		ByteBuffer input = ByteBuffer.wrap(new byte[]{0,1,2,3});
		Chunk expected = Chunks.ofBytes(0,1,2,3);
		Chunk actual;

		actual = Chunks.copy(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testGiveBytesOffLen()
	{
		byte[] input = new byte[]{0,1,2,3};
		Chunk expected = Chunks.ofBytes(1,2);
		Chunk actual;

		actual = Chunks.give(input, 1, 2);
		assertEquals(expected, actual);
	}
}
