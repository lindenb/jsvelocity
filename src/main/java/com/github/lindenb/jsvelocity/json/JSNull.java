package com.github.lindenb.jsvelocity.json;


public class JSNull  implements JSNode{
	private final JSNode parent;
	private final String id = "n" + JSUtils.nextId();
	JSNull(final JSNode parent) {
		this.parent = parent;
		}
	
	@Override
	public final boolean isNull() { return true;}
	
	@Override
	public Object getNodeValue() {
		return null;
	}

	@Override
	public String getNodeId() {
		return id;
	}


	@Override
	public JSNode getParentNode() {
		return this.parent;
	}
	
	public String toString()
		{
		return "";
		}

}
