package com.github.lindenb.jsvelocity.json;


public class JSString  implements JSNode,CharSequence {
	private final JSNode parent;
	private final String value;
	private final String id = "s" + JSUtils.nextId();
	JSString(final JSNode parent,final String s) {
		this.parent = parent;
		this.value =s;
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
		return value;
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

}
