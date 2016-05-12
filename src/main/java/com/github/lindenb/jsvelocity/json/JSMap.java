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

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
	public JSNode remove(Object key) {
		throw new UnsupportedOperationException("Cannot remove");
	}
	
	@Override
	public final boolean isObject() { return true;}
	
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
			return "("+key+"="+value+")";
			}
		}

	


}
