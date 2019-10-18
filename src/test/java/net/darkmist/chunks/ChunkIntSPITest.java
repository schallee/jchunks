package net.darkmist.chunks;

import org.easymock.EasyMock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("UnnecessaryParentheses")
public class ChunkIntSPITest
{
	private static final long LONG_INT_MAX_VALUE = Integer.MAX_VALUE;
	private ChunkSPI mockAbstractSPI;

	@BeforeEach
	public void initMock()
	{
		mockAbstractSPI = EasyMock.mock(ChunkIntSPI.Abstract.class);
		EasyMock.expect(mockAbstractSPI.getSize()).andReturn(LONG_INT_MAX_VALUE).anyTimes();
	}

	@AfterEach
	public void verifyMock()
	{
		EasyMock.verify(mockAbstractSPI);
	}

	@Test
	public void testGetByteLongNeg1()
	{
		Chunk mockChunk;

		EasyMock.replay(mockAbstractSPI);
		mockChunk = Chunk.instance(mockAbstractSPI);

		assertThrows(IndexOutOfBoundsException.class, ()->{mockChunk.getByte(-1L);});
	}

	@Test
	public void testGetByteLong0()
	{
		Chunk mockChunk;
		int expected = 0;

		EasyMock.expect(mockAbstractSPI.getByte(0)).andReturn(expected);

		EasyMock.replay(mockAbstractSPI);
		mockChunk = Chunk.instance(mockAbstractSPI);

		assertEquals(expected, mockChunk.getByte(0L));
	}

	@Test
	public void testGetByteLongMaxInt()
	{
		Chunk mockChunk;
		int expected = 0;

		EasyMock.expect(mockAbstractSPI.getByte(Integer.MAX_VALUE)).andReturn(expected);

		EasyMock.replay(mockAbstractSPI);
		mockChunk = Chunk.instance(mockAbstractSPI);

		assertEquals(expected, mockChunk.getByte((long)(Integer.MAX_VALUE)));
	}

	@Test
	public void testGetByteLongMaxIntPlus1()
	{
		Chunk mockChunk;

		EasyMock.replay(mockAbstractSPI);
		mockChunk =  Chunk.instance(mockAbstractSPI);

		assertThrows(IndexOutOfBoundsException.class, ()->{mockChunk.getByte(1L + Integer.MAX_VALUE);});
	}
}
