package net.darkmist.chunks;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
		assertThrows(NegativeArraySizeException.class, ()->new TestChunkSPI(-1));
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
			@Nullable
			@Override
			public Chunk coalesce()
			{
				return coalesce((size)->null);
			}
		};
		assertNull(spi.coalesce());
	}
}
