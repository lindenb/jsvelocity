package com.github.lindenb.jsvelocity.json;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JSMap extends AbstractMap<String,JSNode>
	implements JSNode{
	private final JSNode parent;
	private final Map<String,JSNode> map ;
	private final String id = "a" + JSUtils.nextId();
	JSMap(final JSNode parent,final JsonObject object) {
		this.parent = parent;
		this.map = object.entrySet().stream().
				collect(Collectors.toMap(
						E-> E.getKey(),
						E-> JSUtils.convert(this, E.getValue())
						))				
				;
		}
	
	@Override
	public final boolean isObject() { return true;}
	
	@Override
	public Object getNodeValue() {
		return this.map;
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
	public int size() {
		return this.map.size();
	}
	
	@Override
	public JSNode remove(Object key) {
		throw new UnsupportedOperationException("Cannot remove");
	}
	
	@Override
	public Set<java.util.Map.Entry<String, JSNode>> entrySet() {
		return this.map.entrySet();
	}

}
