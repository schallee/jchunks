package net.darkmist.chunks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

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
		0l,
		-1l,
		1l,
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
		try
		{
			Util.requireValidOffLen(10,1,10);
			fail();
		}
		catch(Exception expected)
		{
		}
	}

	@Test
	public void requireValidOffLenNeg1_Neg1_Neg1()
	{
		try
		{
			Util.requireValidOffLen(-1,-1,-1);
			fail();
		}
		catch(Exception e)
		{
			// expected
		}
	}

	@Test
	public void requireValidOffLen10_5_MAX()
	{
		try
		{
			Util.requireValidOffLen(10,5,Integer.MAX_VALUE);
			fail();
		}
		catch(Exception e)
		{
			// expected
		}
	}

	@Test
	public void requireValidOffLen10_MIN_5()
	{
		try
		{
			Util.requireValidOffLen(10,Integer.MIN_VALUE,5);
			fail();
		}
		catch(Exception e)
		{
			// expected
		}
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
		byte[] inputBytes;

		for(int i=Short.MIN_VALUE;i<=Short.MAX_VALUE;i++)
		{
			inputBytes = dataOSShortBE2Bytes((short)i);
			//if(logger.isDebugEnabled())
				//logger.debug("{}", String.format("Short %s: a=%02x, b=%02x, input=%04x=%d", ByteOrder.BIG_ENDIAN, inputBytes[0], inputBytes[1], i&0xffff, i));
			assertEquals(i, Util.shortFromBytes(inputBytes,ByteOrder.BIG_ENDIAN));
			assertEquals(i, Util.shortFromBytesBigEndian(inputBytes));
		}
	}

	@Test
	public void shortFromBytesLE()
	{
		byte[] inputBytes;

		for(int i=Short.MIN_VALUE;i<=Short.MAX_VALUE;i++)
		{
			inputBytes = dataOSShortLE2Bytes((short)i);
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
		byte[] inputBytes;

		for(int input : INT_TESTS)
		{
			inputBytes = dataOSIntBE2Bytes(input);
			//if(logger.isDebugEnabled())
				//logger.debug("Int BE: {}", String.format("a=%02x, b=%02x, c=%02x, d=%02x input=%08x=%d", inputBytes[0], inputBytes[1], inputBytes[2], inputBytes[3], input, input));
			assertEquals(input, Util.intFromBytes(inputBytes,ByteOrder.BIG_ENDIAN));
			assertEquals(input, Util.intFromBytesBigEndian(inputBytes));
		}
	}

	@Test
	public void intFromBytesLE()
	{
		byte[] inputBytes;

		for(int input : INT_TESTS)
		{
			inputBytes = dataOSIntLE2Bytes(input);
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
		byte[] inputBytes;

		for(long input : LONG_TESTS)
		{
			inputBytes = dataOSLongBE2Bytes(input);
			assertEquals(input, Util.longFromBytes(inputBytes,ByteOrder.BIG_ENDIAN));
			assertEquals(input, Util.longFromBytesBigEndian(inputBytes));
		}
	}

	@Test
	public void longFromBytesLE()
	{
		byte[] inputBytes;

		for(long input : LONG_TESTS)
		{
			inputBytes = dataOSLongLE2Bytes(input);
			//if(logger.isDebugEnabled())
				//logger.debug("Long LE: {}", String.format("a=%02x, b=%02x, c=%02x, d=%02x, e=%02x, f=%02x, g=%02x, h=%02x, input=%d", inputBytes[0], inputBytes[1], inputBytes[2], inputBytes[3], inputBytes[4], inputBytes[5], inputBytes[6], inputBytes[7], input));
			assertEquals(input, Util.longFromBytes(inputBytes,ByteOrder.LITTLE_ENDIAN));
			assertEquals(input, Util.longFromBytesLittleEndian(inputBytes));
		}
	}

	private static void tryIsExtendedByteValueGood(int i)
	{
		assertTrue(String.format("%d=0x%x was not accepted.",i,i),Util.isExtendedByteValue(i));
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
		assertFalse(String.format("%d=0x%x was accepted.",i,i),Util.isExtendedByteValue(i));
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
}
