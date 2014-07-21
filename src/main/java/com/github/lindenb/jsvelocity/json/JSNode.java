package com.github.lindenb.jsvelocity.json;

import java.util.List;

public interface JSNode
	{
	public boolean isArray();
	public boolean isObject();
	public boolean isNumber();
	public boolean isString();
	public boolean isBoolean();
	public boolean isNull();
	public boolean isTrue();
	public boolean isFalse();
	public boolean isDecimal();
	public boolean isInteger();
	public boolean isComplex();
	public boolean isBigDecimal();
	public boolean isBigInteger();
	public abstract Object getNodeValue();
	public String getNodeId();
	public JSNode findById(String s);
	public JSNode getParentNode();
	public void setParentNode(JSNode p);
	public String getNodePath();
	public List<JSNode> eval(String path);
	public JSNode getNodeRoot();
	}
