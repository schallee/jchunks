package net.darkmist.chunks;

import java.nio.ByteOrder;

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
	public byte getByte(long off)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public short getShort(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public int getInt(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public long getLong(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException();
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
		throw new IndexOutOfBoundsException();
	}

	@Override
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		// NOTE: arrayOff <= bytes.length because we're copying nothing.
		if(chunkOff==0 && len==0 && 0<= arrayOff && arrayOff <= bytes.length)
			return bytes;
		throw new IndexOutOfBoundsException();
	}
}
