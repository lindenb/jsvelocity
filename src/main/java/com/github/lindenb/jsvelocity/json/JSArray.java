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
import java.util.AbstractList;
import java.util.List;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.stream.JsonWriter;

public class JSArray extends AbstractList<JSNode>
	implements JSNode, List<JSNode>{
	private final JSNode parent;
	private final List<JSNode> array ;
	private final String id = "a" + JSUtils.nextId();
	JSArray(final JSNode parent,final JsonArray garray) {
		this.parent = parent;
		this.array = StreamSupport.stream(garray.spliterator(), false).
				map(E->JSUtils.convert(this,E)).collect(
				Collectors.toList()
				);
		}
	
	@Override
	public final boolean isArray() { return true;}
	
	@Override
	public Object getNodeValue() {
		return array;
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
	public JSNode get(int index) {
		return this.array.get(index);
	}
	@Override
	public int size() {
		return this.array.size();
	}

	@Override
	public String toString() {
		return toJson();
	}


	public Iterable<Elt> elements() {
		return new EltIterable();
	}

	
	@Override
	public void write(final JsonWriter writer) throws IOException {
	writer.beginArray();
	for(final JSNode n:this.array) {
		n.write(writer);
	}
	writer.endArray();
	}
	

	public class EltIterable implements Iterable<Elt>
		{
		@Override
		public Iterator<Elt> iterator() {
			return new EltIterator();
			}
		}
	
	public class Elt
		{
		private final int index;
		
		public Elt(int index) { this.index=index;}
		public int getIndex() { return index;}
		public boolean isFirst() { return index==0;}
		public boolean isLast() { return index+1>=JSArray.this.size();}
		
		public JSNode getValue() {
			return JSArray.this.get(index);
			}
		
		@Override
		public String toString() {
			return getValue().toString();
			}
		}

	public class EltIterator implements Iterator<Elt>
		{
		private int index=-1;
		@Override
		public boolean hasNext() { return index+1<JSArray.this.size();}
		@Override
		public Elt next() { index++; return new Elt(index);}
		}
}


