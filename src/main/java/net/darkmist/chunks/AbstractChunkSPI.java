package net.darkmist.chunks;

import java.nio.ByteOrder;

/**
 * Abstract class to simplifty implemenation of {@link ChunkSPI}.
 */
public abstract class AbstractChunkSPI implements ChunkSPI
{
	protected final transient long size;

	protected AbstractChunkSPI(long size)
	{
		if(size<0)
			throw new NegativeArraySizeException();
		this.size = size;
	}

	protected final long requireValidOffset(long off)
	{
		if(off<0||size<=off)
			throw new IndexOutOfBoundsException();
		return off;
	}

	/*
	protected final int requireValidOffset(int off)
	{
		if(off<0)
			throw new IndexOutOfBoundsException();
		if(isSizeInt && sizeInt <= off)
			throw new IndexOutOfBoundsException();
		return off;
	}
	
	protected final int requirePositive(int i)
	{
		if(i<0)
			throw new IndexOutOfBoundsException();
		return i;
	}

	protected final long requirePositive(long l)
	{
		if(l<0)
			throw new IndexOutOfBoundsException();
		return l;
	}

	protected final int validEndForOffAndLen(int off, int len)
	{
		int end = Math.addExact(requireValidOffset(off),requirePositive(len));
		if(sizeInt < end)	// == is valid here
			throw new IndexOutOfBoundsException();
		return end;
	}

	protected final long validEndForOffAndLen(long off, long len)
	{
		long end = Math.addExact(requireValidOffset(off),requirePositive(len));
		if(sizeLong < end)	// == is valid here
			throw new IndexOutOfBoundsException();
		return end;
	}
	*/

	/**
	 * @note One of <code>getByte(long)</code> or {@link #getByte(int)} must be overridden.
	 * @return Result of getByte(int) if <code>off</code> is less then or eqaul to {@link Integer.MAX_VALUE}.
	 * @throws IndexOutOfBoundsException if <code>off</code> is greater than <code>Integer.MAX_VALUE</code>.
	 */
	@Override
	public abstract int getByte(long off);

	@Override
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		return Util.shortFromBytes(a,b,order);
	}

	@Override
	public int getInt(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off);
		int d = getByte(off+1);
		return Util.intFromBytes(a,b,c,d,order);
	}

	@Override
	public long getLong(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off);
		int d = getByte(off+1);
		int e = getByte(off);
		int f = getByte(off+1);
		int g = getByte(off);
		int h = getByte(off+1);
		return Util.longFromBytes(a,b,c,d,e,f,g,h,order);
	}

	/**
	 * One of {@link #size()} or <code>getSizeLong()</code> must be overridden.
	 * @return Result of <code>size()</code>.
	 */
	@Override
	public final long getSize()
	{
		return size;
	}

	/**
	 * @return false
	 */
	@Override
	public boolean isCoalesced()
	{
		return false;
	}

	/**
	 * @return null which is translated to this
	 */
	@Override
	public Chunk coalesce()
	{
		byte[] bytes;

		if(isCoalesced())
			return null;	// this
		// FIXME: we could do minimum multi-chunk
		if(size>Integer.MAX_VALUE)
			return null;	//this
		bytes = new byte[(int)size];
		copyTo(bytes, 0l, 0, (int)size);
		return Chunks.give(bytes);
	}

	/**
	 * Default implementation defers to {@link #subChunk(int,int)} if arguments fit in ints. Otherwise it returns <code>null</code>.
	 */
	@Override
	public abstract Chunk subChunk(long off, long len);

	@Override
	@SuppressWarnings("PMD.AvoidReassigningParameters")
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		int end = Math.addExact(arrayOff, len);
		for(;chunkOff<size&&arrayOff<end;chunkOff++,arrayOff++)
			bytes[arrayOff] = (byte)getByte(chunkOff);
		return bytes;
	}
}

