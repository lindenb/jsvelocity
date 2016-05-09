package com.github.lindenb.jsvelocity.json;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;

public class JSArray extends AbstractList<JSNode>
	implements JSNode, List<JSNode>{
	private final JSNode parent;
	private final List<JSNode> array ;
	private final String id = "a" + JSUtils.nextId();
	JSArray(final JSNode parent,final JsonArray garray) {
		this.parent = parent;
		this.array = StreamSupport.stream(garray.spliterator(), false).
				map(E->JSUtils.convert(this,E)).collect(
				Collectors.toList()
				);
		}
	
	@Override
	public final boolean isArray() { return true;}
	
	@Override
	public Object getNodeValue() {
		return array;
	}

	@Override
	public String getNodeId() {
		return id;
	}


	@Override
	public JSNode getParentNode() {
		return this.parent;
	}

	
	@Override
	public JSNode get(int index) {
		return this.array.get(index);
	}
	@Override
	public int size() {
		return this.array.size();
	}

}
