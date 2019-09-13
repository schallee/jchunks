package net.darkmist.chunks;

class RepeatedByteChunkSPI extends AbstractChunkSPI
{
	private final int val;

	private RepeatedByteChunkSPI(int val, long size)
	{
		super(size);
		this.val = val;
	}

	@Override
	public int getByte(long off)
	{
		requireValidOffset(off);
		return val;
	}

	@Override
	public Chunk subChunk(long off, long len)
	{
		Util.requireValidOffLen(size, off, len);
		return Chunk.instance(new RepeatedByteChunkSPI(val, len));
	}

	static Chunk instance(int val, long size)
	{
		if(size == 0)
			return Chunks.empty();
		if(size == 1)
			return Chunks.ofByte(val);
		return Chunk.instance(
			new RepeatedByteChunkSPI(
				Util.requireExtendedByteValue(val),
				Util.requirePos(size)
			)
		);
	}
}
