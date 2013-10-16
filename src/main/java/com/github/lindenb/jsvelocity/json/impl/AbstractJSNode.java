package com.github.lindenb.jsvelocity.json.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.lindenb.jsvelocity.json.JSArray;
import com.github.lindenb.jsvelocity.json.JSNode;
import com.github.lindenb.jsvelocity.json.JSObject;
import com.github.lindenb.jsvelocity.json.expr.JSONExprParser;
import com.github.lindenb.jsvelocity.json.expr.ParseException;

public abstract class AbstractJSNode
	implements JSNode
	{
	private static long ID_GENERATOR=0L;
	
	private JSNode parent=null;
	private long _id=0L;
	protected AbstractJSNode()
		{
		this._id=(++ID_GENERATOR);
		}
	
	@Override
	public void setParentNode(JSNode parent)
		{
		this.parent = parent;
		}
	
	@Override
	public JSNode getParentNode()
		{
		return parent;
		}
	
	@Override
	public boolean isArray() { return false;}
	@Override
	public boolean isObject() { return false;}
	@Override
	public boolean isNumber() { return false;}
	@Override
	public boolean isString() { return false;}
	@Override
	public boolean isBoolean() { return false;}
	@Override
	public boolean isNull() { return false;}
	@Override
	public boolean isTrue() { return false;}
	@Override
	public boolean isFalse() { return false;}
	@Override
	public boolean isDecimal() { return false;}
	@Override
	public boolean isInteger() { return false;}
	@Override
	public final boolean isComplex() { return isArray() || isObject();}
	@Override
	public abstract Object getValue();
	
	
	
	protected long id()
		{
		return _id;
		}

	@Override
	public final String getId()
		{
		return "id"+id();
		}
	
	
	public final String getPath()
		{
		JSNode p=getParentNode();
		if(p==null) return "";
		if(p.isArray())
			{
			JSArray container=(JSArray)p;
			for(int i=0;i<container.size();++i)
				{
				if(container.get(i)==this) return p.getPath()+"["+i+"]";
				}
			throw new IllegalStateException();
			}
		else if( p.isObject())
			{
			JSObject container=(JSObject)p;
			for(String key:container.keySet())
				{
				if(container.get(key)==this)
					{
					String pp=p.getPath();
					return (pp.isEmpty()?"":"/")+key;
					}
				}
			
			}
		return "";
		}
	
	@Override
	public List<JSNode> eval(String path)
		{
		try
			{
			StringReader ss=new StringReader(path);
			JSONExprParser.ExprNode expr=new JSONExprParser(ss).input();
			ss.close();
			List<JSNode> list=new ArrayList<JSNode>();
			expr.eval(this, list);
			return list;
			}
		catch (ParseException e)
			{
			e.printStackTrace();
			return Collections.emptyList();
			}
		}
	
	@Override
	public final JSNode getRoot()
		{
		JSNode curr=this;
		while(curr.getParentNode()!=null)
			{
			curr=curr.getParentNode();
			}
		return curr;
		}
	
	
	@Override
	public JSNode findById(String s)
		  {
		  if(s==null) return null;
		  if(getId().equals(s)) return this;
		  return null;
		  }
	@Override
	public String toString()
		{
		return String.valueOf(getValue());
		}
	}
