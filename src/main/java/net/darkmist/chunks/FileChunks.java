package net.darkmist.chunks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.TooManyMethods","PMD.GodClass"})
// Notes:
// 	No OpenOption other than StandardOpenOption.READ make any sense for us.
public final class FileChunks
{
	private static final Logger logger = LoggerFactory.getLogger(FileChunks.class);
	private static final Set<OpenOption> READ_OPEN_OPTIONS = Collections.singleton(StandardOpenOption.READ);

	private FileChunks()
	{
	}

	private static long requirePos(long l, String name)
	{
		if(l<0)
			throw new IllegalArgumentException(name + " cannot be negative.");
		return l;
	}

	private static void checkFileChannelOffLen(FileChannel fc, long off, long len) throws IOException
	{
		requirePos(off, "Offset");
		requirePos(len, "Length");
		if(Math.addExact(off, len)>fc.size())
			throw new IllegalArgumentException("Offset plus length exceeds file size.");
	}

	// Private map methods:
	// --------------------

	// Final internal map method. This assumes that the arguments have already been properly checked.
	private static Chunk mapSmallPreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{	// optimize zero length here so other methods don't have to check.
		if(len==0)
			return Chunks.empty();
		return Chunks.give(fc.map(FileChannel.MapMode.READ_ONLY, off, len));	
	}
;
	private static BiFunction<Long,Long,Chunk> mapLargePreviouslyCheckedFunc(final FileChannel fc)
	{
		return (off,len)->{
			try
			{
				return mapPreviouslyChecked(fc, off, len);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}

	private static Chunk mapLargePreviouslyChecked(final FileChannel fc, long off, long len)  throws IOException
	{
		LargeChunksHelper helper = LargeChunksHelper.instance(off,len);

		return helper.readChunks(mapLargePreviouslyCheckedFunc(fc));
	}

	private static Chunk mapPreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{
		if(len > LargeChunksHelper.LARGE_CHUNK_SIZE)
			return mapLargePreviouslyChecked(fc, off, len);
		return mapSmallPreviouslyChecked(fc, off, len);
	}

	// Public map methods:
	// -------------------

	public static Chunk map(FileChannel fc, long off, long len) throws IOException
	{
		checkFileChannelOffLen(fc, off, len);
		return mapPreviouslyChecked(fc, off, len);
	}

	public static Chunk map(FileChannel fc, long off) throws IOException
	{
		return map(fc,off,Math.subtractExact(fc.size(), off));
	}

	public static Chunk map(FileChannel fc) throws IOException
	{
		return map(fc, 0);
	}

	public static Chunk map(Path path, long off, long len) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return map(fc, off, len);
		}
	}

	public static Chunk map(Path path, long off) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return map(fc, off);
		}
	}

	public static Chunk map(Path path) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return map(fc, 0l, fc.size());
		}
	}

	public static Function<Path,Chunk> mapFunction()
	{
		return (path)->
		{
			try
			{
				return map(path);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}

	// Slurp private methods:
	// ----------------------

	// Slurp a small chunk of provided length at the current file channel position.
	private static Chunk slurpSmallPreviouslyChecked(FileChannel fc, long len) throws IOException
	{
		ByteBuffer buf;

		if(len > Integer.MAX_VALUE)
			throw new IllegalStateException("Length " + len + ", which was supposidly previously checked, was larger than Integer.MAX_VALUE=" + Integer.MAX_VALUE + '.');
		buf = ByteBuffer.allocate((int)len);
		while(buf.hasRemaining())
		{
			if(fc.read(buf)<0)
				throw new IOException(fc.toString() + " shrank while we were reading it (Size was " + buf.capacity() + " but we hit end of file after " + buf.position() + '.');
		}
		buf.flip();
		return Chunks.give(buf);
	}

	private static BiFunction<Long,Long,Chunk>slurpSmallPreviouslyCheckedFunc(final FileChannel fc)
	{
		// Off is kept track of in the fc so we ignore it in our read method.
		return (off,len)->
		{
			try
			{
				return slurpSmallPreviouslyChecked(fc,len);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}

	private static Chunk slurpSmallPreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{
		if(len==0)
			return Chunks.empty();
		if(off>0)
			fc.position(off);
		return slurpSmallPreviouslyChecked(fc, (int)len);
	}

	private static Chunk slurpLargePreviouslyChecked(FileChannel fc, long off, long len)  throws IOException
	{
		LargeChunksHelper helper = LargeChunksHelper.instance(off,len);

		if(off>0)
			fc.position(off);
		// Off is kept track of in the fc so we ignore it in our read method.
		return helper.readChunks(slurpSmallPreviouslyCheckedFunc(fc));
	}

	private static Chunk slurpPreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{
		if(len > LargeChunksHelper.LARGE_CHUNK_SIZE)
			return slurpLargePreviouslyChecked(fc, off, len);
		return slurpSmallPreviouslyChecked(fc, off, len);
	}

	// Public slurp:
	// -------------

	public static Chunk slurp(FileChannel fc, long off, long len) throws IOException
	{
		checkFileChannelOffLen(fc, off, len);
		return slurpPreviouslyChecked(fc, off, len);
	}

	public static Chunk slurp(Path path, long off, long len) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return slurp(fc, off, len);
		}
	}

	public static Chunk slurp(Path path, long off) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return slurp(fc, off, Math.subtractExact(fc.size(), off));
		}
	}

	public static Chunk slurp(Path path) throws IOException
	{
		return slurp(path,0l);
	}

	// Private mapOrSlurp methods:
	// ---------------------------

	private static Chunk mapOrSlurpSmallPreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{
		try
		{
			return mapSmallPreviouslyChecked(fc, off, len);
		}
		catch(IOException e)
		{
			logger.debug("Failed to map file.", e);
		}
		return slurp(fc,off, len);
	}

	private static Chunk mapOrSlurpLargePreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{
		LargeChunksHelper helper = LargeChunksHelper.instance(off,len);

		try
		{
			return helper.readChunks(mapLargePreviouslyCheckedFunc(fc));
		}
		catch(IOException e)
		{
			logger.debug("Failed to map file.", e);
		}
		if(off>0)
			fc.position(off);
		return helper.readChunks(slurpSmallPreviouslyCheckedFunc(fc));
	}

	private static Chunk mapOrSlurpPreviouslyChecked(FileChannel fc, long off, long len) throws IOException
	{
		if(len > LargeChunksHelper.LARGE_CHUNK_SIZE)
			return mapOrSlurpLargePreviouslyChecked(fc, off, len);
		return mapOrSlurpSmallPreviouslyChecked(fc, off, len);
	}

	// Public mapOrSlurp methods:
	// --------------------------

	public static Chunk mapOrSlurp(FileChannel fc, long off, long len) throws IOException
	{
		checkFileChannelOffLen(fc, off, len);
		return mapOrSlurpPreviouslyChecked(fc, off, len);
	}

	public static Chunk mapOrSlurp(Path path, long off,  long len) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return mapOrSlurp(fc, off, len);
		}
	}

	public static Chunk mapOrSlurp(Path path, long off) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return mapOrSlurp(fc, off, Math.subtractExact(fc.size(),off));
		}
	}

	public static Chunk mapOrSlurp(Path path) throws IOException
	{
		return mapOrSlurp(path, 0l);
	}

	public static Function<Path,Chunk> mapOrSlurpFunction()
	{
		return (path)->
		{
			try
			{
				return mapOrSlurp(path);
			}
			catch(IOException e)
			{
				throw new UncheckedIOException(e);
			}
		};
	}
}
