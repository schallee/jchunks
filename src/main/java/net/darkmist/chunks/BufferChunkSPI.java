package net.darkmist.chunks;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@com.google.errorprone.annotations.Immutable
@Immutable
@SuppressWarnings({"PMD.AvoidLiteralsInIfCondition","PMD.TooManyMethods","Immutable","UnnecessaryParentheses"})
	// We optimize on the case of size 1.
	// We handle buf carefully in an immutable way.
final class BufferChunkSPI extends ChunkIntSPI.Abstract
{
	//private static final Class<BufferChunkSPI> CLASS = BufferChunkSPI.class;
	//private static final Logger logger = LoggerFactory.getLogger(CLASS);
	private final transient ByteBuffer buf;
	private final int size;

	private BufferChunkSPI(ByteBuffer buf)
	{
		this.buf = requireNonNull(buf);
		size = buf.remaining();
	}

	static Chunk giveInstance(ByteBuffer buf)
	{
		int len;

		if(buf==null)
			return Chunks.empty();
		len = buf.remaining();
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.of(buf.get(0));
		return Chunk.instance((ChunkSPI)(new BufferChunkSPI(buf.asReadOnlyBuffer())));
	}

	/**
	 * Wrap an sub-array of bytes in a chunk.
	 * @param array to wrap
	 * @param off Offset in array of sub-array.
	 * @param len Length of sub-array.
	 * @throws NullPointerException if array is <code>null</code>
	 *	unless both <code>off</code> and <code>len</code>
	 *	are <code>0</code> in which case a empty chunk is
	 *	returned.
	 * @throws IndexOutOfBoundsException if the sub-array would be
	 *	outside the bounds of the array.
	 */
	static Chunk giveInstance(byte[] array, int off, int len)
	{
		if(array==null)
		{
			if(off==0 && len==0)
				return Chunks.empty();
			else
				throw new NullPointerException();
		}
		Util.requireValidOffLen(array, off, len);
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return ByteChunkSPI.instance(array[off]);
		return giveInstance(ByteBuffer.wrap(array,off,len));
	}

	public static Chunk giveInstance(byte[] array)
	{
		int len;

		if(array==null)
			return Chunks.empty();
		len = array.length;
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return ByteChunkSPI.instance(array[0]);
		return giveInstance(ByteBuffer.wrap(array));
	}

	static Chunk copyInstance(ByteBuffer buf)
	{
		int len;

		if(buf==null)
			return Chunks.empty();
		len = buf.remaining();
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.of(buf.get(buf.position()));
		return Chunk.instance((ChunkSPI)(new BufferChunkSPI(ReadOnlyByteBuffers.copy(buf))));
	}

	/**
	 * Copy bytes from an array and return them as a chunk.
	 * @param array The array to copy from. This may be <code>null</code> if and only if <code>off==0</code> and <code>len==0</code>.
	 * @param off Offset of the bytes into <code>array</code>.
	 * @param len Number of bytes after offset to copy.
	 * @return Chunk containing a copy of the specified bytes
	 *	from the <code>array</code>. If <code>array==null</code>,
	 *	<code>off==0</code> and <code>len==0</code> then an
	 * 	empty chunk is returned.
	 * @throws NullPointerException if <code>array</code> is null and either <code>off</code> or <code>len</code> is not zero.
	 * @throws IndexOutOfBoundsException if <code>off</code> and <code>len<code> would reference bytes not in <code>array</code>.
	 */
	static Chunk copyInstance(byte[] array, int off, int len)
	{
		int end;

		if(array==null)
		{
			if(off==0 && len==0)
				return Chunks.empty();
			else
				throw new NullPointerException();
		}
		end = Util.requireValidOffLenRetEnd(array, off, len);
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.of(array[off]);
		return giveInstance(ByteBuffer.wrap(Arrays.copyOfRange(array, off, end)));
	}

	public static Chunk copyInstance(byte[] array)
	{
		int len;

		if(array==null)
			return Chunks.empty();
		len = array.length;
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.of(array[0]);
		return giveInstance(ReadOnlyByteBuffers.copy(array));
	}

	@Override
	public int getByte(int off)
	{
		return buf.get(buf.position()+off)&0xff;
	}

	@Override
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(int off, ByteOrder order)
	{
		return Util.fromBig(buf.getShort(buf.position() + off),order);
	}

	@Override
	public int getInt(int off, ByteOrder order)
	{
		return Util.fromBig(buf.getInt(buf.position() + off),order);
	}

	@Override
	public long getLong(int off, ByteOrder order)
	{
		return Util.fromBig(buf.getLong(buf.position() + off),order);
	}

	@Override
	public long getSize()
	{
		return size;
	}

	@Override
	public boolean isCoalesced()
	{
		return buf.position()==0 && buf.limit()==buf.capacity();
	}

	@Override
	public Chunk coalesce()
	{
		if(isCoalesced())
			return null;
		return Chunks.give(ReadOnlyByteBuffers.copy(buf));
	}

	@Override
	public Chunk subChunk(int off, int len)
	{
		Chunk ret;
		int end;

		if(off==0 && len==size)
			return null;	// self
		end = Util.requireValidOffLenRetEnd(size, off, len);
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.of(getByte(off));
		ret = Chunks.give(ReadOnlyByteBuffers.unslicedRangeNoArgCheck(buf, off, end));
		return ret;
	}

	@Override
	public byte[] copyTo(byte[] bytes, int chunkOff, int arrayOff, int len)
	{
		ByteBuffer myBuf;

		Util.requireValidOffLen(bytes,arrayOff,len);
		Util.requireValidOffLen(size,chunkOff,len);
		myBuf = buf.duplicate();
		myBuf.position(myBuf.position() + chunkOff);
		myBuf.get(bytes,arrayOff,len);
		return bytes;
	}
}
