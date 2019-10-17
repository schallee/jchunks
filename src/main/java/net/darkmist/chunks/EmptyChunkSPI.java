package net.darkmist.chunks;

import java.nio.ByteOrder;

import javax.annotation.Nullable;

import com.google.errorprone.annotations.Immutable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Immutable
final class EmptyChunkSPI implements ChunkSPI
{
	static final EmptyChunkSPI EMPTY_SPI = new EmptyChunkSPI();
	static final Chunk EMPTY = Chunk.instance(EMPTY_SPI);

	private EmptyChunkSPI()
	{
	}

	@Override
	public int getByte(int off)
	{
		throw new IndexOutOfBoundsException("Empty chunk does not have byte at offset " + off + '.');
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
		return 0L;
	}

	@Override
	public boolean isCoalesced()
	{
		return true;
	}

	// null translated to this Chunk
	@Nullable
	@Override
	public Chunk coalesce()
	{
		return null;
	}

	// null translated to this Chunk
	@Nullable
	@Override
	public Chunk subChunk(long off, long len)
	{
		if(off==0L && len==0L)
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

        /*--------+
         | Object |
         +--------*/

	@Override
	@SuppressFBWarnings(value="NSE_NON_SYMMETRIC_EQUALS",justification="Compiler optimization of getSize().")
	public boolean equals(Object o)
	{
		if(this==o)
			return true;
		if(!(o instanceof ChunkSPI))
			return false;
		ChunkSPI that = (ChunkSPI)o;
		return this.getSize() == that.getSize();
	}

	@Override
	public int hashCode()
	{
		return 0;
	}

	@Override
	public String toString()
	{
		return "EMPTY chunk";
	}
}
