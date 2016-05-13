package com.github.lindenb.jsvelocity.json;

import java.io.IOException;
import java.math.BigDecimal;

import com.google.gson.stream.JsonWriter;


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
	public final boolean isBigDecimal() { return true;}
	
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
	
	
	public final double doubleValue() {
	return value.doubleValue();
	}
	
	@Override
	public void write(final JsonWriter writer) throws IOException {
		writer.value(this.value);
	}
}
