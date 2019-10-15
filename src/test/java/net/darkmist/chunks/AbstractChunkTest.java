package net.darkmist.chunks;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	private static ChunkSPI singleByteTestChunkSPI(int b)
	{
		return new TestChunkSPI(1)
		{
			@Override
			public int getByte(long off)
			{
				if(off==0)
					return b;
				throw new IndexOutOfBoundsException("offset " + off + " is not zero.");
			}
		};
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

	@Test
	public void testHashCodeSize0()
	{
		ChunkSPI spi = new TestChunkSPI(0);
		assertEquals(0,spi.hashCode());
	}

	@Test
	public void testHashCode10()
	{
		ChunkSPI spi = singleByteTestChunkSPI(0);
		assertEquals(0,spi.hashCode());
	}

	@Test
	public void testHashCode11()
	{
		ChunkSPI spi = singleByteTestChunkSPI(1);
		assertEquals(1,spi.hashCode());
	}

	@Test
	public void testEqualsNull()
	{
		ChunkSPI spi = new TestChunkSPI(0);
		assertFalse(spi.equals(null));
	}

	@Test
	public void testEqualsString()
	{
		ChunkSPI spi = new TestChunkSPI(0);
		assertFalse(spi.equals("toast"));
	}

	@Test
	public void testEqualsDifferentLengths()
	{
		ChunkSPI zero = new TestChunkSPI(0);
		ChunkSPI one = new TestChunkSPI(1);
		assertFalse(zero.equals(one));
	}

	@Test
	public void testEqualsDifferentContent()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		ChunkSPI b = singleByteTestChunkSPI(1);
		assertFalse(a.equals(b));
	}

	@Test
	public void testEqualsContentEqual()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		ChunkSPI b = singleByteTestChunkSPI(0);
		assertTrue(a.equals(b));
	}

	@Test
	public void testEqualsSelf()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		assertTrue(a.equals(a));
	}

	@Test
	public void testDefaultHashCodeNull()
	{
		ChunkSPI nullSPI = null;
		assertEquals(0, ChunkSPI.defaultHashCode(nullSPI));
	}

	@Test
	public void testDefaultEqualsObjectNull()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		assertFalse(ChunkSPI.defaultEquals(a,null));
	}

	@Test
	public void testDefaultEqualsSPINull()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		ChunkSPI nullSPI = null;
		assertFalse(ChunkSPI.defaultEquals(nullSPI, a));
	}

	@Test
	public void testDefaultEqualsString()
	{
		ChunkSPI chunkSPI = singleByteTestChunkSPI(0);
		assertFalse(ChunkSPI.defaultEquals(chunkSPI, "toast"));
	}

	@Test
	public void testDefaultEqualsSameSizeDifferentContent()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		ChunkSPI b = singleByteTestChunkSPI(1);
		assertFalse(ChunkSPI.defaultEquals(a,b));
	}

	@Test
	public void testDefaultEqualsSameSizeDifferentSize()
	{
		ChunkSPI a = singleByteTestChunkSPI(0);
		ChunkSPI b = EmptyChunkSPI.EMPTY_SPI;
		assertFalse(ChunkSPI.defaultEquals(a,b));
	}
}
