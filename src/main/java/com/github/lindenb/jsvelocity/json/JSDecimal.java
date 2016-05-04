package com.github.lindenb.jsvelocity.json;

import java.math.BigDecimal;


public class JSDecimal  implements JSNode {
	private final JSNode parent;
	private BigDecimal value;
	private final String str;
	private final String id = "f" + JSUtils.nextId();
	JSDecimal(final JSNode parent,final String str,final BigDecimal value) {
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
