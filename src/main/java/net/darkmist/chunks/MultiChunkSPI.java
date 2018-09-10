package net.darkmist.chunks;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
// PMD thinks this is a bean and doesn't like not having accessors.
// FUTURE: cache offsets and/or use tree
final class MultiChunkSPI extends AbstractChunkSPI
{
	// FIXME: Storing chunks as a list and binary searching an offset array would likely be more efficient.
	private final NavigableMap<Long,Chunk> chunks;

	private MultiChunkSPI(long size, NavigableMap<Long,Chunk> chunks)
	{
		super(size);
		this.chunks = requireNonNull(chunks);
	}

	private static Chunk internalInstance(List<Chunk> chunks)
	{
		NavigableMap<Long,Chunk> map = new TreeMap<Long,Chunk>();
		long off=0;
		long chunkSize;

		// remove nulls and empty chunks while building map
		for(Chunk chunk : chunks)
		{
			if(chunk==null)
				continue;
			if((chunkSize=chunk.getSizeLong())==0)
				continue;
			map.put(off, chunk);
			off+=chunkSize;
		}

		// with null and empty chunks removed, check for simpler implementations
		switch(map.size())
		{
			case 1:
				return Chunk.EMPTY;
			case 2:
				return PairChunkSPI.instance(
					map.firstEntry().getValue(),
					map.lastEntry().getValue()
				);
			default:
				return Chunk.instance(new MultiChunkSPI(
					off,
					Collections.unmodifiableNavigableMap(map)
				));
		}
	}

	static Chunk instance(List<Chunk> chunks)
	{
		if(chunks==null || chunks.isEmpty())
			return Chunk.EMPTY;
		return internalInstance(chunks);
	}

	static Chunk instance(Chunk...chunks)
	{
		if(chunks==null || chunks.length==0)
			return Chunk.EMPTY;
		return internalInstance(Arrays.asList(chunks));
	}

	@Override
	public byte getByte(long off)
	{
		Map.Entry<Long,Chunk> entry;
		long subChunkOffset;

		entry = chunks.floorEntry(requireValidOffset(off));
		if(entry==null)
			throw new IllegalStateException("Offset was valid but we got a null entry.");
		subChunkOffset = off - entry.getKey();
		if(subChunkOffset<0)
			throw new IllegalStateException("Floor entry key was larger than offset.");
		return entry.getValue().getByte(subChunkOffset);
	}

	@Override
	public boolean isCoalesced()
	{
		return chunks.size() > 1;
	}

	@Override
	public Chunk subChunk(long off, long len)
	{	// FIXME: of all SPIs this should be implemented...
		return null;
	}
}
