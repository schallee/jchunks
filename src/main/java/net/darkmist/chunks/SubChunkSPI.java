package net.darkmist.chunks;

import javax.annotation.CheckReturnValue;
import javax.annotation.meta.When;

import static java.util.Objects.requireNonNull;

final class SubChunkSPI extends AbstractChunkSPI
{
	private final Chunk chunk;
	private final long off;

	private SubChunkSPI(Chunk chunk, long off, long len)
	{	// checks performed in factory
		super(len);
		Util.requireValidOffLen(
			requireNonNull(chunk).getSizeLong(),
			off,
			len);
		this.chunk = chunk;
		this.off = off;
	}

	static Chunk instance(Chunk chunk, long off, long len)
	{
		return Chunk.instance(new SubChunkSPI(chunk, off, len));
	}

	@CheckReturnValue(when=When.NEVER)
	@Override
	public byte getByte(long off)
	{
		return chunk.getByte(
			Math.addExact(
				requireValidOffset(off),
				this.off)
			);
	}

	@Override
	public boolean isCoalesced()
	{
		return false;
	}

	@Override
	public Chunk subChunk(long off, long len)
	{
		return null;
	}
}
