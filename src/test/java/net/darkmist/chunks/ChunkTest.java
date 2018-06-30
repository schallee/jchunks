package net.darkmist.chunks;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChunkTest
{
	@Test
	public void requireValidOffLen0_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(0,0,0));
	}

	@Test
	public void emptySize()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		assertTrue(empty.isSizeLong());
		assertEquals(0l,empty.getSizeLong());
		assertTrue(empty.isSizeInt());
		assertEquals(0,empty.getSizeInt());
		assertEquals(0,empty.size());
	}

	@Test
	public void emptyGetLong0()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
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
	public void emptyGetLongNeg1()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(-1l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void emptyGetInt0()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(0);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void emptyGetIntNeg1()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(-1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void byte0Size()
	{
		Chunk zero = Chunk.byteInstance((byte)0);

		assertTrue(zero.isSizeLong());
		assertEquals(1l, zero.getSizeLong());
		assertTrue(zero.isSizeInt());
		assertEquals(1, zero.getSizeInt());
		assertEquals(1, zero.size());
	}

	@Test
	public void byte0Get0()
	{
		Chunk zero = Chunk.byteInstance(0);

		assertEquals(0, zero.getByte(0));
		assertEquals(0, zero.getByte(0l));
	}

	@Test
	public void byte0Get1()
	{
		Chunk zero = Chunk.byteInstance(0);

		try
		{
			zero.getByte(1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void byte0GetNeg1()
	{
		Chunk zero = Chunk.byteInstance(0);

		try
		{
			zero.getByte(-1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void byteIntNeg1Get0()
	{
		Chunk chunk = Chunk.byteInstance(-1);

		assertTrue(chunk.isSizeLong());
		assertEquals(1l, chunk.getSizeLong());
		assertTrue(chunk.isSizeInt());
		assertEquals(1, chunk.getSizeInt());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@Test
	public void byteInt255Get0()
	{
		Chunk chunk = Chunk.byteInstance(255);

		assertTrue(chunk.isSizeLong());
		assertEquals(1l, chunk.getSizeLong());
		assertTrue(chunk.isSizeInt());
		assertEquals(1, chunk.getSizeInt());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}
}
