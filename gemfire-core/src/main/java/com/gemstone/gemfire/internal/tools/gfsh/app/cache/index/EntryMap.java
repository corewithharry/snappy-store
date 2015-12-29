/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal.tools.gfsh.app.cache.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gemstone.gemfire.DataSerializable;
import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.internal.cache.CachedDeserializable;

public class EntryMap extends HashMap<DataSerializable, Object> implements DataSerializable
{
	private boolean keysOnly = false;
	
	public EntryMap()
	{
		super();
	}

	public EntryMap(int size)
	{
		super(size);
	}

	public EntryMap(int size, float loadfactor)
	{
		this(size, loadfactor, false);
	}
	
	public EntryMap(int size, float loadfactor, boolean keysOnly)
	{
		super(size, loadfactor);
		this.keysOnly = keysOnly;
	} 

	public void fromData(DataInput in) throws IOException,
			ClassNotFoundException
	{
		keysOnly = in.readBoolean();
		int size = in.readInt();
		
		if (keysOnly) {
			
			for(int i=0;i<size;i++)
			{
				DataSerializable key = (DataSerializable) DataSerializer.readObject(in);
				put(key, key);
			}
			
		} else {
			for(int i=0;i<size;i++)
			{
				DataSerializable key = (DataSerializable) DataSerializer.readObject(in);
				// This will be deserialized by Java client and this will never be used in the server...
				// We can de-serialize the value object to it's base class, rather than
				// to a byte array.
				DataSerializable value = (DataSerializable) DataSerializer.readObject(in);
				put(key, value);
			}
		}
	}

	public void toData(DataOutput out) throws IOException
	{
		out.writeBoolean(keysOnly);
		out.writeInt(size());
		if (keysOnly) {
			for (Map.Entry<DataSerializable, Object> e : entrySet()) {
				DataSerializer.writeObject(e.getKey(), out);
			}
		} else {
			for (Map.Entry<DataSerializable, Object> e : entrySet()) {
				DataSerializer.writeObject(e.getKey(), out);
				out.write(((CachedDeserializable) e.getValue()).getSerializedValue());
			}
		}
	}
}