package net.darkmist.chunks;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD.TooManyMethods")
// Notes:
// 	No OpenOption other than StandardOpenOption.READ make any sense for us.
final class FileChunks
{
	private static final Logger logger = LoggerFactory.getLogger(FileChunks.class);
	private static final Set<OpenOption> READ_OPEN_OPTIONS = Collections.singleton(StandardOpenOption.READ);
	private static final Set<OpenOption> ALLOWED_OPEN_OPTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		StandardOpenOption.READ,
		StandardOpenOption.DELETE_ON_CLOSE,
		StandardOpenOption.READ,
		LinkOption.NOFOLLOW_LINKS
	)));

	private static int requirePosInt(long l)
	{
		if(l<0)
			throw new IllegalArgumentException("Negative value for size.");
		if(l>Integer.MAX_VALUE)
			throw new UnsupportedOperationException("Presently sizes over " + Integer.MAX_VALUE + " are presently not supported.");
		return (int)l;
	}

	private static final Set<OpenOption> combineOpts(Set<OpenOption> opts)
	{
		Set<OpenOption> ret;

		if(opts==null || opts.isEmpty())
			return READ_OPEN_OPTIONS;
		for(OpenOption opt : opts)
			if(!ALLOWED_OPEN_OPTIONS.contains(opt))
				throw new IllegalArgumentException("Unsupported OpenOption " + opt + " for read only file mappings.");
		if(opts.contains(StandardOpenOption.READ))
			return opts;
		// some set+1 extra wrapper would be nice here
		ret = new HashSet<OpenOption>(opts);
		ret.add(StandardOpenOption.READ);
		return ret;
	}

	Chunk map(FileChannel fc, int off, int len) throws IOException
	{
		return Chunk.giveInstance(fc.map(FileChannel.MapMode.READ_ONLY, off, len));	
	}

	Chunk map(FileChannel fc, int off) throws IOException
	{
		return map(fc,off,requirePosInt(fc.size()));
	}

	Chunk map(FileChannel fc) throws IOException
	{
		return map(fc, 0);
	}

	// FIXME: long off & len version?
	Chunk map(Path path, Set<OpenOption> opts, int off, int len) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			// let FileChannel figure out if the bounds make sense.
			return map(fc, off, len);
		}
	}

	Chunk map(Path path, Set<OpenOption> opts, int off) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			// let FileChannel figure out if the bounds make sense.
			return map(fc, off);
		}
	}

	Chunk map(Path path, Set<OpenOption> opts) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			// let FileChannel figure out if the bounds make sense.
			return map(fc);
		}
	}

	Chunk mapOrSlurp(Path path, Set<OpenOption> opts) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			try
			{
				Chunk.giveInstance(fc.map(FileChannel.MapMode.READ_ONLY, 0l, requirePosInt(fc.size())));	
			}
			catch(IOException e)
			{
				logger.debug("Failed to map file {}", path, e);
			}
			return slurp(fc,path);
		}
	}

	Chunk map(File file) throws IOException
	{
		return map(file.toPath());
	}

	Chunk map(Path path) throws IOException
	{
		return map(path, READ_OPEN_OPTIONS);
	}

	Chunk map(String path) throws IOException
	{
		return map(Paths.get(path));
	}

	Chunk slurp(File file) throws IOException
	{
		return slurp(file.toPath());
	}

	private static Chunk slurp(FileChannel fc, Path path) throws IOException
	{
		ByteBuffer buf;

		buf = ByteBuffer.allocate(requirePosInt(fc.size()));
		while(buf.hasRemaining())
		{
			if(fc.read(buf)<0)
				throw new IOException((path==null?"FileChannel":"File " + path) + " shrank while we were reading it (Size was " + buf.capacity() + " but we hit end of file after " + buf.position() + '.');
		}
		buf.flip();
		return Chunk.giveInstance(buf);
	}

	Chunk slurp(Path path) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, READ_OPEN_OPTIONS);
		)
		{
			return slurp(fc, path);
		}
	}

	Chunk slurp(String name) throws IOException
	{
		return slurp(Paths.get(name));
	}
}
