package net.darkmist.chunks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.stream.Stream;
import java.util.function.Supplier;

import com.google.errorprone.annotations.Var;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.ParameterizedTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempt to test {@link ChunkSerializationProxy}. We have to do this by working around the serializtion system as we need to generate {@link ObjectInputStream} with invalid values for deserializing from.
 *
 * For {@link ChunkSerializationProxy#readExternal(ObjectInput)} we do this by writing the <code>long</code> and <code>byte[]</code> to a {@link ObjectOutputStream} and then calling <code>readExternal(ObjectInput)</code> directly. In this manner we don't have to fake the necessary class header values in the stream that would be needed to test via {@link ObjectInputStream.readObject()}.
 */
public class ChunkSerializationProxyTest
{
	private static final Logger logger = LoggerFactory.getLogger(ChunkSerializationProxy.class);

	private static ObjectInputStream mkObjStreamTest(long size, byte...bytes)
	{
		try
		(
			ByteArrayOutputStream baos = new ByteArrayOutputStream(Long.BYTES+bytes.length);
			ObjectOutputStream oos = new ObjectOutputStream(baos)
		)
		{
			oos.writeLong(size);
			oos.write(bytes);
			oos.flush();
			return new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));

		}
		catch(IOException e)
		{
			throw new IllegalStateException("Failed to serialize long and bytes to ByteArrayOutputStream.", e);
		}
	}

	private static Arguments mkReadExternalTest(Supplier<String> infoSupplier, Chunk expected, long size, byte...bytes)
	{
		return Arguments.of(
			infoSupplier,
			expected,
			size,
			bytes
		);
	}

	private static Arguments mkReadExternalTest(Supplier<String> infoSupplier, Chunk expected, long size, int...ints)
	{
		byte[] bytes = new byte[ints.length];
		for(int i=0; i<ints.length; i++)
			bytes[i] = Util.requireExtendedByteValue(ints[i]);
		return mkReadExternalTest(infoSupplier, expected, size, bytes);
	}

	public static Stream<Arguments> streamValidReadExternalTests()
	{
		return Stream.of(
			mkReadExternalTest(()->"Empty Chunk",		Chunks.empty(),			0L),
			mkReadExternalTest(()->"Single byte chunk",	Chunks.ofByte(0x55),		1L, 0x55),
			mkReadExternalTest(()->"Single byte chunk",	Chunks.ofBytes(0x00, 0x55, 0xff),	3L, 0x00, 0x55, 0xff)
		);
	}

	@ParameterizedTest
	@MethodSource("streamValidReadExternalTests")
	public void validReadExternalTest(Supplier<String> testInfo, Chunk expected, long size, byte...bytes) throws IOException
	{
		ChunkSerializationProxy proxy;
		ObjectInputStream ois;
		Object o;
		Chunk actual;

		proxy = new ChunkSerializationProxy();
		ois = mkObjStreamTest(size, bytes);
		proxy.readExternal(ois);
		o = proxy.readResolve();
		assertTrue(o instanceof Chunk);
		actual = (Chunk)o;
		assertEquals(expected, actual, ()->{return "Expected test " + testInfo.get() + " did not return expected results:\n" +
			"\texpected: " + expected + '\n' +
			"\t  actual: " + actual + '\n';}
		);
	}

	public static Stream<Arguments> streamInvalidReadExternalTests()
	{
		return Stream.of(
			mkReadExternalTest(()->"Negative size.",null, -1),
			mkReadExternalTest(()->"Long.MAX_VALUE size",null, Long.MAX_VALUE)
		);
	}

	@ParameterizedTest
	@MethodSource("streamInvalidReadExternalTests")
	public void invalidReadExternalTest(Supplier<String> testInfo, Chunk expected, long size, byte...bytes) throws IOException
	{
		ChunkSerializationProxy proxy = new ChunkSerializationProxy();
		@Var
		boolean oisCreated=false;

		try
		(
			ObjectInputStream ois = mkObjStreamTest(size, bytes);
		)
		{
			oisCreated=true;
			proxy.readExternal(ois);
			fail("Expected Read external to throw an exception for " + testInfo.get() + '.');
		}
		catch(IOException | UnsupportedOperationException e)
		{
			if(oisCreated)
			{
				if(logger.isDebugEnabled())
					logger.debug("Caught expected exception for {}.", testInfo.get(), e);
			}
			else // exception came from mkObjStreamTest
				throw e;
		}
	}

	@Test
	public void readResolveLove()
	{
		ChunkSerializationProxy proxy = new ChunkSerializationProxy();

		assertThrows(IOException.class, ()->proxy.readResolve());
	}

	@Test
	public void testToStringNoChunk()
	{
		assertNotNull(new ChunkSerializationProxy().toString());
	}

	@Test
	public void testToStringChunk()
	{
		assertNotNull(new ChunkSerializationProxy(Chunks.empty()).toString());
	}

	@Test
	public void testEqualsRefEquals()
	{
		ChunkSerializationProxy proxy = new ChunkSerializationProxy(Chunks.empty());
		assertTrue(proxy.equals(proxy));
	}

	@Test
	public void testEqualsChunkEquals()
	{
		ChunkSerializationProxy a = new ChunkSerializationProxy(Chunks.empty());
		ChunkSerializationProxy b = new ChunkSerializationProxy(Chunks.empty());
		assertTrue(a.equals(b));
		assertTrue(a.hashCode()==b.hashCode());
	}

	@Test
	public void testNotEqualsNull()
	{
		ChunkSerializationProxy proxy = new ChunkSerializationProxy(Chunks.empty());
		assertFalse(proxy.equals(null));
	}

	@Test
	public void testNotEqualsNonProxy()
	{
		ChunkSerializationProxy proxy = new ChunkSerializationProxy(Chunks.empty());
		assertFalse(proxy.equals(Boolean.TRUE));
	}

	@Test
	public void testNotEqualsProxyDiffChunks()
	{
		ChunkSerializationProxy a = new ChunkSerializationProxy(Chunks.fromISOLatin1("a"));
		ChunkSerializationProxy b = new ChunkSerializationProxy(Chunks.fromISOLatin1("b"));
		assertFalse(a.equals(b));
	}
}
