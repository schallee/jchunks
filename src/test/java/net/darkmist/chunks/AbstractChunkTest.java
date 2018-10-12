package net.darkmist.chunks;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for parts of {@link AbstractChunkSPI} that are not exercised through other subclass tests.
 */
public class AbstractChunkTest
{
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(AbstractChunkTest.class);

	private static class TestChunkSPI extends AbstractChunkSPI
	{
		TestChunkSPI(long size)
		{
			super(size);
		}

		@Override
		public int getByte(long off)
		{
			throw new UnsupportedOperationException("Minimum test only sub class.");
		}

		@Override
		public Chunk subChunk(long off, long len)
		{
			throw new UnsupportedOperationException("Minimum test only sub class.");
		}
	}

	@Test
	public void testNegativeSize()
	{
		try
		{
			ChunkSPI spi = new TestChunkSPI(-1);
			fail("AbstractChunkSPI#AbstractChunkSPI(-1) should have thrown an exception.");
		}
		catch(NegativeArraySizeException expected)
		{
			logger.debug("Cauth NegativeArraySizeExeption as expected.", expected);
		}
	}

	@Test
	public void testDefaultIsCoalesed()
	{
		ChunkSPI spi = new TestChunkSPI(0);

		assertFalse(spi.isCoalesced());
	}
	
	@Test
	public void testCoalesceHuge()
	{
		ChunkSPI spi = new TestChunkSPI(Long.MAX_VALUE);

		assertNull(spi.coalesce());
	}

	@Test
	public void testCoalesceSubClaimsCoalesced()
	{
		ChunkSPI spi = new TestChunkSPI(0)
		{
			@Override
			public boolean isCoalesced()
			{
				return true;
			}
		};

		assertNull(spi.coalesce());
	}

	@Test
	public void testCoalesceAllocFailure()
	{
		ChunkSPI spi = new TestChunkSPI(42)
		{
			@Override
			public Chunk coalesce()
			{
				return coalesce((size)->null);
			}
		};
		assertNull(spi.coalesce());
	}
}
