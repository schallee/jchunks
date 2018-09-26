package net.darkmist.chunks;

import java.io.InvalidObjectException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.AbstractList;

abstract class AbstractNotSerializableList<T> extends AbstractList<T>
{
	@SuppressWarnings("PMD.UnusedFormalParameter")
	private void writeObject(ObjectOutputStream oos) throws IOException
	{
		throw new NotSerializableException("Class cannot be serialized.");
	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	private void readObject(ObjectInputStream ois) throws IOException
	{
		throw new InvalidObjectException("Class cannot be serialized.");
	}

	@SuppressWarnings({ "PMD.UnusedFormalParameter", "PMD.UnusedPrivateMethod", "unused" })
	private void readObjectNoData() throws ObjectStreamException
	{
		throw new InvalidObjectException("Class cannot be serialized.");
	}
}
