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
import java.util.Iterator;

public class JSString  implements JSNode,CharSequence {
	private final JSNode parent;
	private final String value;
	private final String id = "s" + JSUtils.nextId();
	JSString(final JSNode parent,final String s) {
		this.parent = parent;
		this.value =s;
		}

	@Override
	public final boolean isString() { return true;}

	@Override
	public Object getNodeValue() {
		return value;
	}

	@Override
	public String getNodeId() {
		return id;
	}

	@Override
	public final boolean isString() { return true;}

	@Override
	public JSNode getParentNode() {
		return this.parent;
	}

	public boolean isEmpty() {
		return this.value.isEmpty();
	}
	
	@Override	
	public String toString() {
		return value;
		}
	
	public final int size() {
		return this.length();
	}
	
	@Override
	public int length() {
		return value.length();
	}
	
	@Override
	public char charAt(int index) {
		return value.charAt(index);
	}
	
	@Override
	public CharSequence subSequence(int start, int end) {
		return value.subSequence(start, end);
	}
	
	public Iterable<Char> characters() {
		return new CharIterable();
	}


	public class CharIterable implements Iterable<Char>
		{
		@Override
		public Iterator<Char> iterator() {
			return new CharIterator();
			}
		}
	
	public class Char
		{
		private final int index;
		
		public Char(int index) { this.index=index;}
		public int getIndex() { return index;}
		public boolean isFirst() { return index==0;}
		public boolean isLast() { return index+1>=JSString.this.length();}
		
		public Character getCharacter() {
			return JSString.this.charAt(index);
			}
		
		@Override
		public String toString() {
			return getCharacter().toString();
			}
		}

	public class CharIterator implements Iterator<Char>
		{
		private int index=-1;
		@Override
		public boolean hasNext() { return index+1<JSString.this.length();}
		@Override
		public Char next() { index++; return new Char(index);}
		}
	
	}
