/*
The MIT License (MIT)

Copyright (c) 2016 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
package com.github.lindenb.jsvelocity.json;

import java.io.IOException;
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public class JSMap extends AbstractMap<String,JSNode>
	implements JSNode{
	private final JSNode parent;
	private final Map<String,JSNode> map ;
	private final String id = "a" + JSUtils.nextId();
	JSMap(final JSNode parent,final JsonObject object) {
		this.parent = parent;
		this.map = object.entrySet().stream().
				collect(Collectors.toMap(
						E-> E.getKey(),
						E-> JSUtils.convert(this, E.getValue())
						))				
				;
		}
	
	@Override
	public final boolean isObject() { return true;}
	
	@Override
	public Object getNodeValue() {
		return this.map;
	}

	@Override
	public String getNodeId() {
		return id;
	}


	@Override
	public JSNode getParentNode() {
		return this.parent;
	}


	@Override
	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public int size() {
		return this.map.size();
	}
	
	@Override
	public String toString() {
		return toJson();
		}
	
	@Override
	public JSNode remove(final Object key) {
		throw new UnsupportedOperationException("Cannot remove");
	}
	
	@Override
	public Set<java.util.Map.Entry<String, JSNode>> entrySet() {
		return this.map.entrySet();
	}

	public java.util.List<KeyValue> elements() {
		final java.util.List<KeyValue> L = new java.util.ArrayList<KeyValue>(size());
		int i=0;
		for(final String k:this.map.keySet()) {
			L.add(new KeyValue(i++,k,this.map.get(k)));
			}
		return L;
	}

	@Override
	public void write(final JsonWriter writer) throws IOException {
		writer.beginObject();
		for(final String k:this.map.keySet()) {
			writer.name(k);
			this.map.get(k).write(writer);
		}
		writer.endObject();
	}
	
	
	public class KeyValue
		{
		private final int index;
		private final String key;
		private final JSNode value;
		
		public KeyValue(int index,String key,JSNode value) { this.index=index;this.key=key;this.value=value;}
		public int getIndex() { return index;}
		public boolean isFirst() { return index==0;}
		public boolean isLast() { return index+1>=JSMap.this.size();}
		public String getKey() { return key;}
		public JSNode getValue() { return value;}
		
		@Override
		public String toString() {
			JsonWriter w;
			final StringWriter sw=new StringWriter();
			try {
				w = new JsonWriter(sw);
				w.beginObject();
				w.name(this.key);
				value.write(w);
				w.endObject();
				w.close();
				return sw.toString();
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
			}
		}

	


}
