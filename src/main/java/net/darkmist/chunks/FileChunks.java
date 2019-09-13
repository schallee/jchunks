package net.darkmist.chunks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.TooManyMethods","PMD.GodClass"})
public final class FileChunks
{
	private static final Logger logger = LoggerFactory.getLogger(FileChunks.class);
	// No OpenOption other than StandardOpenOption.READ make any sense for us.
	private static final Set<OpenOption> READ_OPEN_OPTIONS = Collections.singleton(StandardOpenOption.READ);

	private FileChunks()
	{
	}

	private static <R> R withFileChannelFor(Path path, IOEFunctional.IOEThrowingFunction<FileChannel,R> func) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return func.apply(fc);
		}
	}

	private static void checkFileChannelOffLen(FileChannel fc, long off, long len) throws IOException
	{
		Util.requirePos(off);
		Util.requirePos(len);
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
		return Chunks.giveBuffer(fc.map(FileChannel.MapMode.READ_ONLY, off, len));	
	}

	private static Chunk mapLargePreviouslyChecked(final FileChannel fc, long off, long len)  throws IOException
	{
		return LargeChunksHelper.instance(off,len).readChunks(
			(offArg,lenArg)->mapSmallPreviouslyChecked(fc,offArg,lenArg)
		);
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
		return withFileChannelFor(path, (fc)->map(fc, off, len));
	}

	public static Chunk map(Path path, long off) throws IOException
	{
		return withFileChannelFor(path, (fc)->map(fc, off));
	}

	public static Chunk map(Path path) throws IOException
	{
		return map(path, 0l);
	}

	public static Function<Path,Chunk> mapFunction()
	{
		return IOEFunctional.asFunction(FileChunks::map);
	}

	// Slurp private methods:
	// ----------------------

	// Slurp a small chunk of provided length at the current file channel position.
	private static Chunk slurpSmallPreviouslyChecked(FileChannel fc, long len) throws IOException
	{
		ByteBuffer buf;

		buf = ByteBuffer.allocate((int)len);
		while(buf.hasRemaining())
		{
			if(fc.read(buf)<0)
				throw new IOException(fc.toString() + " shrank while we were reading it (Size was " + buf.capacity() + " but we hit end of file after " + buf.position() + '.');
		}
		buf.flip();
		return Chunks.giveBuffer(buf);
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
		if(off>0)
			fc.position(off);
		// Off is kept track of in the fc so we ignore it in our read method.
		return LargeChunksHelper.instance(off,len).readChunks(
			(offArg,lenArg)->slurpSmallPreviouslyChecked(fc,lenArg)
		);
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

	public static Chunk slurp(FileChannel fc, long off) throws IOException
	{
		return slurp(fc, off, Math.subtractExact(fc.size(), off));
	}

	public static Chunk slurp(FileChannel fc) throws IOException
	{
		return slurp(fc, 0l);
	}

	public static Chunk slurp(Path path, long off, long len) throws IOException
	{
		return withFileChannelFor(path, (fc)->slurp(fc, off, len));
	}

	public static Chunk slurp(Path path, long off) throws IOException
	{
		return withFileChannelFor(path, (fc)->slurp(fc,off));
	}

	public static Chunk slurp(Path path) throws IOException
	{
		return slurp(path,0l);
	}

	public static Function<Path,Chunk> slurpFunction()
	{
		return IOEFunctional.asFunction(FileChunks::slurp);
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
			return helper.readChunks(
				(offArg,lenArg)->mapSmallPreviouslyChecked(fc,offArg,lenArg)
			);
		}
		catch(IOException e)
		{
			logger.debug("Failed to map file.", e.getCause());
		}
		// Just in case mapping changed position, always reset position here.
		fc.position(off);
		// Offset kept track of in fc position
		return helper.readChunks(
			(offArg,lenArg)->slurpSmallPreviouslyChecked(fc,lenArg)
		);
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

	public static Chunk mapOrSlurp(FileChannel fc, long off) throws IOException
	{
		return mapOrSlurp(fc, off, Math.subtractExact(fc.size(),off));
	}

	public static Chunk mapOrSlurp(FileChannel fc) throws IOException
	{
		return mapOrSlurp(fc, 0l);
	}

	public static Chunk mapOrSlurp(Path path, long off,  long len) throws IOException
	{
		return withFileChannelFor(path, (fc)->mapOrSlurp(fc, off, len));
	}

	public static Chunk mapOrSlurp(Path path, long off) throws IOException
	{
		return withFileChannelFor(path, (fc)->mapOrSlurp(fc,off));
	}

	public static Chunk mapOrSlurp(Path path) throws IOException
	{
		return mapOrSlurp(path, 0l);
	}

	public static Function<Path,Chunk> mapOrSlurpFunction()
	{
		return IOEFunctional.asFunction(FileChunks::mapOrSlurp);
	}
}
