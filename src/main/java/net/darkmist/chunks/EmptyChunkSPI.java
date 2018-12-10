package net.darkmist.chunks;

import java.nio.ByteOrder;

import com.google.errorprone.annotations.Immutable;

@Immutable
enum EmptyChunkSPI implements ChunkSPI
{
	EMPTY;

	final transient Chunk chunk;

	private EmptyChunkSPI()
	{
		this.chunk = Chunk.instance(this);
	}

	Chunk getChunk()
	{
		return chunk;
	}

	@Override
	public int getByte(long off)
	{
		throw new IndexOutOfBoundsException("Empty chunk does not have byte at offset " + off + '.');
	}

	@Override
	public short getShort(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException("Empty chunk does not have short at offset " + off + '.');
	}

	@Override
	public int getInt(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException("Empty chunk does not have int at offset " + off + '.');
	}

	@Override
	public long getLong(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException("Empty chunk does not have long at offset " + off + '.');
	}

	@Override
	public long getSize()
	{
		return 0l;
	}

	@Override
	public boolean isCoalesced()
	{
		return true;
	}

	// null translated to this Chunk
	@Override
	public Chunk coalesce()
	{
		return null;
	}

	// null translated to this Chunk
	@Override
	public Chunk subChunk(long off, long len)
	{
		if(off==0l && len==0l)
			return null;
		throw new IndexOutOfBoundsException("Empty chunk cannot be subchunked with offset " + off + " and length " + len + '.');
	}

	@Override
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		// NOTE: arrayOff <= bytes.length because we're copying nothing.
		if(chunkOff==0 && len==0 && 0<= arrayOff && arrayOff <= bytes.length)
			return bytes;
		throw new IndexOutOfBoundsException("Empty chunk cannot be copied with offset " + chunkOff + " and lenth " + len + '.');
	}

	// Use enum implementations of equals, hashCode & toString
}
