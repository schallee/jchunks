package net.darkmist.chunks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunksTest
{
	@SuppressWarnings({ "UnusedVariable", "unused" })
	private static final Logger logger = LoggerFactory.getLogger(ChunkTest.class);

	@Test
	public void testFromUTF8Null()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.fromUTF8(null);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testFromISOLatin1Null()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.fromISOLatin1(null);
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Deprecated
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
	public void testFromUTF8Empty()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.fromUTF8("");
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Test
	public void testFromISOlatin1Empty()
	{
		Chunk expected = Chunks.empty();
		Chunk actual;

		actual = Chunks.fromISOLatin1("");
		assertEquals(expected, actual);
		assertSame(expected, actual);
	}

	@Deprecated
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
	public void testFromStringNullCharset()
	{
		assertThrows(NullPointerException.class, ()->Chunks.fromString("A", null));
	}

	@Deprecated
	@Test
	public void testFromStrNullCharset()
	{
		assertThrows(NullPointerException.class, ()->Chunks.from("A", null));
	}

	@Test
	public void testFromLongBO()
	{
		long input = 0x0001020304050607L;
		Chunk expected = Chunks.ofBytes(0,1,2,3,4,5,6,7);
		Chunk actual;

		actual = Chunks.fromLong(input, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromLongBODeprecated()
	{
		long input = 0x0001020304050607L;
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

		actual = Chunks.fromInt(input, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromIntBODeprecated()
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

		actual = Chunks.fromShort(input, ByteOrder.BIG_ENDIAN);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromShortBODeprecated()
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

		actual = Chunks.copyBytes(input, 1, 2);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testCopyBytesOffLenDeprecated()
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

		actual = Chunks.copyBuffer(input);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testCopyByteBufferDeprecated()
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

		actual = Chunks.giveBytes(input, 1, 2);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testGiveBytesDeprecatedOffLen()
	{
		byte[] input = new byte[]{0,1,2,3};
		Chunk expected = Chunks.ofBytes(1,2);
		Chunk actual;

		actual = Chunks.give(input, 1, 2);
		assertEquals(expected, actual);
	}

	/*-------------------------+
	 | Deprecated Method Tests |
	 +-------------------------*/

	// These are all wrappers to the new name so we just need to call them once.
	
	@Deprecated
	@Test
	public void testByteOf()
	{
		Chunk expected = Chunks.ofByte(0x0);
		Chunk actual = Chunks.of((byte)0x0);

		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testIntOf()
	{
		Chunk expected = Chunks.ofByte(0x0);
		Chunk actual = Chunks.of(0x0);

		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testBytesOf()
	{
		Chunk expected = Chunks.ofBytes(0x0,0x1);
		Chunk actual = Chunks.of(new byte[]{(byte)0x0, (byte)0x1});

		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testIntsOf()
	{
		Chunk expected = Chunks.ofBytes(0x0, 0x1);
		Chunk actual = Chunks.of(new int[]{0x0,0x1});

		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromStrCharsetDeprecated()
	{
		Chunk expected = Chunks.empty();
		Chunk actual = Chunks.from("", StandardCharsets.UTF_8);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testOfChunkChunkDeprecated()
	{
		Chunk a = Chunks.ofByte('a');
		Chunk b = Chunks.ofByte('b');
		Chunk actual = Chunks.of(a,b);
		Chunk expected = Chunks.ofBytes('a','b');
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromLongDeprecated()
	{
		Chunk actual = Chunks.from(0x0123456789abcdefL);
		Chunk expected = Chunks.ofBytes(0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromIntDeprecated()
	{
		Chunk actual = Chunks.from(0x01234567);
		Chunk expected = Chunks.ofBytes(0x01, 0x23, 0x45, 0x67);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testFromShortDeprecated()
	{
		Chunk actual = Chunks.from((short)0x0123);
		Chunk expected = Chunks.ofBytes(0x01, 0x23);
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testOfChunksArrayDeprecated()
	{
		Chunk a = Chunks.ofByte('a');
		Chunk b = Chunks.ofByte('b');
		Chunk c = Chunks.ofByte('c');
		Chunk actual = Chunks.of(a,b,c);
		Chunk expected = Chunks.ofBytes('a','b','c');
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testOfChunksListDeprecated()
	{
		Chunk a = Chunks.ofByte('a');
		Chunk b = Chunks.ofByte('b');
		Chunk c = Chunks.ofByte('c');
		Chunk actual = Chunks.of(Arrays.asList(a,b,c));
		Chunk expected = Chunks.ofBytes('a','b','c');
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testCopyBytesDeprecated()
	{
		Chunk expected = Chunks.ofBytes('a','b','c');
		Chunk actual = Chunks.copy(new byte[]{'a','b','c'});
		assertEquals(expected, actual);
	}
	
	@Deprecated
	@Test
	public void testGiveBytesDeprecated()
	{
		Chunk expected = Chunks.ofBytes('a','b','c');
		Chunk actual = Chunks.give(new byte[]{'a','b','c'});
		assertEquals(expected, actual);
	}

	@Deprecated
	@Test
	public void testGiveBufferDeprecated()
	{
		Chunk expected = Chunks.ofBytes('a','b','c');
		Chunk actual = Chunks.give(ByteBuffer.wrap(new byte[]{'a','b','c'}));
		assertEquals(expected, actual);
	}
}
