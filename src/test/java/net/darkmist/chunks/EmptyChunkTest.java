package net.darkmist.chunks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyChunkTest
{
	private static final Logger logger = LoggerFactory.getLogger(EmptyChunkTest.class);

	@Test
	public void getByte()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.getByte(0l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void getShort()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.getShort(0l, null);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void getInt()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.getInt(0l, null);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void getLong()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.getLong(0l, null);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void getSize()
	{
		Chunk empty;

		empty = Chunks.empty();
		assertEquals(0l,empty.getSize());
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
		result = empty.subChunk(0l,0l);
		assertEquals(empty,result);
	}

	@Test
	public void subChunkOff1Len0()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.subChunk(1l,0l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void subChunkOff0Len1()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.subChunk(0l,1l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void subChunkOff1Len1()
	{
		Chunk empty;

		empty = Chunks.empty();
		try
		{
			empty.subChunk(1l,1l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void copyToChunkOff0ArrayOff0ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
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
		long chunkOff = 0l;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff0ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
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
		long chunkOff = 0l;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff1ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
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
		long chunkOff = 0l;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 0;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff0ArrayLength1Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 0;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength0Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 0;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}
	
	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength0Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 1;
		int arrayLength = 0;
		int length = 1;
		byte[] input = new byte[arrayLength];
		byte[] expected = new byte[arrayLength];
		byte[] result;

		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff1ArrayOff1ArrayLength1Length1()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 1l;
		int arrayOff = 1;
		int arrayLength = 1;
		int length = 1;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOffNeg1ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
		int arrayOff = -1;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void copyToChunkOff0ArrayOff2ArrayLength1Length0()
	{
		Chunk empty = Chunks.empty();
		long chunkOff = 0l;
		int arrayOff = 2;
		int arrayLength = 1;
		int length = 0;
		byte[] input = new byte[]{0x55};
		byte[] expected = new byte[]{0x55};
		byte[] result;

		assertEquals(arrayLength, input.length);
		assertEquals(arrayLength, expected.length);
		try
		{
			result = empty.copyTo(input, chunkOff, arrayOff, length);
			fail();
		}
		catch(IndexOutOfBoundsException expectedException)
		{
		}
		// Make sure input wasn't inadvertantly changed...
		assertArrayEquals(expected,input);
	}

	@Test
	public void emptyToString()
	{
		Chunks.empty().toString();
	}
}
