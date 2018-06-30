package net.darkmist.chunks;

import java.nio.Buffer;
import java.nio.ByteBuffer;

final class ReadOnlyByteBuffers
{
	/**
	 * Empty buffer. This does not need to be duplicated as nothing can be set outside of the range of 0-0.
	 */
	static final ByteBuffer EMPTY = ByteBuffer.allocate(0).asReadOnlyBuffer();
	// private static final ByteBuffer BYTES = mkBytes();

	private ReadOnlyByteBuffers()
	{
	}

	/*
	private static final class BytesHolder
	{
		private static final List<ByteBuffer> BYTES = mkBytes();

		private static List<ByteBuffers> mkBytes()
		{
			ByteBuffer[] buffers = new ByteBuffer[256];

			for(int i=0; i <= 0xff; i++)
				buffers[i] = ByteBuffer.allocate(1).put(0,(byte)i).asReadOnlyBuffer();
			return Collections.unmodifiableList(Arrays.asList(buffers));
		}
	}

	static ByteBuffer singleByte(byte b)
	{
		return BytesHolder.BYTES.get(b&0xff).duplicate();
	}
	*/

	/*
	private static ByteBuffer mkBytes()
	{
		ByteBuffer buf = ByteBuffer.allocate(256);

		for(int i=0; i <= 0xff; i++)
			buf.put(i,(byte)i);
		return flip(buf).asReadOnlyBuffer();
	}

	static ByteBuffer singleByte(byte b)
	{
		int i = b & 0xff;

		return limit(
			position(BYTES.duplicate(),i),
			i+1
		);
	}
	*/

	/*
	static ByteBuffer singleByte(byte b)
	{
		return ByteBuffer.allocate(1).put(0,b).asReadOnlyBuffer();
	}
	*/

	static ByteBuffer copy(ByteBuffer buf)
	{
		int len;

		if(buf==null)
			return EMPTY;
		len = buf.remaining();
		if(len==0)
			return EMPTY;
		/*
		if(len==1)
			return singleByte(buf.get(0));
		*/
		return flip(ByteBuffer.allocate(len).put(buf.duplicate())).asReadOnlyBuffer();
	}

	static ByteBuffer copy(byte[] array)
	{
		int len;

		if(array==null)
			return EMPTY;
		len = array.length;
		if(len==0)
			return EMPTY;
		/*
		if(len==1)
			return singleByte(array[0]);
		*/
		return flip(ByteBuffer.allocate(len).put(array)).asReadOnlyBuffer();
	}

	static <T extends Buffer> T flip(T buf)
	{
		buf.flip();
		return buf;
	}

	static <T extends Buffer> T position(T buf, int pos)
	{
		buf.position(pos);
		return buf;
	}

	static <T extends Buffer> T limit(T buf, int pos)
	{
		buf.limit(pos);
		return buf;
	}

	static ByteBuffer asReadOnlyOrSelf(ByteBuffer buf)
	{
		if(buf.isReadOnly())
			return buf;
		return buf.asReadOnlyBuffer();
	}

	// this is separate and package purely for testing
	static ByteBuffer unslicedRangeNoArgCheckRW(ByteBuffer buf, int off, int end)
	{
		int pos;

		// The byte buffer could overflow internally, so be exact.

		pos = buf.position();
		return limit(
				position(
					buf.duplicate(),
					Math.addExact(pos,off)
				),
				Math.addExact(pos,end)
			);
	}

	static ByteBuffer unslicedRangeNoArgCheck(ByteBuffer buf, int off, int end)
	{
		return asReadOnlyOrSelf(unslicedRangeNoArgCheckRW(buf,off,end).asReadOnlyBuffer());
	}

	// this is separate and package purely for testing
	static ByteBuffer unslicedSubRW(ByteBuffer buf, int off, int len)
	{
		return unslicedRangeNoArgCheckRW(
			buf,
			off,
			Util.requireValidOffLenRetEnd(buf.remaining(), off, len));
	}

	static ByteBuffer unslicedSub(ByteBuffer buf, int off, int len)
	{
		return asReadOnlyOrSelf(unslicedSubRW(buf,off,len));
	}
}
