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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkTest
{
	private static final Logger logger = LoggerFactory.getLogger(ChunkTest.class);

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
	public void byte0Size()
	{
		Chunk zero = Chunks.of((byte)0);

		logger.debug("zero.spi={}", zero.getSPI());
		logger.debug("zero={} zero.spi={}", zero, zero.getSPI());
		assertEquals(1l, zero.getSize());
		assertEquals(1, zero.size());
	}

	@Test
	public void byte0Get0()
	{
		Chunk zero = Chunks.of(0);

		assertEquals(0, zero.getByte(0));
		assertEquals(0, zero.getByte(0l));
	}

	@Test
	public void byte0Get1()
	{
		Chunk zero = Chunks.of(0);

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
		Chunk zero = Chunks.of(0);

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
		Chunk chunk = Chunks.of(-1);

		assertEquals(1l, chunk.getSize());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@Test
	public void byteInt255Get0()
	{
		Chunk chunk = Chunks.of(255);

		logger.debug("chunk.spi={}", chunk, chunk.getSPI());
		logger.debug("chunk={} chunk.spi={}", chunk, chunk.getSPI());
		assertEquals(1l, chunk.getSize());
		assertEquals(1, chunk.size());
		assertEquals(-1, (int)(chunk.get(0)));
	}

	@Test
	public void testStringSerialization() throws ClassNotFoundException, IOException
	{
		Chunk input = Chunks.from("toast is yummy");
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

		chunk = Chunks.from(str);
		actual = chunk.copy();
		assertArrayEquals(expected, actual);
	}
}
