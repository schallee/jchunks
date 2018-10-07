package net.darkmist.chunks;

import java.nio.ByteOrder;

/**
 * Abstract class to simplify implementation of {@link ChunkSPI}.
 */
public abstract class AbstractChunkSPI implements ChunkSPI
{
	//private static final Class<AbstractChunkSPI> CLASS = AbstractChunkSPI.class;
	protected final transient long size;

	protected AbstractChunkSPI(long size)
	{
		this.size = Util.requirePos(size, NegativeArraySizeException::new);
	}

	protected final long requireValidOffset(long off)
	{
		return Util.requireValidOffset(size, off);
	}

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
		int c = getByte(off+2);
		int d = getByte(off+3);
		return Util.intFromBytes(a,b,c,d,order);
	}

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
	 */
	@Override
	public abstract Chunk subChunk(long off, long len);

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

