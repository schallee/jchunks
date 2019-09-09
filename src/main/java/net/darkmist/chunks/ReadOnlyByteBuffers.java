package net.darkmist.chunks;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

final class ReadOnlyByteBuffers
{
	private static final boolean BUFFER_ALLOCATE = true;
	//private static final Logger logger = LoggerFactory.getLogger(ReadOnlyByteBuffers.class);

	/**
	 * Empty buffer. This does not need to be duplicated as nothing can be set outside of the range of 0-0.
	 */
	static final ByteBuffer EMPTY = ByteBuffer.allocate(0).asReadOnlyBuffer();
	// private static final ByteBuffer BYTES = mkBytes();

	private ReadOnlyByteBuffers()
	{
	}

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
		if(BUFFER_ALLOCATE)
			return flip(ByteBuffer.allocate(len).put(buf.duplicate())).asReadOnlyBuffer();

		byte bytes[] = new byte[len];
		buf.duplicate().get(bytes);
		return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
	}

	static ByteBuffer copy(byte[] array)
	{
		int len;

		if(array==null)
			return EMPTY;
		len = array.length;
		if(len==0)
			return EMPTY;
		/* Chunks wrapper should handle this and the empty case.
		if(len==1)
			return singleByte(array[0]);
		*/
		if(BUFFER_ALLOCATE)
			return flip(ByteBuffer.allocate(len).put(array)).asReadOnlyBuffer();
		return ByteBuffer.wrap(Arrays.copyOf(array,len)).asReadOnlyBuffer();
	}

	private static <T extends Buffer> T flip(T buf)
	{
		buf.flip();
		return buf;
	}

	// package for testing
	static ByteBuffer asReadOnlyOrSelf(ByteBuffer buf)
	{
		if(buf.isReadOnly())
			return buf;
		return buf.asReadOnlyBuffer();
	}

	// this is separate and package purely for testing
	private static ByteBuffer unslicedRangeNoArgCheckRW(final ByteBuffer origBuf, int off, int end)
	{
		ByteBuffer buf;
		int pos;
		int newPos;
		// int limit;
		int newLimit;

		// The byte buffer could overflow internally, so be exact.

		buf = origBuf.duplicate();
		pos = buf.position();
		// limit = buf.limit();

		newPos = Math.addExact(pos, off);
		newLimit = Math.addExact(pos,end);

		buf.position(newPos);
		buf.limit(newLimit);
		/*
		if(DEEP_DEBUG && logger.isDebugEnabled())
		{
			logger.debug("");
			logger.debug("\t\t\tunslicedRangeNoArgCheck(buf,off={},end={})", off, end);
			logger.debug("\t\t\t\tbuf.pos={} buf.limit={}", pos, limit);
			logger.debug("\t\t\t\tnew.pos={} new.limit={}", newPos, newLimit);
		}
		*/

		return buf;
	}

	static ByteBuffer unslicedRangeNoArgCheck(ByteBuffer buf, int off, int end)
	{
		return asReadOnlyOrSelf(unslicedRangeNoArgCheckRW(buf,off,end));
	}

	/*
	// this is separate and package purely for testing
	private static ByteBuffer unslicedSubRW(ByteBuffer buf, int off, int len)
	{
		return unslicedRangeNoArgCheckRW(
			buf,
			off,
			Util.requireValidOffLenRetEnd(buf.remaining(), off, len));
	}
	*/

	/*
	private static ByteBuffer unslicedSub(ByteBuffer buf, int off, int len)
	{
		return asReadOnlyOrSelf(unslicedSubRW(buf,off,len));
	}
	*/
}
