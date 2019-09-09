package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.function.IntFunction;

import javax.annotation.concurrent.Immutable;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Abstract class to simplify implementation of {@link ChunkSPI}.
 */
@com.google.errorprone.annotations.Immutable
@Immutable
abstract class AbstractChunkSPI implements ChunkSPI
{
	//private static final Class<AbstractChunkSPI> CLASS = AbstractChunkSPI.class;
	//private static final Logger logger = LoggerFactory.getLogger(CLASS);

	/** 
	 * Size of the chunk.
	 */
	protected final transient long size;

	protected AbstractChunkSPI(long size)
	{
		this.size = Util.requirePos(size, NegativeArraySizeException::new);
	}

	protected final long requireValidOffset(long off)
	{
		//if(logger.isDebugEnabled())
			//logger.debug("size={} off={}", size, off);
		return Util.requireValidOffset(size, off);
	}

	/**
	 * {@inheritDoc}
	 * Subclasses must implement this as other methods are implemented
	 * using it.
	 */
	@Override
	public abstract int getByte(long off);

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		return Util.shortFromBytes(a,b,order);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getInt(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		return Util.intFromBytes(a,b,c,d,order);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLong(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		int e = getByte(off+4);
		int f = getByte(off+5);
		int g = getByte(off+6);
		int h = getByte(off+7);
		return Util.longFromBytes(a,b,c,d,e,f,g,h,order);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final long getSize()
	{
		return size;
	}

	/**
	 * {@inheritDoc}
	 *
	 * The default implementation always returns false.
	 *
	 * @return false
	 */
	@Override
	public boolean isCoalesced()
	{
		return false;
	}

	/**
	 * Default coalesce implementation using specified allocator.
	 *
	 * @param allocator method used to allocate the byte array. This
	 * method should return <code>null</code> if allocation fails.
	 *
	 * If {@link #isCoalesced()} returns <code>false</code>,
	 * {@link #size} is less than or equal to {@link Integer#MAX_VALUE} and
	 * a byte array of <code>size</code> is successfully
	 * allocated using <code>allocator</code> then
	 * {@link #copyTo(byte[],long,int,int)} is used to copy the
	 * byte array is returned as a {@link Chunk} using {@link
	 * Chunks#give(byte[])}.
	 *
	 * @return If the conditions describe above are meet, a
	 * copy of this chunk in a byte array is made. Otherwise,
	 * <code>null</code> is returned which results in
	 * {@link Chunk#coalesce()} returning the <code>Chunk</code>
	 * utilizing this <code>ChunkSPI</code>.
	 */
	protected Chunk coalesce(IntFunction<byte[]> allocator)
	{
		byte[] bytes;

		if(isCoalesced())
			return null;	// this
		// FIXME: we could do minimum multi-chunk
		if(size>Integer.MAX_VALUE)
			return null;	//this
		if((bytes=allocator.apply((int)size))==null)
			return null;
		copyTo(bytes, 0l, 0, (int)size);
		return Chunks.give(bytes);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The default implementation calls
	 * {@link #coalesce(IntFunction)} with an allocator that
	 * attempts to allocate a new <code>byte[size]</code> using
	 * <code>new</code>. If this allocation fails with a
	 * {@link OutOfMemoryError} <code>null</code> is returned.
	 *
	 * @return A new <code>Chunk</code> or <code>null</code>
	 * if already coalesced, coalescing is not implemented
	 * or fails.  Returning <code>null</code> results in
	 * {@link Chunk#coalesce()} returning the <code>Chunk</code> utilizing
	 * this <code>ChunkSPI</code>.
	 */
	@Override
	public Chunk coalesce()
	{
		return coalesce(Util::guardedAllocateBytes);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract Chunk subChunk(long off, long len);

	/**
	 * {@inheritDoc}
	 *
	 * The default implementation utilizes {@link #getByte(long)}
	 * for each byte. Subclasses are encouraged to override this if
	 * a more efficient method is available.
	 *
	 */
	@Override
	@SuppressWarnings("PMD.AvoidReassigningParameters")
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		Util.requireValidOffLen(size, chunkOff, len);
		int end =Util.requireValidOffLenRetEnd(bytes.length, arrayOff, len);
		for(;arrayOff<end;chunkOff++,arrayOff++)
			bytes[arrayOff] = (byte)getByte(chunkOff);
		return bytes;
	}
}

