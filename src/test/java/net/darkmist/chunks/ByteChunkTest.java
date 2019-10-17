package net.darkmist.chunks;

import org.easymock.EasyMock;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("UnnecessaryParentheses")
public class ByteChunkTest
{
	private static final boolean TEST_ALL = false;
	private static final ByteOrder bo = ByteOrder.BIG_ENDIAN;	// Doesn't matter but needs to be something

	private static Stream<Integer> streamAllBytes()
	{
		return IntStream.range(0,256).boxed();
	}

	private static Stream<Integer> streamTestBytes()
	{
		if(TEST_ALL)
			return streamAllBytes();
		return Stream.of(Integer.valueOf(42));
	}

	public static Stream<Chunk> streamTestChunks()
	{
		return streamTestBytes()
			.map((i)->Chunks.ofByte(i));
	}

	private static Stream<IdFlagsChunkOffLen<Integer,Boolean>> streamChunkOffLenCases()
	{
		return streamTestBytes()
			.flatMap((i)->
			{
				Chunk chunk = Chunks.ofByte(i);

				List<IdFlagsChunkOffLen<Integer,Boolean>> list = new ArrayList<>(8);

				// expect success tests
				list.add(IdFlagsChunkOffLen.instance(i,true,chunk,0,0));
				list.add(IdFlagsChunkOffLen.instance(i,true,chunk,0,1));
				list.add(IdFlagsChunkOffLen.instance(i,true,chunk,1,0));

				// expect failure tests
				list.add(IdFlagsChunkOffLen.instance(i,false,chunk,1,1));

				list.add(IdFlagsChunkOffLen.instance(i,false,chunk,-1,0));
				list.add(IdFlagsChunkOffLen.instance(i,false,chunk,-1,1));
				list.add(IdFlagsChunkOffLen.instance(i,false,chunk,2,0));
				list.add(IdFlagsChunkOffLen.instance(i,false,chunk,2,1));
				return list.stream();
			});
	}

	public static Stream<Arguments> getChunkOffLenCaseArguments()
	{
		return streamChunkOffLenCases()
			.map((testCase)->{
				return Arguments.of(
					testCase.getId(),
					testCase.getChunk(),
					testCase.getChunkOffset(),
					testCase.getChunkLength(),
					testCase.getFlags()
				);
			});
	}

	public static Stream<Arguments> getIntChunkTests()
	{
		return streamTestBytes()
			.map((i)->Arguments.of(i,Chunks.ofByte(i)));
	}

	private static List<Byte> invertedBytes(int len)
	{
		Byte[] byteArray = new Byte[len];

		for(int i=0;i<len;i++)
			byteArray[i]=(byte)((~i)&0xff);
		return Collections.unmodifiableList(Arrays.asList(byteArray));
	}

	private static IdFlagsChunkOffLenArrayOffLen.Builder<Integer,Boolean> chunkArrayBuilder()
	{
		return IdFlagsChunkOffLenArrayOffLen.builder();
	}

	private static Stream<IdFlagsChunkOffLenArrayOffLen<Integer,Boolean>> arrayOffsets(IdFlagsChunkOffLen<Integer,Boolean> subChunkTestCase)
	{
		long chunkLen = subChunkTestCase.getChunkLength();

		// note: arrayLen is the actual size of the array provided
		// invalid boundry cases for array off and len:
		//	off neg
		//	len neg	<== this should be handled with the chunk len
		//	off larger then dest
		//	off + len larger than dest
		//	len larger than dest
		//	off + len exceed max int
		// valid anything in array bounds
		//	len == 0 or len==1
		//	


		// 3 * 6 = 24 ~ 25 => 2 * 3 = 6 ~ 7
		return Stream.concat(
			IntStream.range(0,2).boxed()
				.flatMap((arrayLen)->
				{
					List<Byte> byteList = invertedBytes(arrayLen);
					return IntStream.range(-1,2).boxed().map((arrayOff)->
					{
						return chunkArrayBuilder()
							.from(subChunkTestCase)
							.arrayOffset(arrayOff)
							.arrayLength(arrayLen)
							.addAllByteList(byteList)
							.flags(
								subChunkTestCase.getFlags()
								&& arrayOff>=0
								&& ( arrayOff+chunkLen <= arrayLen || (chunkLen==0 && arrayOff.equals(arrayLen))))
							.build()
						;
					});
				}),
			chunkLen>0
				? Stream.of(chunkArrayBuilder()
					.from(subChunkTestCase)
					.arrayOffset(Integer.MAX_VALUE)
					.arrayLength(0)
					.array()
					.flags(false)
					.build())
				: Stream.empty()
		);

		/*
		List<IdFlagsChunkOffLenArrayOffLen<Integer,Boolean>> testCases = new ArrayList<>();

		for(int arrayLen=0;arrayLen<2;arrayLen++)
		{
			List<Byte> byteList = invertedBytes(arrayLen);

			// 3
			for(int arrayOff=-1;arrayOff<2;arrayOff++)
			{
				testCases.add(chunkArrayBuilder()
					.from(subChunkTestCase)
					.arrayOffset(arrayOff)
					.arrayLength(arrayLen)
					.addAllByteList(byteList)
					.flags(
						subChunkTestCase.getFlags()
						&& arrayOff>=0
						&& ( arrayOff+chunkLen <= arrayLen || (chunkLen==0 && arrayOff==arrayLen)))
					.build());
			}
		}
		if(chunkLen>0)
			testCases.add(chunkArrayBuilder()
				.from(subChunkTestCase)
				.arrayOffset(Integer.MAX_VALUE)
				.arrayLength(0)
				.array()
				.flags(false)
				.build());
		return testCases.stream();
		*/
	}

	private static byte[] copy(byte[] bytes)
	{
		byte[] ret = new byte[bytes.length];

		for(int i=0;i<bytes.length;i++)
			ret[i] = bytes[i];
		return ret;
	}

	public static Stream<Arguments> getChunkOffLenArrayOffLenCases()
	{
		return streamChunkOffLenCases()
			.flatMap((testCase)->arrayOffsets(testCase))
			.map((testCase)->{
				return Arguments.of(
					testCase.getId(),
					testCase.getChunk(),
					testCase.getChunkOffset(),
					testCase.getChunkLength(),
					testCase.getArray(),
					testCase.getArrayOffset(),
					testCase.getFlags()
				);
			});
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getByte0(int i, Chunk chunk)
	{
		int val = chunk.getByte(0);
		assertEquals(i,val,()->String.format("Value for 0x%02x was 0x%02x", i, val));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getByte1(int i, Chunk chunk)
	{
		assertThrows(IndexOutOfBoundsException.class, ()->chunk.getByte(1));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getByte1LongOff(int i, Chunk chunk)
	{
		assertThrows(IndexOutOfBoundsException.class, ()->chunk.getByte(1L));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getShort0(int i, Chunk chunk)
	{
		assertThrows(IndexOutOfBoundsException.class, ()->chunk.getShort(0, bo));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getInt0(int i, Chunk chunk)
	{
		assertThrows(IndexOutOfBoundsException.class, ()->chunk.getInt(0, bo));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getLong0(int i, Chunk chunk)
	{
		assertThrows(IndexOutOfBoundsException.class, ()->chunk.getLong(0, bo));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void getSize(int i, Chunk chunk)
	{
		long val = chunk.getSize();
		assertEquals(1L,val, ()->String.format("Size for 0x%02x was %d", i, val));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void isCoalesced(int i, Chunk chunk)
	{
		boolean val = chunk.isCoalesced();
		assertTrue(val, ()->String.format("IsCoalesced for 0x%02x was false", i));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void coalesce(int i, Chunk input)
	{
		Chunk result = input.coalesce();
		assertEquals(input, result, ()->String.format("Coalesced chunk for 0x%02x was not itself.",i));
	}

	@ParameterizedTest
	@MethodSource("getChunkOffLenCaseArguments")
	public void subChunkOffLenExpectedSuccess(int i, Chunk input, long chunkOff, long chunkLen, boolean expectSuccess)
	{
		if(expectSuccess)
		{
			//subChunkOffLenExpectedSuccess(i, input, chunkOff, chunkLen);
			Chunk expected = (chunkLen==0) ? Chunks.empty() : input;
			Chunk result = input.subChunk(chunkOff,chunkLen);
			assertEquals(expected, result, ()->String.format("Subchunk of 0x%02x at %d for length %d returned %s instead of %s.", i, chunkOff, chunkLen, result, expected));
		}
		else
		{
			//subChunkOffLenExpectedFailure(i, input, chunkOff, chunkLen);
			try
			{
				assertThrows(IndexOutOfBoundsException.class, ()->input.subChunk(chunkOff,chunkLen));
			}
			catch(IndexOutOfBoundsException expectedException)
			{
			}
		}
	}

	@ParameterizedTest
	@MethodSource("getChunkOffLenArrayOffLenCases")
	public void chunkOffLenArrayOffLenTest(int i, Chunk chunk, long chunkOff, long chunkLen, byte[] array, int arrayOff, boolean expectSuccess)
	{
		byte[] result;
		int arrayLen = (int)chunkLen;

		if(expectSuccess)
		{
			if(chunkLen < Integer.MIN_VALUE || Integer.MAX_VALUE < chunkLen)
				throw new IllegalArgumentException("chunkLen=" + chunkLen + " doesn't fit in an int.");
			byte[] expected = copy(array);

			if(arrayLen > 0)
				expected[arrayOff] = (byte)(i&0xff);
			result = chunk.copyTo(array, chunkOff, arrayOff, arrayLen);
			assertArrayEquals(expected, result, ()->String.format("Chunk 0x%02x did not return expected array for chunk offset %d chunk length %d array offset %d and array length %d.", i, chunkOff, chunkLen, arrayOff, arrayLen));
			assertEquals(array, result, ()->String.format("Chunk 0x%02x did not return the same array passed to it.", i));
		}
		else
		{
			try
			{
				result = chunk.copyTo(array, chunkOff, arrayOff, arrayLen);
				//if(chunkLen==0)
					fail(String.format("Chunk 0x%02x did not throw an expception for for chunk offset %d chunk length %d array offset %d and array length %d but returned %s.", i, chunkOff, chunkLen, arrayOff, array.length, Arrays.toString(result)));
			}
			catch(IndexOutOfBoundsException | ArithmeticException expectedException)
			{
			}
		}
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void copyToByteOff1Len0(int i, Chunk chunk)
	{
		long chunkOff = 1L;
		int arrayOff = 0;
		int arrayLen= 0;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		result = chunk.copyTo(input, chunkOff, arrayOff, arrayLen);
		assertArrayEquals(expected,result, ()->String.format("Chunk 0x%02x did not return empty array for offset %d and length %d but %s", i, chunkOff, arrayLen, Arrays.toString(result)));
		assertEquals(input, result, ()->String.format("Chunk 0x%02x did not return the same array passed to it.", i));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void copyToByteOff0Len1(int i, Chunk chunk)
	{
		long chunkOff = 0L;
		int arrayOff = 0;
		int arrayLen= 1;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		expected[0]=(byte)(i&0xff);
		result = chunk.copyTo(input, chunkOff, arrayOff, arrayLen);
		assertArrayEquals(expected,result, ()->String.format("Chunk 0x%02x %s did not return an array containing itself for offset %d and length %d but %s", i, chunk, chunkOff, arrayLen, Arrays.toString(result)));
		assertEquals(input, result, ()->String.format("Chunk 0x%02x did not return the same array passed to it.", i));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void copyToByteOff0Len1ArrayOff1(int i, Chunk chunk)
	{
		long chunkOff=0L;
		int arrayOff=1;
		int arrayLen=3;
		int length =1;
		byte[] expected = new byte[arrayLen];
		byte[] input = new byte[arrayLen];
		byte[] result;

		input[0]=expected[0]=(byte)((~i)&0xff);
		input[1]=(byte)((~i)&0xff);
		expected[1]=(byte)(i&0xff);
		input[2]=expected[2]=(byte)((~i)&0xff);
		result = chunk.copyTo(input, chunkOff, arrayOff, length);
		assertArrayEquals(expected, result, ()->String.format("Chunk 0x%02x %s did not return expected array %s for chunkOff %d, arrayLen %d, arrayOff %d and length %d but %s.", i, chunk, Arrays.toString(expected), chunkOff, arrayLen, arrayOff, length, result));
		assertEquals(input, result, ()->String.format("Chunk 0x%02x did not return the same array passed to it.", i));
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void copyToByteOff0Len2(int i, Chunk chunk)
	{
		long chunkOff = 0L;
		int arrayOff = 0;
		int arrayLen= 2;
		byte[] input = new byte[arrayLen];
		byte[] expected = new byte[arrayLen];

		expected[0]=expected[1]=input[0]=input[1]=(byte)((~i)&0xff);
		assertThrows(IndexOutOfBoundsException.class, ()->chunk.copyTo(input, chunkOff, arrayOff, arrayLen));
		assertArrayEquals(expected, input, ()->String.format("Chunk 0x%02x threw as expected for offset %d and length %d but modified the input array: %s", i, chunkOff, arrayOff, Arrays.toString(input)));
	}

	public static Stream<ChunkSPI> streamSPIs()
	{
		return IntStream
			.rangeClosed((int)(Byte.MIN_VALUE), (int)(Byte.MAX_VALUE))
			.mapToObj(Chunks::ofByte)
			.map((chunk)->chunk.getSPI());
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directHashCode(ChunkSPI spi)
	{
		assertEquals(spi.getByte(0L),spi.hashCode());
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directEqualsNull(ChunkSPI spi)
	{
		assertFalse(spi.equals(null));
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directEqualsString(ChunkSPI spi)
	{
		assertFalse(spi.equals("toast"));
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directEqualsEmpty(ChunkSPI spi)
	{
		assertFalse(spi.equals(EmptyChunkSPI.EMPTY_SPI));
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directEqualsPlus1(ChunkSPI spi)
	{
		ChunkSPI next = Chunks.ofByte((spi.getByte(0)+1)&0xff).getSPI();

		assertFalse(spi.equals(next));
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directEqualsMock(ChunkSPI spi)
	{
		ChunkSPI mock = EasyMock.mock(ChunkSPI.class);

		EasyMock.expect(mock.getSize()).andReturn(1L);
		EasyMock.expect(mock.getByte(0)).andReturn(spi.getByte(0));
		EasyMock.replay(mock);

		assertTrue(spi.equals(mock));
		EasyMock.verify(mock);
	}

	@ParameterizedTest
	@MethodSource("streamSPIs")
	public void directEqualsSelf(ChunkSPI spi)
	{
		assertTrue(spi.equals(spi));
	}

	/*
	@ParameterizedTest
	@MethodSource("streamTestChunks")
	public void checkToStrings(Chunk chunk)
	{
		assertNotNull(chunk.getSPI().toString());
	}

	@ParameterizedTest
	@MethodSource("getIntChunkTests")
	public void checkHashCode(int i, Chunk chunk)
	{
		int expected = Collections.singleton(Integer.valueOf(i&0xff).byteValue()).hashCode();
		int actual = chunk.getSPI().hashCode();

		assertEquals(expected, actual, ()->String.format("Expected hash code %d for chunk %s but got %d instead.", expected, chunk, actual));
	}

	@ParameterizedTest
	@MethodSource("streamTestChunks")
	public void checkEqualsNull(Chunk chunk)
	{
		assertFalse(chunk.getSPI().equals(null),()->{return "Chunk " + chunk + " was equal to null.";});
	}

	@ParameterizedTest
	@MethodSource("streamTestChunks")
	public void checkEqualsSelf(Chunk chunk)
	{
		assertTrue(chunk.getSPI().equals(chunk.getSPI()),()->{return "Chunk " + chunk + " was not equal to itself.";});
	}

	@SuppressWarnings("unlikely-arg-type")
	@ParameterizedTest
	@MethodSource("streamTestChunks")
	public void checkEqualsBoolean(Chunk chunk)
	{
		assertFalse(chunk.getSPI().equals(Boolean.FALSE),()->{return "Chunk " + chunk + " was equal to " + Boolean.FALSE + '.';});
	}

	@ParameterizedTest
	@MethodSource("streamTestChunks")
	public void checkEqualsNext(Chunk chunk)
	{
		Chunk next = ByteChunkSPI.instance((chunk.get(0)+1)&0xff);
		assertFalse(chunk.getSPI().equals(next.getSPI()),()->{return "Chunk " + chunk + " was equal to " + next + '.';});
	}

	@Test
	public void checkEqualsEqualNotSelf()
	{
		ChunkSPI self = ByteChunkSPI.instance(42).getSPI();
		ChunkSPI notself = ByteChunkSPI.instance42ForEqualsTestingOnly();
		assertTrue(self.equals(notself),()->{return "Chunk " + self + " was not equal to " + notself + '.';});
	}
	*/
}
