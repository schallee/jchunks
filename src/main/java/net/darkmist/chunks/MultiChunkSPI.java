package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.BeanMembersShouldSerialize")
// PMD thinks this is a bean and doesn't like not having accessors.
// FUTURE: cache offsets and/or use tree
final class MultiChunkSPI extends AbstractChunkSPI
{
	private static final Logger logger = LoggerFactory.getLogger(MultiChunkSPI.class);
	private static final byte[] CROSSES_CHUNK_BOUNDRIES = new byte[0];
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
			if((chunkSize=chunk.getSize())==0)
				continue;
			map.put(off, chunk);
			off+=chunkSize;
		}

		// with null and empty chunks removed, check for simpler implementations
		switch(map.size())
		{
			case 1:
				return Chunks.empty();
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
			return Chunks.empty();
		return internalInstance(chunks);
	}

	static Chunk instance(Chunk...chunks)
	{
		if(chunks==null || chunks.length==0)
			return Chunks.empty();
		return internalInstance(Arrays.asList(chunks));
	}

	private <T> T applyToSubChunk(long off, BiFunction<Long,Chunk,T> func)
	{
		Map.Entry<Long,Chunk> entry;
		long  subChunkOffset;

		entry = chunks.floorEntry(requireValidOffset(off));
		if(entry==null)
			throw new IllegalStateException("Offset was valid but we got a null entry.");
		subChunkOffset = off - entry.getKey();
		if(subChunkOffset<0)
			throw new IllegalStateException("Floor entry key was larger than offset.");
		return func.apply(subChunkOffset, entry.getValue());
	}

	@Override
	public int getByte(long off)
	{
		return applyToSubChunk(off, (subOff,chunk)->{return chunk.getByte(subOff);});
	}

	@Override
	@SuppressWarnings("PMD.AvoidUsingShortType")
	public short getShort(long off, ByteOrder order)
	{
		Short ret;

		ret = applyToSubChunk(off, (subOff,chunk)->
		{
			if(chunk.getSize()-off<Short.BYTES)
				return null;	// crosses subchunk boundry
			return chunk.getShort(subOff,order);
		});
		if(ret==null)
		{	// on subchunk boundry, fall back to byte based method
			return super.getShort(off, order);
		}
		return ret;
	}

	@Override
	public int getInt(long off, ByteOrder order)
	{
		Integer ret;

		ret = applyToSubChunk(off, (subOff,chunk)->
		{
			if(chunk.getSize()-off<Integer.BYTES)
				return null;	// crosses subchunk boundry
			return chunk.getInt(subOff, order);
		});
		if(ret==null)
		{	// on subchunk boundry, fall back to byte based method
			return super.getInt(off, order);
		}
		return ret;
	}

	@Override
	public long getLong(long off, ByteOrder order)
	{
		Long ret;

		ret = applyToSubChunk(off, (subOff,chunk)->
		{
			if(chunk.getSize()-off<Long.BYTES)
				return null;	// crosses subchunk boundry
			return chunk.getLong(subOff, order);
		});
		if(ret==null)
		{	// on subchunk boundry, fall back to byte based method
			return super.getLong(off, order);
		}
		return ret;
	}

	@Override
	@SuppressFBWarnings(value="RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification="validity checks")
	public byte[] copyTo(final byte[] bytes, long chunkOff, final int arrayOff, final int len)
	{
		byte[] ret;

		requireNonNull(bytes);
		requireValidOffset(chunkOff);
		if(arrayOff<0)
			throw new IndexOutOfBoundsException();
		if(arrayOff>=bytes.length)
			throw new IndexOutOfBoundsException();
		if(bytes.length < Math.addExact(arrayOff, len))
			throw new IndexOutOfBoundsException();
		ret = applyToSubChunk(chunkOff, (subOff,subChunk)->
		{
			if(subChunk.getSize()-subOff<len)
				return CROSSES_CHUNK_BOUNDRIES;	// crosses subchunk boundry
			return subChunk.copyTo(bytes, subOff, arrayOff, len);
		});
		if(ret==CROSSES_CHUNK_BOUNDRIES)
		{	// on subchunk boundry, fall back to byte based method
			return super.copyTo(bytes, chunkOff, arrayOff, len);
		}
		return ret;
	}

	private static Chunk subChunkEntryOff(Map.Entry<Long,Chunk> entry, long off)
	{
		long chunkOff = entry.getKey();
		Chunk chunk = entry.getValue();
		long offInChunk = off - chunkOff;
		long lenInChunk = chunk.getSize() - offInChunk;

		try
		{
			return chunk.subChunk(offInChunk, lenInChunk);
		}
		catch(IndexOutOfBoundsException e)
		{
			if(logger.isDebugEnabled())
				logger.debug("chunkOff={} chunk.getSize={} offInChunk={} lenInChunk={}", chunkOff, chunk.getSize(), offInChunk, lenInChunk);
			throw e;
		}
	}

	private static Chunk subChunkEntryLen(Map.Entry<Long,Chunk> entry, long len)
	{
		long chunkOff = entry.getKey();
		Chunk chunk = entry.getValue();
		long lenInChunk = len - chunkOff;

		try
		{
			return chunk.subChunk(0l, lenInChunk);
		}
		catch(IndexOutOfBoundsException e)
		{
			if(logger.isDebugEnabled())
				logger.debug("chunkOff={} chunk.getSize={} offInChunk={} lenInChunk={}", chunkOff, chunk.getSize(), 0l, lenInChunk);
			throw e;
		}
	}

	@Override
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
	public Chunk subChunk(long off, long len)
	{
		long end;
		Map.Entry<Long,Chunk> firstEntry;
		Map.Entry<Long,Chunk> lastEntry;
		NavigableMap<Long,Chunk> subMap;
		List<Chunk> subChunks;
		Chunk firstChunk;
		Chunk lastChunk;
		Chunk ret;

		// Empty case and validation
		if(off==0 && len==size)
			return null;	// use our selves
		end = Util.requireValidOffLenRetEnd(size, off, len);
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.of(getByte(off));

		// Figure out the first chunk
		firstEntry = chunks.floorEntry(off);
		if(firstEntry==null)
			throw new IllegalStateException("Offset was valid but we got a null entry.");

		// Figure out last chunk
		lastEntry = chunks.lowerEntry(end);
		if(lastEntry==null)
			throw new IllegalStateException("Offset + length was valid but we got a null entry.");

		// Check for trivial casae:
		if(firstEntry.getKey().equals(lastEntry.getKey()))
		{	// It's all in one chunk! Trivial Case!
			return firstEntry.getValue().subChunk(off - firstEntry.getKey(), len);
		}

		// first firstChunk, lastChunk and any chunks in between:
		firstChunk = subChunkEntryOff(firstEntry, off);
		lastChunk = subChunkEntryLen(lastEntry, len);	// HERE
		subMap = chunks.subMap(firstEntry.getKey(), false, lastEntry.getKey(), false);

		// Build our sub chunk list
		subChunks = new ArrayList<Chunk>(subMap.size() + 2);
		subChunks.add(firstChunk);
		for(Chunk chunk : subMap.values())
			subChunks.add(chunk);
		subChunks.add(lastChunk);

		ret = internalInstance(subChunks);
		if(logger.isDebugEnabled())
			logger.debug("subChunk: ret.size={} ret.getByte(0)={}", ret.getSize(), Integer.toHexString(ret.getByte(0l)));
		return ret;
	}

	@Override
	public boolean isCoalesced()
	{
		// If we're longer than int max we have to be multi;
		return size>Integer.MAX_VALUE;
	}

	/**
	 * @return null which is translated to this
	 */
	@Override
	@SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
	public Chunk coalesce()
	{
		// FIXME: do this better.
		if(size>Integer.MAX_VALUE)
			return null;
		return super.coalesce();
	}
}
