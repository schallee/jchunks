package net.darkmist.chunks;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.LinkOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

// Notes:
// 	No OpenOptioisn other than StandardOpenOption.READ make any sense.
final class FileChunks
{
	private static final Set<OpenOption> READ_OPEN_OPTIONS = Collections.singleton(StandardOpenOption.READ);
	private static final Set<OpenOption> ALLOWED_OPEN_OPTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
		StandardOpenOption.READ,
		StandardOpenOption.DELETE_ON_CLOSE,
		StandardOpenOption.READ,
		LinkOption.NOFOLLOW_LINKS
	)));

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

	Chunk instance(FileChannel fc, int off, int len) throws IOException
	{
		return Chunk.giveInstance(fc.map(FileChannel.MapMode.READ_ONLY, off, len));	
	}

	Chunk instance(FileChannel fc, int off) throws IOException
	{
		return Chunk.giveInstance(fc.map(FileChannel.MapMode.READ_ONLY, off, fc.size()));	
	}

	Chunk instance(FileChannel fc) throws IOException
	{
		return Chunk.giveInstance(fc.map(FileChannel.MapMode.READ_ONLY, 0l, fc.size()));	
	}

	// FIXME: long off & len version?
	Chunk instance(Path path, Set<OpenOption> opts, int off, int len) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			// let FileChannel figure out if the bounds make sense.
			return instance(fc, off, len);
		}
	}

	Chunk instance(Path path, Set<OpenOption> opts, int off) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			// let FileChannel figure out if the bounds make sense.
			return instance(fc, off);
		}
	}

	Chunk instance(Path path, Set<OpenOption> opts) throws IOException
	{
		try
		(
			FileChannel fc = FileChannel.open(path, combineOpts(opts));
		)
		{
			// let FileChannel figure out if the bounds make sense.
			return instance(fc);
		}
	}

	Chunk instance(Path path) throws IOException
	{
		return instance(path, READ_OPEN_OPTIONS);
	}

	Chunk instance(String path) throws IOException
	{
		return instance(Paths.get(path));
	}
}
