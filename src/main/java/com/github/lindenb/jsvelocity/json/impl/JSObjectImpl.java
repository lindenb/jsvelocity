package com.github.lindenb.jsvelocity.json.impl;

import com.github.lindenb.jsvelocity.json.JSNode;
import com.github.lindenb.jsvelocity.json.JSObject;

public class JSObjectImpl
	extends AbstractJSNode
	implements JSObject
	{
	private java.util.Map<String,JSNode> map=new java.util.LinkedHashMap<String,JSNode>();

	@Override
	public final boolean isObject() { return true;}

	@Override public Object getValue()
		{
		return this.map;
		}
	
	@Override  public  int size(){ return this.map.size();}
	  @Override public  boolean isEmpty(){ return this.map.isEmpty();}
	  @Override  public  boolean containsKey(java.lang.Object o){ return this.map.containsKey(o);}
	  @Override public  boolean containsValue(java.lang.Object o){ return this.map.containsValue(o);}
	  @Override public  JSNode get(java.lang.Object k){ return this.map.get(k);}
	  @Override public  JSNode put(String k, JSNode v){ return this.map.put(k,v);}
	  @Override public  JSNode remove(java.lang.Object o){ return this.map.remove(o);}
	  @Override public void putAll(java.util.Map<? extends String,? extends JSNode> c) {  this.map.putAll(c);}
	  @Override public  void clear(){  this.map.clear();}
	  @Override public  java.util.Set<String> keySet(){ return this.map.keySet();}
	  @Override public  java.util.Collection<JSNode> values(){ return this.map.values();}
	  @Override public  java.util.Set<java.util.Map.Entry<String,JSNode>> entrySet(){ return this.map.entrySet();}
	  @Override public  boolean equals(java.lang.Object o){ return o!=null && o.getClass()==this.getClass() && this.map.equals(((JSObjectImpl)o).map);}
	  @Override public  int hashCode(){ return this.map.hashCode();}

	  
	  @Override
	  public JSNode findById(String s)
		  {
		  if(s==null) return null;
		  if(getId().equals(s)) return this;
		  for(String k:this.keySet())
			  {
			  JSNode c=this.get(k);
			  JSNode r=c.findById(s);
			  if(r!=null) return r;
			  }
		  return null;
		  }
	  
	}
