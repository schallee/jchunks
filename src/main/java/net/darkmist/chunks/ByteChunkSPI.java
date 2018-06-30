package net.darkmist.chunks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("PMD.UnnecessaryFinalModifier")
final class ByteChunkSPI implements ChunkSPI
{
	private static final List<Chunk> chunks = mkChunks();

	private final byte b;

	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	private static List<Chunk> mkChunks()
	{
		Chunk chunks[] = new Chunk[256];
		
		for(int i=0;i<0x100;i++)
			chunks[i] = Chunk.instance((new ByteChunkSPI((byte)i)));
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
		if(Byte.MIN_VALUE <= i && i<=0xff)
			return instance((byte)(i&0xff));
		throw new IllegalArgumentException("Integer " + i + " cannot be represented as a byte.");
	}

	private ByteChunkSPI(byte b)
	{
		this.b=b;
	}

	@Override
	public final byte getByte(long off)
	{
		if(off!=0l)
			throw new IndexOutOfBoundsException();
		return b;
	}

	@Override
	public final byte getByte(int off)
	{
		return getByte((long)off);
	}

	@Override
	public final boolean isCoalesced()
	{
		return true;
	}

	@Override
	public final Chunk coalesce()
	{
		return null;
	}

	@Override
	public final Chunk subChunk(int off, int len)
	{
		return subChunk((long)off,(long)len);
	}

	@Override
	public final Chunk subChunk(long off, long len)
	{
		if(len==0 && (off==0 || off==1))
			return Chunk.EMPTY;
		if(len==1 && off==0)
			return null;
		throw new IndexOutOfBoundsException();
	}

	@Override
	public final long getSizeLong()
	{
		return 1l;
	}

	@Override
	public final boolean isSizeLong()
	{
		return true;
	}

	@Override
	public final int getSizeInt()
	{
		return 1;
	}

	@Override
	public final boolean isSizeInt()
	{
		return true;
	}
}
