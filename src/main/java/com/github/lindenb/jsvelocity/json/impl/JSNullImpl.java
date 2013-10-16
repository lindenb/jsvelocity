package com.github.lindenb.jsvelocity.json.impl;

import com.github.lindenb.jsvelocity.json.JSNull;

public class JSNullImpl
	extends AbstractJSNode
	implements JSNull

	{
	@Override
	public final Object getValue()
		{
		return null;
		}
	@Override
	public final boolean isNull() { return true;}
	@Override
	public final String toString()
		{
		return "";
		}
	}
