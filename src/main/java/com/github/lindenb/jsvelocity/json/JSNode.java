package com.github.lindenb.jsvelocity.json;

public interface JSNode {
		public default boolean isArray() { return false;}
	    public default boolean isObject() { return false;}
	    public default boolean isNumber() { return false;}
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
}
