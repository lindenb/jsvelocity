package com.github.lindenb.jsvelocity.json.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.github.lindenb.jsvelocity.json.JSPrimitive;

public class JSPrimitiveImpl
	extends AbstractJSNode
	implements JSPrimitive
	{
	private Object value;
	public JSPrimitiveImpl(Object value)
		{
		this.value=value;
		}
	
	@Override public boolean isNumber() { return value instanceof Number;}
	@Override public boolean isString() { return value instanceof String;}
	@Override public boolean isBoolean() {  return value instanceof Boolean;}
	@Override public boolean isTrue() { return isBoolean() && value==Boolean.TRUE;}
	@Override public boolean isFalse() { return isBoolean() && value==Boolean.FALSE;}
	@Override
	public boolean isBigDecimal() {
		return value instanceof BigDecimal;
		}
	@Override
	public boolean isBigInteger() {
		return value instanceof BigInteger;
		}

	
	public Object getValue()
		{
		return value;
		}
	

	

	}
