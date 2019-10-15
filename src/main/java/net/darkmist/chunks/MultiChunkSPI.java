package net.darkmist.chunks;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

import javax.annotation.concurrent.Immutable;
import javax.annotation.Nullable;

import com.google.errorprone.annotations.Var;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// PMD thinks this is a bean and doesn't like not having accessors.
// FUTURE: cache offsets and/or use tree
// Immutability: erroprone does not like the chunks member which is not
//   modified. This might be alieviated with guaba immutable collections
//   but we're trying to keep dependencies down.
@com.google.errorprone.annotations.Immutable
@Immutable
@SuppressWarnings({"PMD.BeanMembersShouldSerialize","PMD.TooManyMethods","Immutable"})
final class MultiChunkSPI extends AbstractChunkSPI
{
	//private static final Class<MultiChunkSPI> CLASS = MultiChunkSPI.class;
	private static final byte[] CROSSES_CHUNK_BOUNDRIES = new byte[0];

	// FIXME: Storing chunks as a list and binary searching an offset array would likely be more efficient.
	private final NavigableMap<Long,Chunk> chunks;

	private MultiChunkSPI(long size, NavigableMap<Long,Chunk> chunks)
	{
		super(size);
		this.chunks = Collections.unmodifiableNavigableMap(requireNonNull(chunks));
	}

	private static Chunk internalInstance(List<Chunk> chunks)
	{
		NavigableMap<Long,Chunk> map = new TreeMap<>();
		@Var
		long off=0L;

		// remove nulls and empty chunks while building map
		for(Chunk chunk : chunks)
		{
			long chunkSize; 

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
			case 0:
				return Chunks.empty();
			case 1:
				return map.firstEntry().getValue();
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
		subChunkOffset = off - entry.getKey();
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
	public byte[] copyTo(byte[] bytes, long chunkOff, int arrayOff, int len)
	{
		byte[] ret;

		requireNonNull(bytes);

		// validate sizes, offsets, etc
		Util.requireValidOffLen(bytes, arrayOff, len);
		Util.requireValidOffLen(size,chunkOff,len);

		if(len==0)
			return bytes;
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
		long chunkSize = chunk.getSize();
		long offInChunk = off - chunkOff;
		long lenInChunk = chunkSize - offInChunk;

		return chunk.subChunk(offInChunk, lenInChunk);
	}

	private static Chunk subChunkEntryLen(Map.Entry<Long,Chunk> entry, long subChunkOff, long subChunkLen)
	{
		long chunkOff = entry.getKey();
		Chunk chunk = entry.getValue();
		long subChunkEnd = subChunkOff + subChunkLen;
		long lenInChunk = subChunkEnd - chunkOff;

		return chunk.subChunk(0L, lenInChunk);
	}

	@Nullable
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

		// Empty case and validation
		if(off==0 && len==size)
			return null;	// use our selves
		end = Util.requireValidOffLenRetEnd(size, off, len);
		if(len==0)
			return Chunks.empty();
		if(len==1)
			return Chunks.ofByte(getByte(off));

		// Figure out the first chunk
		firstEntry = chunks.floorEntry(off);

		// Check for trivial casae:
		if(firstEntry.getValue().getSize() >= end - firstEntry.getKey())
			return firstEntry.getValue().subChunk(off - firstEntry.getKey(), len);

		// Figure out last chunk
		lastEntry = chunks.lowerEntry(end);

		// first firstChunk, lastChunk and any chunks in between:
		firstChunk = subChunkEntryOff(firstEntry, off);
		lastChunk = subChunkEntryLen(lastEntry, off, len);
		subMap = chunks.subMap(firstEntry.getKey(), false, lastEntry.getKey(), false);

		// Build our sub chunk list
		subChunks = new ArrayList<>(subMap.size() + 2);
		subChunks.add(firstChunk);
		if(!subMap.isEmpty())
			subChunks.addAll(subMap.values());
		subChunks.add(lastChunk);

		return internalInstance(subChunks);
	}

	@Override
	public boolean isCoalesced()
	{
		// If we're longer than our LARGE_CHUNK_SIZE we'll claim to be coalesced. In theory wec ould make it all the way to Integer.MAX_VALUE.
		return size>LargeChunksHelper.LARGE_CHUNK_SIZE;
	}

	@Nullable
	Chunk testableCoalesce(IntFunction<byte[]> allocator)
	{
		byte[] bytes;
		@Var
		int off=0;

		// FIXME: do this better.
		if(size>LargeChunksHelper.LARGE_CHUNK_SIZE)	// 1G
			return null;
		// we know we're less then MAX_INT here.
		if((bytes = allocator.apply((int)size))==null)
			return null;
		for(Chunk chunk : chunks.values())
		{
			int chunkSize = chunk.size();
			chunk.copyTo(bytes, 0L, off, chunkSize);
			off+=chunkSize;
		}
		return Chunks.giveBytes(bytes);
	}

	/**
	 * @return null which is translated to this
	 */
	@Nullable
	@Override
	public Chunk coalesce()
	{
		return testableCoalesce(Util::guardedAllocateBytes);
	}

        /*--------+
         | Object |
         +--------*/

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " containing " + chunks.size() + " chunks.";
	}
}
