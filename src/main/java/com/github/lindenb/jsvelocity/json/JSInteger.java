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

import java.math.BigInteger;


public class JSInteger  implements JSNode {
	private final JSNode parent;
	private BigInteger value;
	private final String str;
	private final String id = "d" + JSUtils.nextId();
	JSInteger(final JSNode parent,final String str,final BigInteger value) {
		this.parent = parent;
		this.str = str;
		this.value=value;
		}
	
	@Override
	public final boolean isBigInteger() { return true;}
	
	@Override
	public Object getNodeValue() {
		return value;
	}

	@Override
	public String getNodeId() {
		return id;
	}


	@Override
	public JSNode getParentNode() {
		return this.parent;
	}
	
	public String toString() {
		return str;
		}
	
	@Override
	public final boolean isBigInteger() {
	return true;
	}
	
	
	public boolean isInt() {
	try { value.intValueExact(); return true;}
	catch(java.lang.ArithmeticException err) {return false;}
	}
	
	public final int intValue() {
	return value.intValueExact();
	}
	public final long longValue() {
	return value.longValueExact();
	}
}
