package com.github.lindenb.jsvelocity.json;


public class JSBoolean  implements JSNode {
	private final JSNode parent;
	private final boolean value;
	private final String id = "b" + JSUtils.nextId();
	JSBoolean(final JSNode parent,final boolean b) {
		this.parent = parent;
		this.value =b;
		}
	
	@Override	
	public final boolean isBoolean() { return true;}
	@Override	
	public final boolean isTrue() { return value;}
	@Override	
	public final boolean isFalse() { return !value;}
	
	
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
		return String.valueOf(this.value);
		}


}
