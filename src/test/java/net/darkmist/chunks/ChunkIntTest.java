package net.darkmist.chunks;

import java.nio.ByteBuffer;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkIntTest
{
	private static final Logger logger = LoggerFactory.getLogger(ChunkIntTest.class);

	private static final ChunkSPI SPI = BufferChunkSPI.giveInstance(ByteBuffer.wrap(new byte[]{0,1,2,3,4,5,6,7,8,9})).getSPI();
	private static final ChunkIntSPI INT_SPI = (ChunkIntSPI)SPI;
	private static final Chunk CHUNK = Chunk.instance(SPI);
	private static final Chunk INT_CHUNK = Chunk.instance(INT_SPI);
	private static final int CHUNK_SIZE = CHUNK.size();
	private static final ByteOrder BIG = ByteOrder.BIG_ENDIAN;

	@Test
	public void getBytes()
	{
		for(int i=0;i<CHUNK_SIZE;i++)
			assertEquals(CHUNK.getByte(i), INT_CHUNK.getByte(i));
	}

	@Test
	public void getShorts()
	{
		int end = CHUNK_SIZE - Short.BYTES + 1;
		for(int i=0;i<end;i++)
			assertEquals(CHUNK.getShort(i, BIG), INT_CHUNK.getShort(i, BIG));
	}

	@Test
	public void getInts()
	{
		int end = CHUNK_SIZE - Integer.BYTES + 1;
		for(int i=0;i<end;i++)
			assertEquals(CHUNK.getInt(i, BIG), INT_CHUNK.getInt(i, BIG));
	}

	@Test
	public void getLongs()
	{
		int end = CHUNK_SIZE - Long.BYTES + 1;
		for(int i=0;i<end;i++)
			assertEquals(CHUNK.getLong(i, BIG), INT_CHUNK.getLong(i, BIG));
	}

	@Test
	public void getSize()
	{
		assertEquals(CHUNK.getSize(), INT_CHUNK.getSize());
	}

	@Test
	public void isCoalesced()
	{
		assertEquals(CHUNK.isCoalesced(), INT_CHUNK.isCoalesced());
	}

	@Test
	public void coalesce()
	{
		assertEquals(INT_CHUNK, INT_CHUNK.coalesce());
	}

	@Test
	public void subChunk12()
	{
		Chunk one = INT_CHUNK.subChunk(1,1);

		assertEquals(1, one.getSize());
		assertEquals(1, one.getByte(0));
	}

	@Test
	public void copy()
	{
		assertArrayEquals(CHUNK.copy(), INT_CHUNK.copy());
	}
}
