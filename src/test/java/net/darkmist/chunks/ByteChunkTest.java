package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class ByteChunkTest
{
	//private static final Logger logger = LoggerFactory.getLogger(ByteChunkTest.class);

	private static final ByteOrder bo = ByteOrder.BIG_ENDIAN;	// Doesn't matter but needs to be something
	private static List<Chunk> chunks;

	@BeforeClass
	public static void mkChunks()
	{
		List<Chunk> list = new ArrayList<>(256);

		for(int i=0;i<=0xff;i++)
			list.add(Chunks.of(((byte)(i&0xff))));
		chunks = Collections.unmodifiableList(list);
	}

	@Test
	public void getByte0()
	{
		int val;

		for(int i=0;i<=0xff;i++)
		{
			val = chunks.get(i).getByte(0);
			assertEquals(String.format("Value for 0x%02x was 0x%02x", i, val),i,val);
		}
	}

	@Test
	public void getByte1()
	{
		int val;

		for(int i=0;i<=0xff;i++)
			try
			{
				val = chunks.get(i).getByte(1);
				fail(String.format("Received byte value 0x%02x instead of exception for 0x%02x.", val, i));
			}
			catch(IndexOutOfBoundsException expected)
			{
			}
	}

	@Test
	public void getShort0()
	{
		int val;

		for(int i=0;i<=0xff;i++)
			try
			{
				val = chunks.get(i).getShort(0, bo);
				fail(String.format("Received short value 0x%04x instead of exception for 0x%02x.", val, i));
			}
			catch(IndexOutOfBoundsException expected)
			{
			}
	}

	@Test
	public void getInt0()
	{
		int val;

		for(int i=0;i<=0xff;i++)
			try
			{
				val = chunks.get(i).getInt(0, bo);
				fail(String.format("Received int value 0x%08x instead of exception for 0x%02x.", val, i));
			}
			catch(IndexOutOfBoundsException expected)
			{
			}
	}

	@Test
	public void getLong0()
	{
		long val;

		for(int i=0;i<=0xff;i++)
			try
			{
				val = chunks.get(i).getLong(0, bo);
				fail(String.format("Received long value 0x%016x instead of exception for 0x%02x.", val, i));
			}
			catch(IndexOutOfBoundsException expected)
			{
			}
	}

	@Test
	public void getSize()
	{
		long val;

		for(int i=0;i<=0xff;i++)
		{
			val = chunks.get(i).getSize();
			assertEquals(String.format("Size for 0x%02x was %d", i, val),1l,val);
		}
	}

	@Test
	public void isCoalesced()
	{
		boolean val;

		for(int i=0;i<=0xff;i++)
		{
			val = chunks.get(i).isCoalesced();
			assertTrue(String.format("IsCoalesced for 0x%02x was false", i),val);
		}
	}

	@Test
	public void coalesce()
	{
		Chunk input;
		Chunk result;

		for(int i=0;i<=0xff;i++)
		{
			input = chunks.get(i);
			result = input.coalesce();
			assertEquals(String.format("Coalesced chunk for 0x%02x was not itself.",i), input, result);
		}
	}

	@Test
	public void subChunkOff0Len0()
	{
		Chunk expected = Chunks.empty();
		Chunk input;
		Chunk result;
		long chunkOff=0l;
		long chunkLen=0l;

		for(int i=0;i<=0xff;i++)
		{
			input = chunks.get(i);
			result = input.subChunk(chunkOff,chunkLen);
			assertEquals(String.format("Subchunk of 0x%02x at %d for length %d returned %s instead of %s.", i, chunkOff, chunkLen, result, expected), expected, result);
		}
	}

	@Test
	public void subChunkOff1Len0()
	{
		Chunk expected = Chunks.empty();
		Chunk input;
		Chunk result;
		long chunkOff=1l;
		long chunkLen=0l;

		for(int i=0;i<=0xff;i++)
		{
			input = chunks.get(i);
			result = input.subChunk(chunkOff,chunkLen);
			assertEquals(String.format("Subchunk of 0x%02x at %d for length %d returned %s instead of %s.", i, chunkOff, chunkLen, result, expected), expected, result);
		}
	}

	@Test
	public void subChunkOff0Len1()
	{
		Chunk input;
		Chunk result;
		Chunk expected;
		long chunkOff=0l;
		long chunkLen=1l;

		for(int i=0;i<=0xff;i++)
		{
			expected = input = chunks.get(i);
			result = input.subChunk(chunkOff,chunkLen);
			assertEquals(String.format("Subchunk of 0x%02x at %d for length %d returned %s instead of %s.", i, chunkOff, chunkLen, result, expected), input, result);
		}
	}

	@Test
	public void subChunkOff0Len2()
	{
		Chunk input;
		Chunk result;
		long chunkOff=0l;
		long chunkLen=2l;

		for(int i=0;i<=0xff;i++)
		{
			input = chunks.get(i);
			try
			{
				result = input.subChunk(chunkOff,chunkLen);
				fail(String.format("Subchunk of 0x%02x at %d for length %d did not throw an exception but returned: %s", i, chunkOff, chunkLen, result));
			}
			catch(IndexOutOfBoundsException expected)
			{
			}
		}
	}

	@Test
	public void subChunkOff1Len1()
	{
		Chunk input;
		Chunk result;

		for(int i=0;i<=0xff;i++)
		{
			input = chunks.get(i);
			try
			{
				result = input.subChunk(1l,1l);
				fail(String.format("Subchunk of 0x%02x at 1 for length 1 did not throw an exception but returned: %s", i, result));
			}
			catch(IndexOutOfBoundsException expected)
			{
			}
		}
	}

	@Test
	public void copyToByteOff0Len0()
	{
		Chunk chunk;
		long chunkOff = 0l;
		int arrayOff = 0;
		int arrayLen= 0;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		for(int i=0;i<=0xff;i++)
		{
			chunk = chunks.get(i);
			result = chunk.copyTo(input, chunkOff, arrayOff, arrayLen);
			assertArrayEquals(String.format("Chunk 0x%02x did not return empty array for offset %d and length %d but %s", i, chunkOff, arrayLen, Arrays.toString(result)),expected,result);
			assertEquals(String.format("Chunk 0x%02x did not return the same array passed to it.", i),input,result);
		}
	}

	@Test
	public void copyToByteOff1Len0()
	{
		Chunk chunk;
		long chunkOff = 1l;
		int arrayOff = 0;
		int arrayLen= 0;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		for(int i=0;i<=0xff;i++)
		{
			chunk = chunks.get(i);
			result = chunk.copyTo(input, chunkOff, arrayOff, arrayLen);
			assertArrayEquals(String.format("Chunk 0x%02x did not return empty array for offset %d and length %d but %s", i, chunkOff, arrayLen, Arrays.toString(result)),expected,result);
			assertEquals(String.format("Chunk 0x%02x did not return the same array passed to it.", i),input,result);
		}
	}

	@Test
	public void copyToByteOff0Len1()
	{
		Chunk chunk;
		long chunkOff = 0l;
		int arrayOff = 0;
		int arrayLen= 1;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		for(int i=0;i<=0xff;i++)
		{
			chunk = chunks.get(i);
			expected[0]=(byte)(i&0xff);
			result = chunk.copyTo(input, chunkOff, arrayOff, arrayLen);
			assertArrayEquals(String.format("Chunk 0x%02x %s did not return an array containing itself for offset %d and length %d but %s", i, chunk, chunkOff, arrayLen, Arrays.toString(result)),expected,result);
			assertEquals(String.format("Chunk 0x%02x did not return the same array passed to it.", i),input,result);
		}
	}

	@Test
	public void copyToByteOff0Len1ArrayOff1()
	{
		Chunk chunk;
		long chunkOff=0l;
		int arrayOff=1;
		int arrayLen=3;
		int length =1;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		for(int i=0;i<=0xff;i++)
		{
			chunk = chunks.get(i);
			input[0]=expected[0]=(byte)((~i)&0xff);
			input[1]=(byte)((~i)&0xff);
			expected[1]=(byte)(i&0xff);
			input[2]=expected[2]=(byte)((~i)&0xff);
			result = chunk.copyTo(input, chunkOff, arrayOff, length);
			assertArrayEquals(String.format("Chunk 0x%02x %s did not return expected array %s for chunkOff %d, arrayLen %d, arrayOff %d and length %d but %s.", i, chunk, Arrays.toString(expected), chunkOff, arrayLen, arrayOff, length, Arrays.toString(result)),expected,result);
			assertEquals(String.format("Chunk 0x%02x did not return the same array passed to it.", i),input,result);
		}
	}

	@Test
	public void copyToByteOff0Len2()
	{
		Chunk chunk;
		long chunkOff = 0l;
		int arrayOff = 0;
		int arrayLen= 2;
		byte[] input = new byte[arrayLen];
		byte[] expected = new byte[arrayLen];
		byte[] result;

		for(int i=0;i<=0xff;i++)
		{
			chunk = chunks.get(i);
			try
			{
				expected[0]=expected[1]=input[0]=input[1]=(byte)((~i)&0xff);
				result = chunk.copyTo(input, chunkOff, arrayOff, arrayLen);
				fail(String.format("Chunk 0x%02x did not throw an exception for offset %d and length %d. Result was %s", i, chunkOff, arrayLen, Arrays.toString(result)));
			}
			catch(IndexOutOfBoundsException expectedException)
			{
			}
			assertArrayEquals(String.format("Chunk 0x%02x threw as expected for offset %d and length %d but modified the input array: %s", i, chunkOff, arrayOff, Arrays.toString(input)),expected, input);
		}
	}

	@Test
	public void checkToStrings()
	{
		for(Chunk chunk : chunks)
			assertNotNull(chunk.getSPI().toString());
	}
}
