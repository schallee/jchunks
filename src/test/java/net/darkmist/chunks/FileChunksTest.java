package net.darkmist.chunks;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
//import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.google.errorprone.annotations.Var;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileChunksTest
{
	private static final Class<FileChunksTest> CLASS = FileChunksTest.class;
	private static final Logger logger = LoggerFactory.getLogger(CLASS);
	private static final String CLASS_NAME = CLASS.getName();
	private static final boolean OLD_SIZES = false;
	private static Path tmp_dir;
	private static final int SEQ_LEN=256;
	private static final ByteBuffer bytes = mkBytes();
	private static final long LONGEST_TO_CHECK_ZEROS = 1024 * 4;
	private static final SortedSet<Long> MAX_SIZES = mkMaxSizes();
	private static final SortedSet<Long> MIN_SIZES = mkMinSizes();
	private static final SortedSet<Long> SMALL_SIZES = mkSmallSizes();
	private static final SortedSet<Long> ALL_SIZES = mkAllSizes();

	private static ByteBuffer mkBytes()
	{
		byte[] bytes = new byte[256];

		for(int i=0;i<256;i++)
			bytes[i]=(byte)i;
		return ByteBuffer.wrap(bytes).asReadOnlyBuffer();
	}

        /*----------------------+
         | Chunk/File Size Sets |
         +----------------------*/

	private static SortedSet<Long> mkMinSizes()
	{
		SortedSet<Long> set = new TreeSet<>();

		set.add(0L);
		set.add(1L);
		set.add(256L);
		set.add(1024L);
		return Collections.unmodifiableSortedSet(set);
	}

	private static SortedSet<Long> mkAllSizes()
	{
		SortedSet<Long> set = new TreeSet<>();

		set.addAll(MAX_SIZES);
		set.addAll(SMALL_SIZES);
		set.addAll(MIN_SIZES);
		return Collections.unmodifiableSortedSet(set);
	}

	private static SortedSet<Long> mkSmallSizes()
	{
		SortedSet<Long> set=new TreeSet<>();

		set.add(0L);
		set.add(1L);
		set.add(Byte.MAX_VALUE-1L);
		set.add(Long.valueOf(Byte.MAX_VALUE));
		set.add(Byte.MAX_VALUE+1L);
		//set.add(Short.MAX_VALUE-1L);
		//set.add(Long.valueOf(Short.MAX_VALUE));
		//set.add(Short.MAX_VALUE+1L);

		return Collections.unmodifiableSortedSet(set);
	}

	private static SortedSet<Long> mkMaxSizes()
	{
		SortedSet<Long> set=new TreeSet<>();

		set.add(LargeChunksHelper.LARGE_CHUNK_SIZE-1);
		set.add(LargeChunksHelper.LARGE_CHUNK_SIZE);
		set.add(LargeChunksHelper.LARGE_CHUNK_SIZE+1);
		set.add(Integer.MAX_VALUE-1L);
		set.add(Long.valueOf(Integer.MAX_VALUE));
		set.add(Integer.MAX_VALUE+1L);

		return Collections.unmodifiableSortedSet(set);
	}

        /*---------------------------+
         | Chunk/File Size Streaming |
         +---------------------------*/

	public static Stream<Long> streamMaxSizes()
	{
		return MAX_SIZES.stream();
	}

	public static Stream<Long> streamSmallSizes()
	{
		return SMALL_SIZES.stream();
	}

	public static Stream<Long> streamMinSizes()
	{
		return MIN_SIZES.stream();
	}

	private static Path mkFileName(long size)
	{
		return tmp_dir.resolve("test." + size);
	}

        /*------------+
         | Validation |
         +------------*/

	@SuppressWarnings("UnnecessaryParentheses")
	private static void validateSequence(Chunk chunk, long chunkFileOff, @Var long chunkOff, long chunkEnd, @Var long seqOff)
	{
		if(chunkOff < 0)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is negative.");
		if(chunkEnd < 0)
			throw new IllegalArgumentException("ChunkEnd=" + chunkEnd + " is negative.");

		if(chunkOff > chunkEnd)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is less than chunkEnd=" + chunkEnd + '.');
		for(;seqOff<SEQ_LEN&&chunkOff<chunkEnd;seqOff++,chunkOff++)
			assertEquals((byte)(seqOff), (byte)(chunk.get(chunkOff)), "fileOff=" + (chunkOff+chunkFileOff) + " seqOff=" + seqOff + " chunkOff=" + chunkOff);
	}

	@SuppressWarnings("UnnecessaryParentheses")
	private static void validateZeros(Chunk chunk, long chunkFileOff, @Var long chunkOff, long chunkEnd)
	{
		if(chunkOff < 0)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is negative.");
		if(chunkEnd < 0)
			throw new IllegalArgumentException("ChunkEnd=" + chunkEnd + " is negative.");
		if(chunkOff > chunkEnd)
			throw new IllegalArgumentException("ChunkOff=" + chunkOff + " is less than chunkEnd=" + chunkEnd + '.');
		for(;chunkOff<chunkEnd;chunkOff++)
			assertEquals((byte)0, (byte)(chunk.get(chunkOff)), "fileOff=" + (chunkOff+chunkFileOff) + " chunkOff=" + chunkOff);
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

	@SuppressWarnings("ReferenceEquality")
	private static void validateChunk(Chunk chunk, long chunkLen, long chunkFileOff, long fileLen)
	{
		@Var
		long off;
		@Var
		long end;

		if(logger.isDebugEnabled())
			logger.debug("chunkLen={} chunkFileOff={} fileLen={}", chunkLen, chunkFileOff, fileLen);
		assertEquals(chunkLen, chunk.getSize());


		// Leading sequence:
		// file: starts at 0
		// file: ends at SEQ_LEN
		off = translateOff(0L,		chunkFileOff, chunkLen);
		end = translateOff(SEQ_LEN,	chunkFileOff,chunkLen);
		if(logger.isDebugEnabled())
			logger.debug("initial sequence check: off={} end={} len={}", off, end, end - off);
		validateSequence(chunk, chunkFileOff, off, end, chunkFileOff);

		if(fileLen<=512)
		{
			off = translateOff(SEQ_LEN,	chunkFileOff, chunkLen);
			end = translateOff(fileLen,	chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("final sequence check: off={} end={} len={}", off, end, end - off);
			validateSequence(chunk, chunkFileOff, off, end, 0L);
			return;
		}

		if(chunkLen <= LONGEST_TO_CHECK_ZEROS)
		{
			// Zeros:
			// file: starts at SEQ_LEN
			// file: ends at fileLen-SEQ_LEN
			off = translateOff(SEQ_LEN,		chunkFileOff, chunkLen);
			end = translateOff(fileLen-SEQ_LEN,	chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("zero check: off={} end={} len={}", off, end, end - off);
			validateZeros(chunk, chunkFileOff, off, end);
		}
		else
		{
			off = translateOff(SEQ_LEN,			chunkFileOff, chunkLen);
			end = translateOff(SEQ_LEN+SEQ_LEN,		chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("first zero check: off={} end={} len={}", off, end, end - off);
			validateZeros(chunk, chunkFileOff, off, end);
			
			off = translateOff(fileLen-SEQ_LEN-SEQ_LEN,	chunkFileOff, chunkLen);
			end = translateOff(fileLen-SEQ_LEN,		chunkFileOff, chunkLen);
			if(logger.isDebugEnabled())
				logger.debug("second zero check: off={} end={} len={}", off, end, end - off);
			validateZeros(chunk, chunkFileOff, off, end);
		}

		// Trailing zequence:
		// file: starts at fileSize-SEQ_LEN
		// file: ends at fileLen
		off = translateOff(fileLen-SEQ_LEN,	chunkFileOff, chunkLen);
		end = translateOff(fileLen,		chunkFileOff, chunkLen);
		if(logger.isDebugEnabled())
			logger.debug("final sequence check: off={} end={} len={}", off, end, end - off);
		validateSequence(chunk, chunkFileOff, off, end, 0L);

		// Lastly, we need to check some chunks with MultiChunks coalese setting:
		//if(chunk.size()>LargeChunksHelper.LARGE_CHUNK_SIZE)
		if(chunk.getSize()>Integer.MAX_VALUE)
		{
			Chunk chunkity;
			assertTrue(chunk.isCoalesced(), ()->String.format("Chunk of size %d ans SPI %s, which is more than %d, is not coalesced.", chunk.size(), chunk.getSPI(), LargeChunksHelper.LARGE_CHUNK_SIZE));
			chunkity = chunk.coalesce();
			assertTrue(chunk==chunkity);
			assertEquals(Integer.MAX_VALUE, chunk.size());
		}
	}

        /*----------------------+
         | Before And After All |
         +----------------------*/

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

	@BeforeAll
	public static void mkTestFiles() throws IOException
	{
		tmp_dir = Files.createTempDirectory("." + CLASS_NAME);
		logger.info("tmp_dir={}", tmp_dir);
		for(long size : ALL_SIZES)
			mkFile(size);
		if(logger.isInfoEnabled())
			logger.info("created {} files", ALL_SIZES.size());
	}

	@AfterAll
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

        /*-------+
         | Tests |
         +-------*/

	@Deprecated
	@ParameterizedTest
	@MethodSource("streamMaxSizes")
	public void testSlurpFunction(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.slurpFunction().apply(path);
		validateChunk(chunk, size, 0L, size);
	}

	@ParameterizedTest
	@MethodSource("streamMaxSizes")
	public void testSlurpOff1(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(size < 1)
			return;
		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.slurp(path, 1);
		validateChunk(chunk, size-1, 1L, size);
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testSlurpLenNeg1(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(size < 1)
			return;
		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.slurp(path, 0L, size-1);
		validateChunk(chunk, size-1, 0L, size);
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testSlurpOff1LenNeg2(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(size < 2)
			return;
		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.slurp(path, 1L, size-2);
		validateChunk(chunk, size-2, 1L, size);
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testMapFileChannel(long size) throws IOException
	{
		Chunk chunk;
		Path path = mkFileName(size);

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		try
		(
			FileChannel fc = FileChannel.open(path, EnumSet.of(StandardOpenOption.READ));
		)
		{
			chunk = FileChunks.map(fc);
			validateChunk(chunk, size, 0L, size);
		}
	}

	@Deprecated
	@ParameterizedTest
	@MethodSource("streamMinSizes")
	public void testMapFunc(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.mapFunction().apply(path);
		validateChunk(chunk, size, 0L, size);
	}

	@ParameterizedTest
	@MethodSource("streamMaxSizes")
	public void testMapOff1(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(size < 1)
			return;
		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.map(path, 1);
		validateChunk(chunk, size-1, 1L, size);
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testMapLenNeg1(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(size < 1)
			return;
		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.map(path, 0L, size-1);
		validateChunk(chunk, size-1, 0L, size);
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testMapOff1LenNeg2(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(size < 2)
			return;
		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.map(path, 1L, size-2);
		validateChunk(chunk, size-2, 1L, size);
	}

	@ParameterizedTest
	@MethodSource("streamMaxSizes")
	public void testMapOrSlurp(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.mapOrSlurpFunction().apply(path);
		validateChunk(chunk, size, 0L, size);
	}

	// FIXME: how many sizes do we really need to try here?
	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testMapOrSlurpOff0Len0(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		chunk = FileChunks.mapOrSlurp(path, 0L, 0L);
		assertEquals(Chunks.empty(), chunk);
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testMapOrSlurpOff1LenSize(long size) throws IOException
	{
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		assertThrows(IllegalArgumentException.class, ()->FileChunks.mapOrSlurp(path, 1L, size));
	}

	@Deprecated
	@Test
	public void testMapFunctionNonExistant()
	{
		Path notThere = mkFileName(Long.MAX_VALUE);

		assertThrows(UncheckedIOException.class, ()->FileChunks.mapFunction().apply(notThere));
	}

	private static class FileChannelWrapper extends FileChannel
	{
		private static final Method implCloseChannelMeth = TestUtil.getMethod(FileChannel.class, "implCloseChannel");
		private final FileChannel target;

		FileChannelWrapper(FileChannel target)
		{
			this.target = Objects.requireNonNull(target);
		}

		@Override
		public int read(ByteBuffer buf) throws IOException
		{
			return target.read(buf);
		}

		@Override
		public long read(ByteBuffer[] bufs, int off, int len) throws IOException
		{
			return target.read(bufs, off, len);
		}

		@Override
		public int read(ByteBuffer buf, long l1) throws IOException
		{
			return target.read(buf, l1);
		}

		@Override
		public int write(ByteBuffer buf) throws IOException
		{
			return target.write(buf);
		}

		@Override
		public long write(ByteBuffer[] bufs, int off, int len) throws IOException
		{
			return target.write(bufs, off, len);
		}

		@Override
		public int write(ByteBuffer buf, long l1) throws IOException
		{
			return target.write(buf, l1);
		}

		@Override
		public long position() throws IOException
		{
			return target.position();
		}

		@Override
		public FileChannel position(long pos) throws IOException
		{
			target.position(pos);
			return this;
		}

		@Override
		public long size() throws IOException
		{
			return target.size();
		}

		@Override
		public FileChannel truncate(long size) throws IOException
		{
			target.truncate(size);
			return this;
		}

		@Override
		public void force(boolean b) throws IOException
		{
			target.force(b);
		}

		@Override
		public long transferTo(long l1, long l2, WritableByteChannel chan) throws IOException
		{
			return target.transferTo(l1,l2,chan);
		}

		@Override
		public long transferFrom(ReadableByteChannel chan, long l1, long l2) throws IOException
		{
			return target.transferFrom(chan, l1, l2);
		}

		@Override
		public MappedByteBuffer map(FileChannel.MapMode mode, long off, long len) throws IOException
		{
			return target.map(mode, off, len);
		}

		@Override
		public FileLock lock(long l1, long l2, boolean b) throws IOException
		{
			return target.lock(l1,l2,b);
		}

		@Override
		public FileLock tryLock(long l1, long l2, boolean b) throws IOException
		{
			return target.tryLock(l1,l2,b);
		}

		// AbstractInterruptibleChannel:

		@Override
		protected void implCloseChannel() throws IOException
		{
			try
			{
				implCloseChannelMeth.invoke(target);
			}
			catch(IllegalAccessException e)
			{
				throw new IllegalStateException("Unable to invoke implCloseChannel on target.", e);
			}
			catch(InvocationTargetException ite)
			{
				Throwable cause = ite.getCause();

				if(cause instanceof IOException)
					throw (IOException)cause;
				if(cause instanceof RuntimeException)
					throw (RuntimeException)cause;
				if(cause instanceof Error)
					throw (Error)cause;
				throw new IllegalStateException("Unknown exception from implCloseChannel", cause);
			}
		}

		// Object

		@Override
		public String toString()
		{
			return "Testing wrapper of: " + target.toString();
		}

		@Override
		public int hashCode()
		{
			return target.hashCode();
		}

		@Override
		public boolean equals(Object o)
		{
			return target.equals(o);
		}
	}

	@ParameterizedTest
	@MethodSource("streamSmallSizes")
	public void testSlurpReadError(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		try
		(
			FileChannel fcTestWrapper = new FileChannelWrapper(
				FileChannel.open(path, Collections.singleton(StandardOpenOption.READ))
			)
			{
				@Override
				public int read(ByteBuffer buf) throws IOException
				{
					return -1;
				}
			};
		)
		{
			chunk = FileChunks.slurp(fcTestWrapper);
			if(size!=0L)
				fail("Expected exception but recieved instead chunk " + chunk);
			assertEquals(Chunks.empty(),chunk);
		}
		catch(IOException e)
		{
			logger.debug("Received expected exception.", e);
		}
	}

	@ParameterizedTest
	@MethodSource("streamMaxSizes")
	public void testMapOrSlurpUnmappable(long size) throws IOException
	{
		Chunk chunk;
		Path path;

		if(logger.isDebugEnabled())
			logger.debug("size={}", size);
		path = mkFileName(size);
		try
		(
			FileChannel fcTestWrapper = new FileChannelWrapper(
				FileChannel.open(path, Collections.singleton(StandardOpenOption.READ))
			)
			{
				@Override
				public MappedByteBuffer map(FileChannel.MapMode mode, long pos, long size) throws IOException
				{
					throw new IOException("Testing memory mapping failure.");
				}
			};
		)
		{
			chunk = FileChunks.mapOrSlurp(fcTestWrapper);
			validateChunk(chunk, size, 0L, size);
		}
	}
}
