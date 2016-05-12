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

public interface JSNode {
		public default boolean isArray() { return false;}
	    public default boolean isObject() { return false;}
	    public default boolean isNumber() { return isBigInteger() || isBigDecimal();}
	    public default boolean isString() { return false;}
	    public default boolean isBoolean() { return false;}
	    public default boolean isNull() { return false;}
	    public default boolean isTrue() { return false;}
	    public default boolean isFalse() { return false;}
	    public default boolean isDecimal() { return false;}
	    public default boolean isInteger() { return false;}
	    public default boolean isComplex() { return false;}
	    public default boolean isBigDecimal() { return false;}
	    public default boolean isBigInteger()  { return false;}
	    public default Object getNodeValue(){ return null;}
	    public default String getNodeId(){ return null;}
	    public default JSNode findById(String s) { return null;}
	    public default JSNode getParentNode() { return null;}
	    public default String getNodePath() { return null;}
	    public default JSNode getNodeRoot() {
	    	JSNode n=this;
	    	while(n.getParentNode()!=null) n=n.getParentNode();
	    	return n;
	    	}
	    	
	    	
		public default String toUpperCase() {
			return toString().toUpperCase();
			}
		public default boolean hasParentNode() {
			return getParentNode()!=null;
			}
		public default boolean isPrimitive() {
			return isString() || isNumber() || isBoolean();
			}
		}
