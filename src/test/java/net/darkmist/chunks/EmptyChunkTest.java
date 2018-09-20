package net.darkmist.chunks;

import org.junit.Test;
import static org.junit.Assert.*;

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
	public void subChunkSelf()
	{
		Chunk empty;
		Chunk result;

		empty = Chunks.empty();
		result = empty.subChunk(0l,0l);
		assertEquals(empty,result);
	}

	@Test
	public void subChunkLarger()
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
	public void copyToEmptyOff0()
	{
		Chunk empty = Chunks.empty();
		byte[] bytes = new byte[0];
		byte[] bytesResult;

		bytesResult = empty.copyTo(bytes, 0l, 0, 0);
		assertArrayEquals(bytes,bytesResult);
	}

	@Test
	public void copyToEmptyOff1()
	{
		Chunk empty = Chunks.empty();
		byte[] bytes = new byte[0];

		try
		{
			empty.copyTo(bytes, 1l, 0, 0);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void copyToOff0Len0()
	{
		Chunk empty = Chunks.empty();
		byte[] input = new byte[]{1,2,3};
		byte[] expected = new byte[]{1,2,3};
		byte[] bytesResult;

		bytesResult = empty.copyTo(input, 0l, 1, 0);
		assertArrayEquals(expected,bytesResult);
	}

	@Test
	public void copyToOff0Len1()
	{
		Chunk empty = Chunks.empty();
		byte[] input = new byte[]{1,2,3};

		try
		{
			empty.copyTo(input, 0l, 1, 1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}
}
