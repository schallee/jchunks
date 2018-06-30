package net.darkmist.chunks;

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
	public byte getByte(int off)
	{
		throw new IndexOutOfBoundsException();
	}

	@Override
	public long getSizeLong()
	{
		return 0l;
	}

	@Override
	public boolean isSizeLong()
	{
		return true;
	}

	@Override
	public int getSizeInt()
	{
		return 0;
	}

	@Override
	public boolean isSizeInt()
	{
		return true;
	}

	@Override
	public boolean isCoalesced()
	{
		return true;
	}

	@Override
	public Chunk coalesce()
	{
		return null;
	}

	@Override
	public Chunk subChunk(long off, long len)
	{
		if(off==0l && len==0l)
			return null;
		throw new IndexOutOfBoundsException();
	}

	@Override
	public Chunk subChunk(int off, int len)
	{
		if(off==0 && len==0)
			return null;
		throw new IndexOutOfBoundsException();
	}
}
