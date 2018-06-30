package net.darkmist.chunks;

import java.nio.ByteBuffer;
import java.util.Arrays;
import static java.util.Objects.requireNonNull;

// FUTRE: direct vs. indirect
final class BufferChunkSPI extends AbstractChunkSPI
{
	private static final long serialVersionUID = 0l;

	private final transient ByteBuffer buf;

	private BufferChunkSPI(ByteBuffer buf)
	{
		super(requireNonNull(buf).remaining());
		this.buf=buf;
	}

	static Chunk giveInstance(ByteBuffer buf)
	{
		int len;

		if(buf==null)
			return Chunk.EMPTY;
		len = buf.remaining();
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return Chunk.byteInstance(buf.get(0));
		return Chunk.instance(new BufferChunkSPI(buf.duplicate().asReadOnlyBuffer()));
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
				return Chunk.EMPTY;
			else
				throw new NullPointerException();
		}
		Util.requireValidOffLen(array, off, len);
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return ByteChunkSPI.instance(array[off]);
		return giveInstance(ByteBuffer.wrap(array,off,len));
	}

	public static Chunk giveInstance(byte[] array)
	{
		int len;

		if(array==null)
			return Chunk.EMPTY;
		len = array.length;
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return ByteChunkSPI.instance(array[0]);
		return giveInstance(ByteBuffer.wrap(array));
	}

	static Chunk copyInstance(ByteBuffer buf)
	{
		int len;

		if(buf==null)
			return Chunk.EMPTY;
		len = buf.remaining();
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return Chunk.byteInstance(buf.get(0));
		return Chunk.instance(new BufferChunkSPI(ReadOnlyByteBuffers.copy(buf)));
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
				return Chunk.EMPTY;
			else
				throw new NullPointerException();
		}
		end = Util.requireValidOffLenRetEnd(array, off, len);
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return ByteChunkSPI.instance(array[off]);
		return giveInstance(ByteBuffer.wrap(Arrays.copyOfRange(array, off, end)));
	}

	public static Chunk copyInstance(byte[] array)
	{
		int len;

		if(array==null)
			return Chunk.EMPTY;
		len = array.length;
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return ByteChunkSPI.instance(array[0]);
		return giveInstance(ReadOnlyByteBuffers.copy(array));
	}

	@Override
	public byte getByte(int off)
	{
		return buf.get(off);
	}

	@Override
	public boolean isCoalesced()
	{
		return (buf.position()==0 && buf.limit()==buf.capacity());
	}

	@Override
	public Chunk coalesce()
	{
		if(isCoalesced())
			return null;
		return Chunk.giveInstance(ReadOnlyByteBuffers.copy(buf));
	}

	@Override
	public Chunk subChunk(long off, long len)
	{
		return subChunkToInt(off, len);
	}

	@Override
	public Chunk subChunk(int off, int len)
	{
		int end;

		if(off==0 && len==sizeInt)
			return null;	// self
		end = Util.requireValidOffLenRetEnd(sizeInt, off, len);
		if(len==0)
			return Chunk.EMPTY;
		if(len==1)
			return Chunk.byteInstance(getByte(off));
		return Chunk.giveInstance(ReadOnlyByteBuffers.unslicedRangeNoArgCheck(buf, off, end));
	}
}
