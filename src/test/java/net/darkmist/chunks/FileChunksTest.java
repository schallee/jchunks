package net.darkmist.chunks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
//import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChunksTest
{
	private static final Class<FileChunksTest> CLASS = FileChunksTest.class;
	private static final Logger logger = LoggerFactory.getLogger(CLASS);
	private static final String CLASS_NAME = CLASS.getName();
	private static Path tmp_dir;
	private static final int seqLen=256;
	private static SortedSet<Long> sizes = mkSizes();
	private static final ByteBuffer bytes = mkBytes();
	private static final long MAX_SIZE = 1024l * 1024l * 1024l * 4l;	// 4 Gig
	//private static final long MAX_SIZE = 1024l * 1024l * 1024l;	// 1 Gig
	//private static final long MAX_SIZE = (1024l * 1024l * 1024l)/4;	// 256M
	//private static final long MAX_SIZE = 1024l * 1024l;	// 1 M
	// private static final long MAX_SIZE = 1024l;	// 1 k
	private static final long LONGEST_TO_CHECK_ZEROS = 1024 * 4;

	private static ByteBuffer mkBytes()
	{
		byte[] bytes = new byte[256];

		for(int i=0;i<256;i++)
			bytes[i]=(byte)i;
		return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
	}

	private static SortedSet<Long> mkSizes()
	{
		SortedSet<Long> set=new TreeSet<>();

		set.add(0l);
		for(long size=1;size<=MAX_SIZE;size*=2)
		{
			set.add(size-1);
			set.add(size);
			set.add(size+1);
		}
		return Collections.unmodifiableSortedSet(set);
	}

	private static Path mkFileName(long size)
	{
		return tmp_dir.resolve("test." + size);
	}

	private static void mkFile(long size) throws IOException
	{
		Path path = mkFileName(size);
		ByteBuffer ourBytes = bytes.duplicate();
		
		try
		(
			SeekableByteChannel chan = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE, StandardOpenOption.SPARSE);
		)
		{
			if(size <= 256)
			{
				ourBytes.limit((int)size);
				while(ourBytes.hasRemaining())
					chan.write(ourBytes);
				return;
			}
			if(size <= 512)
			{
				while(ourBytes.hasRemaining())
					chan.write(ourBytes);
				ourBytes.position(0);
				ourBytes.limit((int)size-256);
				while(ourBytes.hasRemaining())
					chan.write(ourBytes);
				return;
			}
			// Write one set of bytes
			while(ourBytes.hasRemaining())
				chan.write(ourBytes);
			// Skip forward till last 256
			chan.position(size-256);
			//if(logger.isDebugEnabled())
				//logger.debug("size={} size-256={} chan.position()={}", size, size-256, chan.position());
			// Write last set of bytes
			ourBytes.position(0);
			while(ourBytes.hasRemaining())
				chan.write(ourBytes);
		}
	}


	private static void validateSequence(Chunk chunk, long chunkFileOff, long chunkOff, long chunkEnd, long seqOff)
	{
		if(chunkOff < 0)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is negative.");
		if(chunkEnd < 0)
			throw new IllegalArgumentException("ChunkEnd=" + chunkEnd + " is negative.");

		if(chunkOff > chunkEnd)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is less than chunkEnd=" + chunkEnd + '.');
		for(;seqOff<seqLen&&chunkOff<chunkEnd;seqOff++,chunkOff++)
			assertEquals("fileOff=" + (chunkOff+chunkFileOff) + " seqOff=" + seqOff + " chunkOff=" + chunkOff, (byte)(seqOff), (byte)(chunk.get(chunkOff)));
	}

	private static void validateZeros(Chunk chunk, long chunkFileOff, long chunkOff, long chunkEnd)
	{
		if(chunkOff < 0)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is negative.");
		if(chunkEnd < 0)
			throw new IllegalArgumentException("ChunkEnd=" + chunkEnd + " is negative.");
		if(chunkOff > chunkEnd)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is less than chunkEnd=" + chunkEnd + '.');
		for(;chunkOff<chunkEnd;chunkOff++)
			assertEquals("fileOff=" + (chunkOff+chunkFileOff) + " chunkOff=" + chunkOff, (byte)0, (byte)(chunk.get(chunkOff)));
	}

	// fileOffset to chunkOff= fileOff - chunkFileOff
	private static long translateOff(long fileOff, long chunkFileOff, long chunkLen)
	{
		long off;

		off = fileOff-chunkFileOff;
		if(off<=0)
			return 0;
		if(off>chunkLen)
			return chunkLen;
		return off;
	}

	private static void validateChunk(Chunk chunk, long chunkLen, long chunkFileOff, long fileLen)
	{
		long off, end;

		if(logger.isDebugEnabled())
			logger.debug("chunkLen={} chunkFileOff={} fileLen={}", chunkLen, chunkFileOff, fileLen);
		assertEquals(chunkLen, chunk.getSize());


		// Leading sequence:
		// file: starts at 0
		// file: ends at seqLen
		off = translateOff(0l,		chunkFileOff, chunkLen);
		end = translateOff(seqLen,	chunkFileOff,chunkLen);
		if(logger.isDebugEnabled())
			logger.debug("initial sequence check: off={} end={} len={}", off, end, end - off);
		validateSequence(chunk, chunkFileOff, off, end, chunkFileOff);

		if(fileLen<=512)
		{
			off = translateOff(seqLen,	chunkFileOff, chunkLen);
			end = translateOff(fileLen,	chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("final sequence check: off={} end={} len={}", off, end, end - off);
			validateSequence(chunk, chunkFileOff, off, end, 0l);
			return;
		}

		if(chunkLen <= LONGEST_TO_CHECK_ZEROS)
		{
			// Zeros:
			// file: starts at seqLen
			// file: ends at fileLen-seqLen
			off = translateOff(seqLen,		chunkFileOff, chunkLen);
			end = translateOff(fileLen-seqLen,	chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("zero check: off={} end={} len={}", off, end, end - off);
			validateZeros(chunk, chunkFileOff, off, end);
		}
		else
		{
			off = translateOff(seqLen,			chunkFileOff, chunkLen);
			end = translateOff(seqLen+seqLen,		chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("first zero check: off={} end={} len={}", off, end, end - off);
			validateZeros(chunk, chunkFileOff, off, end);
			
			off = translateOff(fileLen-seqLen-seqLen,	chunkFileOff, chunkLen);
			end = translateOff(fileLen-seqLen,		chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("second zero check: off={} end={} len={}", off, end, end - off);
			validateZeros(chunk, chunkFileOff, off, end);
		}

		// Trailing zequence:
		// file: starts at fileSize-seqLen
		// file: ends at fileLen
		off = translateOff(fileLen-seqLen,	chunkFileOff, chunkLen);
		end = translateOff(fileLen,		chunkFileOff, chunkLen);
		if(logger.isDebugEnabled())
			logger.debug("final sequence check: off={} end={} len={}", off, end, end - off);
		validateSequence(chunk, chunkFileOff, off, end, 0l);
	}

	@BeforeClass
	public static void mkTestFiles() throws IOException
	{
		tmp_dir = Files.createTempDirectory(CLASS_NAME);
		logger.info("tmp_dir={}", tmp_dir);
		for(long size : sizes)
			mkFile(size);
	}

	@AfterClass
	public static void removeTestFiles() throws IOException
	{
		Files.walkFileTree(tmp_dir, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException
			{
				if(e!=null)
					throw e;
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	//@Ignore
	@Test
	public void testSlurp() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.slurp(path);
			validateChunk(chunk, size, 0l, size);
		}
	}

	//@Ignore
	@Test
	public void testSlurpOff1() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(size < 1)
				continue;
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.slurp(path, 1);
			validateChunk(chunk, size-1, 1l, size);
		}
	}

	//@Ignore
	@Test
	public void testSlurpLenNeg1() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(size < 1)
				continue;
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.slurp(path, 0l, size-1);
			validateChunk(chunk, size-1, 0l, size);
		}
	}

	//@Ignore
	@Test
	public void testSlurpLOff1enNeg2() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(size < 2)
				continue;
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.slurp(path, 1l, size-2);
			validateChunk(chunk, size-2, 1l, size);
		}
	}

	@Test
	public void testMap() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.map(path);
			validateChunk(chunk, size, 0l, size);
		}
	}

	@Test
	public void testMapOff1() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(size < 1)
				continue;
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.map(path, 1);
			validateChunk(chunk, size-1, 1l, size);
		}
	}

	@Test
	public void testMapLenNeg1() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(size < 1)
				continue;
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.map(path, 0l, size-1);
			validateChunk(chunk, size-1, 0l, size);
		}
	}

	@Test
	public void testMapLOff1enNeg2() throws IOException
	{
		Chunk chunk;
		Path path;

		for(Long size : sizes)
		{
			if(size < 2)
				continue;
			if(logger.isDebugEnabled())
				logger.debug("size={}", size);
			path = mkFileName(size);
			chunk = FileChunks.map(path, 1l, size-2);
			validateChunk(chunk, size-2, 1l, size);
		}
	}
}
