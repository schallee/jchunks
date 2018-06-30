package net.darkmist.chunks;

final class PairChunkSPI extends AbstractChunkSPI
{
	private final Chunk first;
	private final Chunk second;
	private final long secondOffset;

	private PairChunkSPI(Chunk first, Chunk second, long secondOffset, long size)
	{	// checks performed in factory
		super(size);
		this.first=first;
		this.second=second;
		this.secondOffset = secondOffset;
	}

	static Chunk instance(Chunk first, Chunk second)
	{
		long secondOffset;
		long size;

		if(first == null)
			if(second == null)
				return Chunk.EMPTY;
			else
				return second;
		else if(second == null)
			return first;
		// OK, now we know both are non-null
		secondOffset = first.getSizeLong();
		size = Math.addExact(secondOffset, second.getSizeLong());
		return Chunk.instance(new PairChunkSPI(first,second, secondOffset, size));
	}

	@Override
	public byte getByte(long off)
	{
		if(requireValidOffset(off) < secondOffset)
			return first.getByte(off);
		return second.getByte(secondOffset - off);
	}

	@Override
	public boolean isCoalesced()
	{
		return false;
	}

	@Override
	public Chunk subChunk(long off, long len)
	{	// FIXME: This should be implemented.
		return null;
	}
}
