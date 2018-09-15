package net.darkmist.chunks;

import java.nio.ByteOrder;

// FUTURE: support sizes, lengths and offsets as java.lang.Number and handle sizes larger than Long.MAX_VALUE.
public interface ChunkSPI
{
	/**
	 * Get the byte at the specified offset.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	public byte getByte(long off);

	/**
	 * Get the byte at the specified offset.
	 * @param off Offset of the byte to get.
	 * @return Value at the specified offset.
	 * @throws IndexOutOfBoundsException if <code>off</code> is negative or greater then or equal to the size.
	 */
	default public byte getByte(int off)
	{
		return getByte((long)off);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	default public short getShortBigEndian(long off)
	{
		byte a = getByte(off);
		byte b = getByte(off+1);
		return Util.shortFromBytesBigEndian(a,b);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	default public short getShortLittleEndian(long off)
	{
		byte a = getByte(off);
		byte b = getByte(off+1);
		return Util.shortFromBytesBigEndian(a,b);
	}

	@SuppressWarnings("PMD.AvoidUsingShortType")
	default public short getShort(long off, ByteOrder order)
	{
		byte a = getByte(off);
		byte b = getByte(off+1);
		return Util.shortFromBytes(a,b,order);
	}

	default public int getIntBigEndian(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		return Util.intFromBytesBigEndian(a,b,c,d);
	}

	default public int getIntLittleEndian(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		return Util.intFromBytesLittleEndian(a,b,c,d);
	}

	default public int getInt(long off, ByteOrder order)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		return Util.intFromBytes(a,b,c,d,order);
	}

	default public long getLongBigEndian(long off)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		int e = getByte(off+4);
		int f = getByte(off+5);
		int g = getByte(off+6);
		int h = getByte(off+7);
		return Util.longFromBytesBigEndian(a,b,c,d,e,f,g,h);
	}

	default public long getLongLittleEndian(long off)
	{
		int a = getByte(off);
		int b = getByte(off+1);
		int c = getByte(off+2);
		int d = getByte(off+3);
		int e = getByte(off+4);
		int f = getByte(off+5);
		int g = getByte(off+6);
		int h = getByte(off+7);
		return Util.longFromBytesLittleEndian(a,b,c,d,e,f,g,h);
	}

	default public long getLong(long off, ByteOrder order)
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
	 * Get the size as a long.
	 * @return the size of the chunk as a long.
	 */
	public long getSizeLong();

	/**
	 * @return <code>ture</code> if the size can be represented as an <code>long</code>.
	 */
	default public boolean isSizeLong()
	{
		return true;
	}

	/**
	 * Get the size as a integer.
	 * @return the size of the chunk as an integer.
	 * @throws ArithmeticException if the size of the chunk cannot be represented as an <code>int</code>.
	 */
	default public int getSizeInt()
	{
		if(isSizeInt())
			return (int)getSizeLong();
		throw new ArithmeticException("Size of chunk is larger than Integer.MAX_VALUE");
	}

	/**
	 * @return <code>ture</code> if the size can be represented as an <code>int</code>.
	 */
	default public boolean isSizeInt()
	{
		return getSizeLong() <= Integer.MAX_VALUE;
	}

	/** 
	 * @return <code>true</code> if the chunk coalesced or <code>false</code> otherwise.
	 */
	public boolean isCoalesced();

	/**
	 * @return The coalesced chunk or <code>null</code> if he chunk itself should be returned.
	 */
	public Chunk coalesce();

	/**
	 * Get a chunk from a subset of the current chunk. 
	 * @return The sub chunk, <code>null</code> if the same chunk should be returned or <code>null</code> if the default subchunking should be used.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 *
	 * FIXME: This should not be part of the base SPI. It should be handled by a capability interface.
	 *
	 */
	public Chunk subChunk(long off, long len);

	/**
	 * Get a chunk from a subset of the current chunk. 
	 * @return The sub chunk, <code>null</code> if the same chunk should be returned or <code>null</code> if the default subchunking should be used.
	 * @throws IndexOutOfBoundsException if off or length are outside the chunk.
	 */
	default public Chunk subChunk(int off, int len)
	{
		return subChunk((long)off,(long)len);
	}

}
