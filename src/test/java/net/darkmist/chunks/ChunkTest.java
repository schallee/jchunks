package net.darkmist.chunks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChunkTest
{
	private static <T extends Serializable> byte[] serialize(T obj) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(obj);
		oos.close();
		return baos.toByteArray();
	}

	private static <T extends Serializable> T deserialize(Class<T> cls, byte[] bytes) throws ClassNotFoundException, IOException
	{
		Object o;

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		o = ois.readObject();
		return cls.cast(o);
	}

	private static <T extends Serializable, O extends T> T serializeDeserialize(Class<T> cls, O obj) throws ClassNotFoundException, IOException
	{
		assertTrue(cls.isInstance(obj));
		return deserialize(cls, serialize(obj));
	}

	@Test
	public void requireValidOffLen0_0_0()
	{
		assertEquals(0, Util.requireValidOffLenRetEnd(0,0,0));
	}

	@Test
	public void emptySize()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		assertTrue(empty.isSizeLong());
		assertEquals(0l,empty.getSizeLong());
		assertTrue(empty.isSizeInt());
		assertEquals(0,empty.getSizeInt());
		assertEquals(0,empty.size());
	}

	@Test
	public void emptyGetLong0()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(0l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void emptyGetLongNeg1()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(-1l);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void emptyGetInt0()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(0);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void emptyGetIntNeg1()
	{
		Chunk empty;

		empty = Chunk.emptyInstance();
		try
		{
			empty.getByte(-1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void byte0Size()
	{
		Chunk zero = Chunk.byteInstance((byte)0);

		assertTrue(zero.isSizeLong());
		assertEquals(1l, zero.getSizeLong());
		assertTrue(zero.isSizeInt());
		assertEquals(1, zero.getSizeInt());
		assertEquals(1, zero.size());
	}

	@Test
	public void byte0Get0()
	{
		Chunk zero = Chunk.byteInstance(0);

		assertEquals(0, zero.getByte(0));
		assertEquals(0, zero.getByte(0l));
	}

	@Test
	public void byte0Get1()
	{
		Chunk zero = Chunk.byteInstance(0);

		try
		{
			zero.getByte(1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void byte0GetNeg1()
	{
		Chunk zero = Chunk.byteInstance(0);

		try
		{
			zero.getByte(-1);
			fail();
		}
		catch(IndexOutOfBoundsException expected)
		{
		}
	}

	@Test
	public void byteIntNeg1Get0()
	{
		Chunk chunk = Chunk.byteInstance(-1);

		assertTrue(chunk.isSizeLong());
		assertEquals(1l, chunk.getSizeLong());
		assertTrue(chunk.isSizeInt());
		assertEquals(1, chunk.getSizeInt());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@Test
	public void byteInt255Get0()
	{
		Chunk chunk = Chunk.byteInstance(255);

		assertTrue(chunk.isSizeLong());
		assertEquals(1l, chunk.getSizeLong());
		assertTrue(chunk.isSizeInt());
		assertEquals(1, chunk.getSizeInt());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@Test
	public void testStringSerialization() throws ClassNotFoundException, IOException
	{
		Chunk input = Chunk.instance("toast is yummy");
		Chunk expected = input;
		Chunk actual;

		actual = serializeDeserialize(Chunk.class, input);
		assertEquals(expected, actual);

	}

	@Test
	public void testStringCopy() throws ClassNotFoundException, IOException
	{
		String str = "toast is yummy";
		Chunk chunk;
		byte[] expected = str.getBytes(StandardCharsets.US_ASCII);
		byte[] actual;

		chunk = Chunk.instance(str);
		actual = chunk.copy();
		assertArrayEquals(expected, actual);
	}
}
