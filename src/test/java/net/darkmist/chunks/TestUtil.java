package net.darkmist.chunks;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.api.Assertions.*;

import org.opentest4j.AssertionFailedError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TestUtil
{
	private static final Logger logger = LoggerFactory.getLogger(TestSources.class);

	private TestUtil()
	{
	}

	static <T extends Serializable> byte[] serialize(T obj) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(obj);
		oos.close();
		return baos.toByteArray();
	}

	static <T extends Serializable> T deserialize(Class<T> cls, byte[] bytes) throws ClassNotFoundException, IOException
	{
		Object o;

		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);
		o = ois.readObject();
		return cls.cast(o);
	}

	static <T extends Serializable, O extends T> T serializeDeserialize(Class<T> cls, O obj) throws ClassNotFoundException, IOException
	{
		assertTrue(cls.isInstance(obj));
		return deserialize(cls, serialize(obj));
	}
}
