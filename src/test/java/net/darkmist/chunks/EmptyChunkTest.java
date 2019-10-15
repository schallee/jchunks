package net.darkmist.chunks;

import org.easymock.EasyMock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyChunkTest
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(EmptyChunkTest.class);

	@Test
	public void getByte()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.getByte(0L));
	}

	@Test
	public void getShort()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.getShort(0L,null));
	}

	@Test
	public void getInt()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.getInt(0L,null));
	}

	@Test
	public void getLong()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.getLong(0L,null));
	}

	@Test
	public void getSize()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertEquals(0L,empty.getSize());
	}

	@Test
	public void isCoalesced()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertTrue(empty.isCoalesced());
	}

	@Test
	public void coalesce()
	{
		Chunk empty;
		Chunk result;

		empty = Chunks.empty();
		result = empty.coalesce();
		assertEquals(empty,result);
	}

	@Test
	public void subChunkOff0Len0()
	{
		Chunk empty;
		Chunk result;

		empty = Chunks.empty();
		result = empty.subChunk(0L,0L);
		assertEquals(empty,result);
	}

	@Test
	public void subChunkOff1Len0()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.subChunk(1L,0L));
	}

	@Test
	public void subChunkOff0Len1()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.subChunk(0L,1L));
	}

	@Test
	public void subChunkOff1Len1()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertThrows(IndexOutOfBoundsException.class, ()->empty.subChunk(1L,1L));
	}

	@Test
	public void copyToChunkOff0ArrayOff0ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		result = empty.copyTo(input, chunkOff, arrayOff, length);
		assertArrayEquals(expected,result);
		assertEquals(input,result);
	}

	@Test
	public void copyToChunkOff0ArrayOff0ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff0ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		result = empty.copyTo(input, chunkOff, arrayOff, length);
		assertArrayEquals(expected,result);
		assertEquals(input,result);
	}

	@Test
	public void copyToChunkOff0ArrayOff0ArrayLength1Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		result = empty.copyTo(input, chunkOff, arrayOff, length);
		assertArrayEquals(expected,result);
		assertEquals(input,result);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength1Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength1Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}
	
	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];

		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength1Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1L;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOffNeg1ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = -1;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertently changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff2ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0L;
		int arrayOff = 2;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		assertThrows(IndexOutOfBoundsException.class, ()->empty.copyTo(input, chunkOff, arrayOff, length));
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void emptyToString()
	{
		Chunks.empty().toString();
	}

	@Test
	public void directToString()
	{
		assertNotNull(EmptyChunkSPI.EMPTY_SPI.toString());
	}

	@Test
	public void directHashCode()
	{
		assertEquals(0,EmptyChunkSPI.EMPTY_SPI.hashCode());
	}

	@Test
	public void spiEqualsNull()
	{
		assertFalse(EmptyChunkSPI.EMPTY_SPI.equals(null));
	}

	@Test
	public void spiEqualsString()
	{
		assertFalse(EmptyChunkSPI.EMPTY_SPI.equals("Something"));
	}

	@Test
	public void spiEquals0()
	{
		ChunkSPI a = EmptyChunkSPI.EMPTY_SPI;
		ChunkSPI b = Chunks.ofBytes(0).getSPI();

		assertEquals(0L, a.getSize());
		assertEquals(1L, b.getSize());
		assertFalse(a.equals(b));
	}

	@Test
	public void spiEqualsMock()
	{
		ChunkSPI mock = EasyMock.mock(ChunkSPI.class);

		EasyMock.expect(mock.getSize()).andReturn(0L);
		EasyMock.replay(mock);

		assertTrue(EmptyChunkSPI.EMPTY_SPI.equals(mock));
		EasyMock.verify(mock);
	}

	@Test
	public void spiEqualsSelf()
	{
		assertTrue(EmptyChunkSPI.EMPTY_SPI.equals(EmptyChunkSPI.EMPTY_SPI));
	}
}
