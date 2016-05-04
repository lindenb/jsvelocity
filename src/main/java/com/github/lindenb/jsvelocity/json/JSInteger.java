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
	

}
