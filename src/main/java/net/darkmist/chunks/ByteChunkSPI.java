package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
// PMD thinks this is a bean and doesn't like not having accessors.
final class ByteChunkSPI implements ChunkSPI
{
	private static final List<Chunk> chunks = mkChunks();

	private final byte b;

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private static List<Chunk> mkChunks()
	{
		Chunk chunks[] = new Chunk[256];
		
		for(int i=0;i<0x100;i++)
			chunks[i] = Chunk.instance(new ByteChunkSPI((byte)i));
		return Collections.unmodifiableList(Arrays.asList(chunks));
	}

	static Chunk instance(byte b)
	{
		return chunks.get(b&0xff);
	}

	/**
	 * @param i Integer to convert to the single byte for the returned
	 *	chunk. The conversion is done if <code>{@link Byte.MIN_VALUE}
	 *	&lt;= i &lt;=0xff</code> by anding it with <code>0xff</code>.
	 * @return A Chunk containing a single byte.
	 * @throws IllegalArgumentException If <code>i</code> is not
	 *	between <code>Byte.MIN_VALUE</code> and <code>0xff</code>
	 * 	inclusive.
	 */
	static Chunk instance(int i)
	{
		return instance(Util.requireExtendedByteValue(i));
	}

	private ByteChunkSPI(byte b)
	{
		this.b=b;
	}

	@Override
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
		// Since when was comparing to zero a problem?
	public final byte getByte(long off)
	{
		if(off!=0l)
			throw new IndexOutOfBoundsException();
		return b;
	}

	@Override
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public final short getShort(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public final int getInt(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public final long getLong(long off, ByteOrder order)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public final long getSize()
	{
		return 1l;
	}

	// null => translated to this chunk
	@Override
	public final Chunk subChunk(long off, long len)
	{
		if(len==0 && (off==0 || off==1))
			return Chunks.empty();
		if(len==1 && off==0)
			return null;
		throw new IndexOutOfBoundsException();
	}

	@Override
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		if(chunkOff!=0)
			throw new IndexOutOfBoundsException();
		switch(len)
		{
			case 0:
				return bytes;
			case 1:
				bytes[0] = b;
				return bytes;
			default:
				throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public final boolean isCoalesced()
	{
		return true;
	}

	// null translated to Chunk
	@Override
	public final Chunk coalesce()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return String.format("%s: dec=%d oct=0%o hex=0x%x", getClass().getSimpleName(), b, b, b);
	}

	@Override
	public int hashCode()
	{
		return b;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o)
			return true;
		if(o==null)
			return false;
		if(!(o instanceof ByteChunkSPI))
			return false;
		ByteChunkSPI that = (ByteChunkSPI)o;
		return this.b == that.b;
	}
}
