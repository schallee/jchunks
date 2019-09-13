package net.darkmist.chunks;

import java.io.Externalizable;
import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.Objects;

final class ChunkSerializationProxy implements Externalizable
{
	private static final Class<ChunkSerializationProxy> CLASS = ChunkSerializationProxy.class;
	private static final long serialVersionUID = 1l;
	private transient Chunk chunk; 

	public ChunkSerializationProxy(Chunk chunk)
	{
		this.chunk = chunk;
	}

	public ChunkSerializationProxy()
	{
		this(null);
	}

	@Override
	@SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
		// I really do want to check for 1
	public void readExternal(ObjectInput oi) throws IOException
	{
		long len;
		byte[] bytes;
		len = oi.readLong();
		if(len < 0)
			throw new InvalidObjectException("Length was negative while deserializaing chunk.");
		if(len==0)
		{
			this.chunk = Chunks.empty();
			return;
		}
		if(len==1)
		{
			byte b;

			b=oi.readByte();
			this.chunk=Chunks.ofByte(b);
			return;
		}
		// FIXME: handle lengths larger than Integer.MAX_VALUE
		if(len > Integer.MAX_VALUE)
			throw new UnsupportedOperationException("Deserializaing of Chunks larger than Integer.MAX_VALUE is not yet supported.");
		bytes = new byte[(int)len];
		oi.readFully(bytes);
		this.chunk = Chunks.giveBytes(bytes);
	}

	// FIXME: and how inefficient can we be?
	@Override
	public void writeExternal(ObjectOutput oo) throws IOException
	{
		long len = chunk.getSize();

		oo.writeLong(len);
		for(long pos=0;pos<len;pos++)
			oo.write(chunk.getByte(pos));
	}

	// Non-private for testing.
	Object readResolve() throws ObjectStreamException
	{
		if(chunk==null)
			throw new InvalidObjectException("Deserialization of object did not result in chunk being set.");
		return chunk;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o)
			return true;
		if(o==null)
			return false;
		if(!(o instanceof ChunkSerializationProxy))
			return false;
		ChunkSerializationProxy that = (ChunkSerializationProxy)o;
		return Objects.equals(this.chunk, that.chunk);
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(chunk);
	}

	@Override
	public String toString()
	{
		return CLASS.getSimpleName() + ": chunk=" + Objects.toString(chunk);
	}
}
