package net.darkmist.chunks;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.google.errorprone.annotations.Var;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilTest
{
	private static final Logger logger = LoggerFactory.getLogger(UtilTest.class);
	private static final List<Integer> INT_TESTS = mkIntTests(
		0,
		-1,
		1,
		(int)(Byte.MAX_VALUE),
		(int)(Byte.MIN_VALUE),
		(int)(Short.MAX_VALUE),
		(int)(Short.MIN_VALUE),
		Integer.MAX_VALUE,
		Integer.MIN_VALUE
	);
	private static final List<Long> LONG_TESTS = mkLongTests(
		0L,
		-1L,
		1L,
		(long)(Byte.MAX_VALUE),
		(long)(Byte.MIN_VALUE),
		(long)(Short.MAX_VALUE),
		(long)(Short.MIN_VALUE),
		(long)(Integer.MAX_VALUE),
		(long)(Integer.MIN_VALUE),
		Long.MAX_VALUE,
		Long.MIN_VALUE
	);

	private static List<Integer> mkIntTests(int...base_tests)
	{
		Set<Integer> tests = new TreeSet<>();

		for(int i : base_tests)
		{
			tests.add(i-2);
			tests.add(i-1);
			tests.add(i);
			tests.add(i+1);
			tests.add(i+2);
		}
		return Collections.unmodifiableList(new ArrayList<>(tests));
	}

	private static List<Long> mkLongTests(long...base_tests)
	{
		Set<Long> tests = new TreeSet<>();

		for(long l : base_tests)
		{
			tests.add(l-2);
			tests.add(l-1);
			tests.add(l);
			tests.add(l+1);
			tests.add(l+2);
		}
		return Collections.unmodifiableList(new ArrayList<>(tests));
	}

	@Test
	public void requireValidOffLen0_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(0,0,0));
	}

	@Test
	public void requireValidOffLen10_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(10,0,0));
	}

	@Test
	public void requireValidOffLen10_0_1()
	{
		assertEquals(1, Util.requireValidOffLenRetEnd(10,0,1));
	}

	@Test
	public void requireValidOffLen10_1_1()
	{
		assertEquals(2, Util.requireValidOffLenRetEnd(10,1,1));
	}

	@Test
	public void requireValidOffLen10_0_10()
	{
		assertEquals(10, Util.requireValidOffLenRetEnd(10,0,10));
	}

	@Test
	public void requireValidOffLen10_1_10()
	{
		assertThrows(Exception.class, ()->Util.requireValidOffLen(10,1,10));
	}

	@Test
	public void requireValidOffLenNeg1_Neg1_Neg1()
	{
		assertThrows(Exception.class, ()->Util.requireValidOffLen(-1,-1,-1));
	}

	@Test
	public void requireValidOffLen10_5_MAX()
	{
		assertThrows(Exception.class, ()->Util.requireValidOffLen(10,5,Integer.MAX_VALUE));
	}

	@Test
	public void requireValidOffLen10_MIN_5()
	{
		assertThrows(Exception.class, ()->Util.requireValidOffLen(10,Integer.MIN_VALUE,5));
	}

	private static byte[] dataOSShortBE2Bytes(short s)
	{	// Likely the most inefficient way to do this but we're verifing our implementation.
		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(Short.BYTES);
			DataOutputStream dos = new DataOutputStream(baos);
		)
		{
			dos.writeShort(s);
			dos.flush();
			return baos.toByteArray();
		}
		catch(IOException e)
		{
			throw new IllegalStateException("Error writing short to byte array.", e);
		}
	}

	private static byte[] dataOSShortLE2Bytes(short s)
	{
		return dataOSShortBE2Bytes(Short.reverseBytes(s));
	}

	@Test
	public void shortFromBytesBE()
	{
		for(int i=Short.MIN_VALUE;i<=Short.MAX_VALUE;i++)
		{
			byte[] inputBytes = dataOSShortBE2Bytes((short)i);
			//if(logger.isDebugEnabled())
				//logger.debug("{}", String.format("Short %s: a=%02x, b=%02x, input=%04x=%d", ByteOrder.BIG_ENDIAN, inputBytes[0], inputBytes[1], i&0xffff, i));
			assertEquals(i, Util.shortFromBytes(inputBytes,ByteOrder.BIG_ENDIAN));
			assertEquals(i, Util.shortFromBytesBigEndian(inputBytes));
		}
	}

	@Test
	public void shortFromBytesLE()
	{
		for(int i=Short.MIN_VALUE;i<=Short.MAX_VALUE;i++)
		{
			byte[] inputBytes = dataOSShortLE2Bytes((short)i);
			//if(logger.isDebugEnabled())
				//logger.debug("{}", String.format("Short %s: a=%02x, b=%02x, input=%04x=%d", ByteOrder.LITTLE_ENDIAN, inputBytes[0], inputBytes[1], i&0xffff, i));
			assertEquals(i, Util.shortFromBytes(inputBytes,ByteOrder.LITTLE_ENDIAN));
			assertEquals(i, Util.shortFromBytesLittleEndian(inputBytes));
		}
	}

	private static byte[] dataOSIntBE2Bytes(int i)
	{	// Likely the most inefficient way to do this but we're verifing our implementation.
		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(Integer.BYTES);
			DataOutputStream dos = new DataOutputStream(baos);
		)
		{
			dos.writeInt(i);
			dos.flush();
			return baos.toByteArray();
		}
		catch(IOException e)
		{
			throw new IllegalStateException("Error writing int to byte array.", e);
		}
	}

	private static byte[] dataOSIntLE2Bytes(int i)
	{
		return dataOSIntBE2Bytes(Integer.reverseBytes(i));
	}

	@Test
	public void intFromBytesBE()
	{
		for(int input : INT_TESTS)
		{
			byte[] inputBytes = dataOSIntBE2Bytes(input);
			//if(logger.isDebugEnabled())
				//logger.debug("Int BE: {}", String.format("a=%02x, b=%02x, c=%02x, d=%02x input=%08x=%d", inputBytes[0], inputBytes[1], inputBytes[2], inputBytes[3], input, input));
			assertEquals(input, Util.intFromBytes(inputBytes,ByteOrder.BIG_ENDIAN));
			assertEquals(input, Util.intFromBytesBigEndian(inputBytes));
		}
	}

	@Test
	public void intFromBytesLE()
	{
		for(int input : INT_TESTS)
		{
			byte[] inputBytes = dataOSIntLE2Bytes(input);
			//if(logger.isDebugEnabled())
				//logger.debug("Int LE: {}", String.format("a=%02x, b=%02x, c=%02x, d=%02x input=%08x=%d", inputBytes[0], inputBytes[1], inputBytes[2], inputBytes[3], input, input));
			assertEquals(input, Util.intFromBytes(inputBytes,ByteOrder.LITTLE_ENDIAN));
			assertEquals(input, Util.intFromBytesLittleEndian(inputBytes));
		}
	}

	/*
	private static long dataISBytes2LongBE(byte[] bytes)
	{	// Likely the most inefficient way to do this but we're verifing our implementation.
		assertNotNull(bytes);
		assertEquals(Long.BYTES, bytes.length);

		try
		(
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			DataInputStream dis = new DataInputStream(bais);
		)
		{
			return dis.readLong();
		}
		catch(IOException e)
		{
			throw new IllegalStateException("Error reading long from byte array.", e);
		}
	}

	private static long dataISBytes2LongLE(byte[] bytes)
	{
		return Long.reverse(dataISBytes2LongBE(bytes));
	}
	*/

	private static byte[] dataOSLongBE2Bytes(long l)
	{	// Likely the most inefficient way to do this but we're verifing our implementation.
		//if(logger.isDebugEnabled())
			//logger.debug("BE={}", String.format("%016x", l));
		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(Integer.BYTES);
			DataOutputStream dos = new DataOutputStream(baos);
		)
		{
			dos.writeLong(l);
			dos.flush();
			return baos.toByteArray();
		}
		catch(IOException e)
		{
			throw new IllegalStateException("Error writing long to byte array.", e);
		}
	}

	private static byte[] dataOSLongLE2Bytes(long l)
	{
		//if(logger.isDebugEnabled())
			//logger.debug("preLE={}", String.format("%016x", l));
		return dataOSLongBE2Bytes(Long.reverseBytes(l));
	}

	@Test
	public void longFromBytesBE()
	{
		for(long input : LONG_TESTS)
		{
			byte[] inputBytes = dataOSLongBE2Bytes(input);
			assertEquals(input, Util.longFromBytes(inputBytes,ByteOrder.BIG_ENDIAN));
			assertEquals(input, Util.longFromBytesBigEndian(inputBytes));
		}
	}

	@Test
	public void longFromBytesLE()
	{
		for(long input : LONG_TESTS)
		{
			byte[] inputBytes = dataOSLongLE2Bytes(input);
			//if(logger.isDebugEnabled())
				//logger.debug("Long LE: {}", String.format("a=%02x, b=%02x, c=%02x, d=%02x, e=%02x, f=%02x, g=%02x, h=%02x, input=%d", inputBytes[0], inputBytes[1], inputBytes[2], inputBytes[3], inputBytes[4], inputBytes[5], inputBytes[6], inputBytes[7], input));
			assertEquals(input, Util.longFromBytes(inputBytes,ByteOrder.LITTLE_ENDIAN));
			assertEquals(input, Util.longFromBytesLittleEndian(inputBytes));
		}
	}

	private static void tryIsExtendedByteValueGood(int i)
	{
		assertTrue(Util.isExtendedByteValue(i),()->String.format("%d=0x%x was not accepted.",i,i));
	}

	@Test
	public void isExtendedByteValueGood()
	{
		for(int i=Byte.MIN_VALUE;i<=Byte.MAX_VALUE;i++)
			tryIsExtendedByteValueGood(i);
		for(int i=Byte.MAX_VALUE+1;i<=0xff;i++)
			tryIsExtendedByteValueGood(i);
	}

	private static void tryIsExtendedByteValueBad(int i)
	{
		assertFalse(Util.isExtendedByteValue(i),()->String.format("%d=0x%x was accepted.",i,i));
	}

	@Test
	public void isExtendedByteValueBad()
	{
		tryIsExtendedByteValueBad(0x100);
		tryIsExtendedByteValueBad(0x101);
		// all pow2
		for(int i=0x200;i!=0;i<<=1)
		{
			tryIsExtendedByteValueBad(i-1);
			tryIsExtendedByteValueBad(i);
			tryIsExtendedByteValueBad(i+1);
		}
		for(int i=((int)(Byte.MIN_VALUE))*2;i<Byte.MIN_VALUE;i++)
			tryIsExtendedByteValueBad(i);
		for(int i=0x100;i<((int)(Byte.MAX_VALUE))*3;i++)
			tryIsExtendedByteValueBad(i);
	}

	@Test
	public void testRequireExtendedByteValue256()
	{
		assertThrows(IllegalArgumentException.class, ()->Util.requireExtendedByteValue(256));
	}

	public static IntStream streamRequirePosIntInt()
	{
		return IntStream.of(
			0,
			1,
			2,
			Integer.MAX_VALUE-2,
			Integer.MAX_VALUE-1,
			Integer.MAX_VALUE
		);
	}

	@ParameterizedTest
	@MethodSource("streamRequirePosIntInt")
	public void testRequirePosIntInt(int i)
	{
		int result;
		
		result = Util.requirePosInt(i);
		assertEquals(i,result);
	}

	public static IntStream streamRequirePosIntIntFail()
	{
		return IntStream.of(
			Integer.MIN_VALUE,
			Integer.MIN_VALUE+1,
			-1
			-2
		);
	}

	@ParameterizedTest
	@MethodSource("streamRequirePosIntIntFail")
	public void testRequirePosIntIntFail(int i)
	{
		assertThrows(IllegalArgumentException.class, ()->Util.requirePosInt(i));
	}

	@ParameterizedTest
	@MethodSource("streamRequirePosIntIntFail")
	public void testRequirePosIntIntFail(long l)
	{
		assertThrows(IllegalArgumentException.class, ()->Util.requirePosInt(l));
	}

	public static LongStream streamRequirePosIntLong()
	{
		return LongStream.of(
			0L,
			1L,
			2L,
			Integer.MAX_VALUE-2L,
			Integer.MAX_VALUE-1L,
			(long)Integer.MAX_VALUE
		);
	}

	@ParameterizedTest
	@MethodSource("streamRequirePosIntInt")
	public void testRequirePosIntLong(int l)
	{
		int result;
		
		result = Util.requirePosInt(l);
		assertEquals(l,result);
	}

	public static LongStream streamRequirePosIntLongFail()
	{
		return LongStream.of(
			Long.MIN_VALUE,
			Long.MIN_VALUE+1,
			(long)Integer.MIN_VALUE-1,
			(long)Integer.MIN_VALUE,
			(long)Integer.MIN_VALUE+1,
			-2,
			-1,
			(long)Integer.MAX_VALUE,
			(long)Integer.MAX_VALUE+1,
			(long)Integer.MAX_VALUE+2,
			Long.MAX_VALUE-1,
			Long.MAX_VALUE
		);
	}

	public static Stream<Arguments> streamInvalidRequireValidOffLenRetEndInt()
	{
		return Stream.of(
			Arguments.of(-1,0,0),
			Arguments.of(1,-1,0),
			Arguments.of(1,0,-1),
			Arguments.of(1,2,1),
			Arguments.of(Integer.MAX_VALUE,Integer.MAX_VALUE/2+1,Integer.MAX_VALUE/2 + 1)
		);
	}

	@ParameterizedTest
	@MethodSource("streamInvalidRequireValidOffLenRetEndInt")
	public void testInvalidRequireValidOffLenRetEndInt(int arrayLen, int off, int len)
	{
		try
		{
			int end = Util.requireValidOffLenRetEnd(arrayLen, off, len);
			fail("Got end " + end + " instead of exception being thrown.");
		}
		catch(IndexOutOfBoundsException | IllegalArgumentException e)
		{
			logger.trace("Received expected exception", e);
		}
		//assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffLenRetEnd(arrayLen, off, len));
		//assertThrows(IllegalArgumentException.class, ()->Util.requireValidOffLenRetEnd(arrayLen, off, len));
	}

	public static Stream<Arguments> streamInvalidRequireValidOffLenRetEndLong()
	{
		return Stream.of(
			Arguments.of(-1L,0L,0L),
			Arguments.of(1L,-1L,0L),
			Arguments.of(1L,0L,-1L),
			Arguments.of(1L,2L,1L),
			Arguments.of(Long.MAX_VALUE,Long.MAX_VALUE/2L+1L,Long.MAX_VALUE/2L + 1L)
		);
	}

	@ParameterizedTest
	@MethodSource("streamInvalidRequireValidOffLenRetEndLong")
	public void testInvalidRequireValidOffLenRetEndLong(long arrayLen, long off, long len)
	{
		try
		{
			long result = Util.requireValidOffLenRetEnd(arrayLen, off, len);
			fail("Instead of throwing, " + result + " retruned.");
		}
		catch(IndexOutOfBoundsException | IllegalArgumentException e)
		{
			logger.trace("Received expected exception", e);
		}
		//assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffLenRetEnd(arrayLen, off, len));
		//assertThrows(IllegalArgumentException.class, ()->Util.requireValidOffLenRetEnd(arrayLen, off, len));
	}

	@Test
	public void TestIntRequireValidOffLenNegLen()
	{
		assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffLen(2,1,-1));
	}

	@Test
	public void TestIntRequireValidOffLenOffGTArrayLen()
	{
		assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffLen(2,1,3));
	}

	@Test
	public void TestIntRequireValidOffLenOffOffLenOverflow()
	{
		assertEquals(Integer.MAX_VALUE,
			Util.requireValidOffLenRetEnd(
				Integer.MAX_VALUE,
				Integer.MAX_VALUE/2+1,
				Integer.MAX_VALUE/2
			)
		);
	}

	@Test
	public void testLongRequireValidOffLenNegLen()
	{
		assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffLen(2L,1L,-1L));
	}

	@Test
	public void testLongRequireValidOffLenOffGTArrayLen()
	{
		assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffLen(2L,1L,3L));
	}

	@Test
	public void testLongRequireValidOffLenOffOffLenOverflow()
	{
		assertEquals(Long.MAX_VALUE,
			Util.requireValidOffLenRetEnd(
				Long.MAX_VALUE,
				Long.MAX_VALUE/2+1,
				Long.MAX_VALUE/2
			)
		);
	}

	@Test
	public void testLittleFromBigShort()
	{
		short input = (short)0x0123;
		short expected = (short)0x2301;
		short actual;

		actual = Util.fromBig(input, ByteOrder.LITTLE_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testLittleFromBigInteger()
	{
		int input = 0x01234567;
		int expected = 0x67452301;
		int actual;

		actual = Util.fromBig(input, ByteOrder.LITTLE_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void testLittleFromBigLong()
	{
		long input = 0x0123456789abcdefL;
		long expected = 0xefcdab8967452301L;
		long actual;

		actual = Util.fromBig(input, ByteOrder.LITTLE_ENDIAN);
		assertEquals(expected, actual);
	}

	@Test
	public void BytesFromShortLittle()
	{
		short input = (short)0x0123;
		byte[] expected = new byte[]{0x23,0x01};
		byte[] actual;

		actual = Util.bytesFrom(input, ByteOrder.LITTLE_ENDIAN);
		assertArrayEquals(expected,actual);
	}

	@Test
	public void BytesFromIntLittle()
	{
		int input = 0x01234567;
		byte[] expected = new byte[]{(byte)0x67, (byte)0x45, 0x23, 0x01};
		byte[] actual;

		actual = Util.bytesFrom(input, ByteOrder.LITTLE_ENDIAN);
		assertArrayEquals(expected,actual);
	}

	@Test
	public void BytesFromLongLittle()
	{
		long input = 0x0123456789abcdefL;
		byte[] expected = new byte[]{(byte)0xef, (byte)0xcd, (byte)0xab, (byte)0x89, (byte)0x67, (byte)0x45, 0x23, 0x01};
		byte[] actual;

		actual = Util.bytesFrom(input, ByteOrder.LITTLE_ENDIAN);
		assertArrayEquals(expected,actual);
	}

	// This attempts to allocate buffers until a failure so the list
	// is never accessed. Hence the SuppressWarnings
	@SuppressWarnings("ModifiedButNotUsed")
	@Test
	public void guardedAllocateBytes()
	{
		List<byte[]> arrays = new ArrayList<>();
		@Var
		byte[] bytes;

		bytes = Util.guardedAllocateBytes(Integer.MAX_VALUE);
		while(bytes!=null)
		{
			arrays.add(bytes);
			bytes = Util.guardedAllocateBytes(Integer.MAX_VALUE);
		}
	}

	@Test
	public void isBigNull()
	{
		assertTrue(Util.isBig(null));
	}

	@Test
	public void isBigBIG_ENDIAN()
	{
		assertTrue(Util.isBig(ByteOrder.BIG_ENDIAN));
	}

	@Test
	public void requireValidOffsetZero()
	{
		long size = 1;
		long off = 0;
		long expected = off;
		long actual;

		actual = Util.requireValidOffset(size, off);
		assertEquals(expected, actual);
	}

	@Test
	public void requireValidOffSetFailNeg()
	{
		long size = 0;
		long off = -1;

		assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffset(size, off));
	}

	@Test
	public void requireValidOffSetFailTooBig()
	{
		long size = 1;
		long off = 2;

		assertThrows(IndexOutOfBoundsException.class, ()->Util.requireValidOffset(size, off));
	}

	@Test
	public void requirePosLongZero()
	{
		long input = 0;
		long expected = input;
		long actual;

		actual = Util.requirePos(input);
		assertEquals(expected, actual);
	}

	@Test
	public void requirePosLongFail()
	{
		long input = -1;

		assertThrows(IllegalArgumentException.class, ()->Util.requirePos(input));
	}

	@Test
	public void testIsPosIntLongNeg1()
	{
		assertFalse(Util.isPosInt(-1L));
	}

	@Test
	public void testIsPosIntLong0()
	{
		assertTrue(Util.isPosInt(0L));
	}

	@Test
	public void testIsPosIntLongMaxInt()
	{
		assertTrue(Util.isPosInt((long)(Integer.MAX_VALUE)));
	}

	@Test
	public void testIsPosIntLongMaxIntPlus1()
	{
		assertFalse(Util.isPosInt(1L + Integer.MAX_VALUE));
	}
}
