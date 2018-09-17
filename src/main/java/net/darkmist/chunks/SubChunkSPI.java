package net.darkmist.chunks;

import javax.annotation.CheckReturnValue;
import javax.annotation.meta.When;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
// PMD thinks this is a bean and doesn't like not having accessors.
final class SubChunkSPI extends AbstractChunkSPI
{
	private static final Logger logger = LoggerFactory.getLogger(SubChunkSPI.class);
	private final Chunk chunk;
	private final long off;
	private final long end;

	private SubChunkSPI(Chunk chunk, long off, long len)
	{	// checks performed in factory
		super(len);
		if(logger.isDebugEnabled())
			logger.debug("chunk.getSizeLong()={} off={} len={}", chunk.getSizeLong(), off, len);
		Util.requireValidOffLen(
			requireNonNull(chunk).getSizeLong(),
			off,
			len);
		this.chunk = chunk;
		this.off = off;
		this.end = off+len;
	}

	static Chunk instance(Chunk chunk, long off, long len)
	{
		return Chunk.instance(new SubChunkSPI(chunk, off, len));
	}

	@CheckReturnValue(when=When.NEVER)
	@Override
	public byte getByte(long off)
	{
		if(off>=end)
			throw new IndexOutOfBoundsException();
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
