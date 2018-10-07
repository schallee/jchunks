package net.darkmist.chunks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

public class ReadOnlyByteBuffersTest
{
	private static class BufferMeta
	{
		private static final int INVALID = Integer.MIN_VALUE;

		private final boolean hasArray;
		private final int arrayOffset;
		private final int capacity;
		private final boolean isDirect;
		private final boolean isReadOnly;
		private final int position;
		private final int limit;
		private final boolean hasMark;
		private final int mark;

		private final ByteOrder byteOrder;

		private BufferMeta(ByteBuffer buf)
		{
			boolean markThrew;

			Objects.requireNonNull(buf);
			hasArray = buf.hasArray();
			if(hasArray)
				this.arrayOffset = buf.arrayOffset();
			else
				this.arrayOffset = INVALID;
			this.capacity = buf.capacity();
			this.isDirect = buf.isDirect();
			this.isReadOnly = buf.isReadOnly();
			this.position = buf.position();
			this.limit = buf.limit();

			// FIXME: is this really the only way to do this?
			try
			{
				buf.reset();
				markThrew=false;
			}
			catch(InvalidMarkException e)
			{
				markThrew=true;
			}

			if(markThrew)
			{
				this.hasMark = false;
				this.mark = INVALID;
			}
			else
			{
				this.hasMark = true;
				this.mark = buf.position();
				buf.position(position);
			}

			this.byteOrder = buf.order();
		}

		static BufferMeta instance(ByteBuffer buf)
		{
			return new BufferMeta(buf);
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o)
				return true;
			if(o==null)
				return false;
			if(BufferMeta.class != o.getClass())
				return false;
			BufferMeta that = (BufferMeta)o;
			if(this.hasArray != that.hasArray)
				return false;
			if(this.hasArray && (this.arrayOffset != that.arrayOffset))
				return false;
			if(this.capacity != that.capacity)
				return false;
			if(this.isDirect != that.isDirect)
				return false;
			if(this.position != that.position)
				return false;
			if(this.limit != that.limit)
				return false;
			if(this.hasMark != that.hasMark)
				return false;
			if(this.hasMark && (this.mark != that.mark))
				return false;
			if(!Objects.equals(this.byteOrder, that.byteOrder))
				return false;
			return true;
		}

		public boolean assertEquals(BufferMeta that)
		{
			//if(this==that)
				//return true;
			if(that==null)
				return false;
			Assertions.assertEquals(this.hasArray, that.hasArray);
			if(this.hasArray)
				Assertions.assertEquals(this.arrayOffset, that.arrayOffset);
			Assertions.assertEquals(this.capacity, that.capacity);
			Assertions.assertEquals(this.isDirect, that.isDirect);
			Assertions.assertEquals(this.position, that.position);
			Assertions.assertEquals(this.limit, that.limit);
			Assertions.assertEquals(this.hasMark, that.hasMark);
			if(this.hasMark)
				Assertions.assertEquals(this.hasMark, that.hasMark);
			Assertions.assertEquals(this.byteOrder, that.byteOrder);

			return true;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(
				hasArray,
				arrayOffset,
				capacity,
				isDirect,
				isReadOnly,
				position,
				limit,
				hasMark,
				mark,
				byteOrder
				);
		}
	}

	private static byte[] arrayOf(int...contents)
	{
		byte bytes[] = new byte[contents.length];

		for(int i=0;i<contents.length;i++)
			bytes[i] = (byte)contents[i];
		return bytes;
	}

	private static ByteBuffer bufOf(int...contents)
	{
		return ByteBuffer.wrap(arrayOf(contents));
	}

	private static byte[] copy(byte array[])
	{
		byte[] copy = new byte[array.length];

		System.arraycopy(array, 0, copy, 0, array.length);
		return copy;
	}

	private static byte[] invert(byte array[])
	{
		for(int i=0;i<array.length;i++)
			array[i] = (byte)(~array[i]);
		return array;
	}

	private static ByteBuffer invert(ByteBuffer buf)
	{
		int limit=buf.limit();

		for(int pos=buf.position(); pos<limit; pos++)
			buf.put(pos,(byte)(~buf.get(pos)));
		return buf;
	}

	@SuppressWarnings("ReferenceEquality")
	private static boolean allBytesNotEqual(ByteBuffer a, ByteBuffer b)
	{
		ByteBuffer aDup;
		ByteBuffer bDup;

		if(a.equals(b))
			return false;

		if(a.remaining() != b.remaining())
			throw new IllegalArgumentException("a and b have different lengths");
		
		aDup = a.duplicate();
		bDup = b.duplicate();

		while(aDup.hasRemaining())
			if(aDup.get() == bDup.get())
				return false;
		return true;
	}

	@Test
	public void copyBuf123()
	{
		ByteBuffer input = bufOf(1,2,3);
		BufferMeta inputPreMeta;
		BufferMeta inputPostMeta;
		ByteBuffer result;

		inputPreMeta = BufferMeta.instance(input);
		result = ReadOnlyByteBuffers.copy(input);
		inputPostMeta = BufferMeta.instance(input);

		assertEquals(input, result);
		assertNotSame(input, result);
		inputPreMeta.assertEquals(inputPostMeta);

		// make sure new buffer isn't just wrapping input
		invert(input);
		assertTrue(allBytesNotEqual(input, result));
	}

	@Test
	public void copyBuf1()
	{
		ByteBuffer input = bufOf(1);
		BufferMeta inputPreMeta;
		BufferMeta inputPostMeta;
		ByteBuffer result;

		inputPreMeta = BufferMeta.instance(input);
		result = ReadOnlyByteBuffers.copy(input);
		inputPostMeta = BufferMeta.instance(input);

		assertEquals(input, result);
		assertNotSame(input,result);
		inputPreMeta.assertEquals(inputPostMeta);

		// make sure new buffer isn't just wrapping input
		invert(input);
		assertTrue(allBytesNotEqual(input, result));
	}

	@Test
	public void copyBuf0()
	{
		ByteBuffer input = bufOf();
		BufferMeta inputPreMeta;
		BufferMeta inputPostMeta;
		ByteBuffer result;

		inputPreMeta = BufferMeta.instance(input);
		result = ReadOnlyByteBuffers.copy(input);
		inputPostMeta = BufferMeta.instance(input);

		assertEquals(input, result);
		assertNotSame(input,result);
		inputPreMeta.assertEquals(inputPostMeta);
	}

	@Test
	public void copyArray123()
	{
		byte[] orig = arrayOf(1,2,3);
		ByteBuffer origBuf = ByteBuffer.wrap(orig).asReadOnlyBuffer();
		byte[] input = copy(orig);
		ByteBuffer inputBuf = ByteBuffer.wrap(input).asReadOnlyBuffer();
		ByteBuffer result;

		result = ReadOnlyByteBuffers.copy(input);
		assertEquals(origBuf, result);

		// make sure new buffer isn't just wrapping input
		invert(input);
		assertTrue(allBytesNotEqual(inputBuf, result));
	}

	@Test
	public void copyArray1()
	{
		byte[] orig = arrayOf(1);
		ByteBuffer origBuf = ByteBuffer.wrap(orig).asReadOnlyBuffer();
		byte[] input = copy(orig);
		ByteBuffer inputBuf = ByteBuffer.wrap(input).asReadOnlyBuffer();
		ByteBuffer result;

		result = ReadOnlyByteBuffers.copy(input);
		assertEquals(origBuf, result);

		// make sure new buffer isn't just wrapping input
		invert(input);
		assertTrue(allBytesNotEqual(inputBuf, result));
	}

	@Test
	public void copyArray0()
	{
		byte[] orig = arrayOf(1);
		ByteBuffer origBuf = ByteBuffer.wrap(orig).asReadOnlyBuffer();
		byte[] input = copy(orig);
		ByteBuffer inputBuf = ByteBuffer.wrap(input).asReadOnlyBuffer();
		ByteBuffer result;

		result = ReadOnlyByteBuffers.copy(input);
		assertEquals(origBuf, result);

		// make sure new buffer isn't just wrapping input
		invert(input);
		assertTrue(allBytesNotEqual(inputBuf, result));
	}

	@Test
	public void asReadOnlyOrSelf_ReadOnly()
	{
		ByteBuffer input = bufOf(1,2,3);
		ByteBuffer result;

		assertFalse(input.isReadOnly());
		result = ReadOnlyByteBuffers.asReadOnlyOrSelf(input);
		assertEquals(input, result);
		assertTrue(result.isReadOnly());
		assertNotSame(input,result);
	}

	@Test
	public void asReadOnlyOrSelf_Self()
	{
		ByteBuffer input = bufOf(1,2,3).asReadOnlyBuffer();
		ByteBuffer result;

		assertTrue(input.isReadOnly());
		result = ReadOnlyByteBuffers.asReadOnlyOrSelf(input);
		assertEquals(input, result);
		assertTrue(result.isReadOnly());
		assertSame(input,result);
	}

	@SuppressWarnings("ByteBufferBackingArray")
	private static boolean assertUnsliced(ByteBuffer a, ByteBuffer b)
	{
		assertEquals(a.hasArray(), b.hasArray());
		if(a.hasArray())
			assertArrayEquals(a.array(),b.array());
		assertEquals(a.capacity(), b.capacity());
		return true;
	}

	// Note: unslicedSubRW is used for testing so we can assert that they are unsliced.

	@Test
	public void unslicedSubRW_123_0_3()
	{
		ByteBuffer input = bufOf(1,2,3);
		ByteBuffer result;

		result = ReadOnlyByteBuffers.unslicedSubRW(input, 0, 3);
		assertEquals(input, result);
		assertNotSame(input,result);
		assertUnsliced(input,result);
	}

	@Test
	public void unslicedSubRW_123_1_2()
	{
		ByteBuffer input = bufOf(1,2,3);
		ByteBuffer expected = bufOf(2,3);
		ByteBuffer result;

		result = ReadOnlyByteBuffers.unslicedSubRW(input, 1, 2);
		assertEquals(expected, result);
		assertNotSame(input,result);
		assertUnsliced(input,result);
	}

	@Test
	public void unslicedSubRW_123_1_1()
	{
		ByteBuffer input = bufOf(1,2,3);
		ByteBuffer expected = bufOf(2);
		ByteBuffer result;

		result = ReadOnlyByteBuffers.unslicedSubRW(input, 1, 1);
		assertEquals(expected, result);
		assertNotSame(input,result);
		assertUnsliced(input,result);
	}

	@Test
	public void unslicedSubRW_123_3_0()
	{
		ByteBuffer input = bufOf(1,2,3);
		ByteBuffer expected = bufOf();
		ByteBuffer result;

		result = ReadOnlyByteBuffers.unslicedSubRW(input, 3, 0);
		assertEquals(expected, result);
		assertNotSame(input,result);
		assertUnsliced(input,result);
	}

	@Test
	public void unslicedSubRW_123_1_4()
	{
		ByteBuffer input = bufOf(1,2,3);

		try
		{
			ReadOnlyByteBuffers.unslicedSubRW(input, 1, 4);
			fail();
		}
		catch(Exception expected)
		{
		}
	}

	@Test
	public void unslicedSubRW_123_3_MAX()
	{
		ByteBuffer input = bufOf(1,2,3);

		try
		{
			ReadOnlyByteBuffers.unslicedSubRW(input, 3, Integer.MAX_VALUE);
			fail();
		}
		catch(Exception expected)
		{
		}
	}

	@Test
	public void copyBufNull()
	{
		ByteBuffer input = null;
		ByteBuffer expected = ReadOnlyByteBuffers.EMPTY;
		ByteBuffer actual;

		actual = ReadOnlyByteBuffers.copy(input);
		assertSame(expected, actual);
	}

	@Test
	public void copyArrayNull()
	{
		byte[] input = null;
		ByteBuffer expected = ReadOnlyByteBuffers.EMPTY;
		ByteBuffer actual;

		actual = ReadOnlyByteBuffers.copy(input);
		assertSame(expected, actual);
	}

	@Test
	public void copyArrayEmpty()
	{
		byte[] input = new byte[0];
		ByteBuffer expected = ReadOnlyByteBuffers.EMPTY;
		ByteBuffer actual;

		actual = ReadOnlyByteBuffers.copy(input);
		assertSame(expected, actual);
	}

	// Note: unslicedSub is basically a pass through to unslicedRW so we only need one check.
	@Test
	public void unslicedSub_123_0_3()
	{
		ByteBuffer input = bufOf(1,2,3);
		ByteBuffer result;

		result = ReadOnlyByteBuffers.unslicedSub(input, 0, 3);
		assertEquals(input, result);
		assertNotSame(input,result);
	}


	/*
	@Test
	public void copyNoRemaining()
	{
		ByteBuffer input = ByteBuffer.wrap(new byte[]{0,1,2,3});
		ByteBuffer expected = ReadOnlyByteBuffers.EMPTY;
		ByteBuffer actual;

		input.position(input.limit());
		assertFalse(input.hasRemaining());
		actual = ReadOnlyByteBuffers.copy(input);
		assertSame(expected, actual);
	}
	*/
}
