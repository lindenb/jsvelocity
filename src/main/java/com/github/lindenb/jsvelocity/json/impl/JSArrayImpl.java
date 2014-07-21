package com.github.lindenb.jsvelocity.json.impl;

import com.github.lindenb.jsvelocity.json.JSNode;
import com.github.lindenb.jsvelocity.json.JSArray;

public class JSArrayImpl
	extends AbstractJSNode
	implements JSArray
	{
	private java.util.List<JSNode> array=new java.util.ArrayList<JSNode>();
	@Override
	public boolean isArray() { return true;}
	
	@Override
	public Object getNodeValue()
		{
		return this.array;
		}
	
	  @Override public  int size(){ return this.array.size();}
	  @Override public  boolean isEmpty(){ return this.array.isEmpty();}
	  @Override public  boolean contains(java.lang.Object o){ return this.array.contains(o);}
	  @Override public  java.util.Iterator<JSNode> iterator(){ return this.array.iterator();}
	  @Override public  java.lang.Object[] toArray(){ return this.array.toArray();}
	  @SuppressWarnings("hiding")
	  @Override public  <JSNode> JSNode[] toArray(JSNode a[]){ return this.array.toArray(a);}
	  @Override public  boolean add(JSNode n){ return  this.array.add(n);}
	  @Override public  boolean remove(java.lang.Object o){ return this.array.remove(o);}
	  @Override public  boolean containsAll(java.util.Collection<?> c){ return this.array.containsAll(c);}
	  @Override public  boolean addAll(java.util.Collection<? extends JSNode> c){ return this.array.addAll(c);}
	  @Override public  boolean addAll(int i, java.util.Collection<? extends JSNode> c) { return this.array.addAll(i,c);}
	  @Override public  boolean removeAll(java.util.Collection<?> c){ return this.array.removeAll(c);}
	  @Override public  boolean retainAll(java.util.Collection<?> c){ return this.array.retainAll(c);}
	  @Override public  void clear(){ this.array.clear();}
	  @Override public  boolean equals(java.lang.Object o)
		  	{
		  	return o!=null && o.getClass()==this.getClass() && this.array.equals(((JSArrayImpl)o).array);
		  	}
	  @Override public  int hashCode(){ return this.array.hashCode();}
	  @Override public  JSNode get(int i){ return this.array.get(i);}
	  @Override public  JSNode set(int i,JSNode E){ return this.array.set(i,E);}
	  @Override public  void add(int i,JSNode E){  this.array.add(i,E);}
	  @Override public  JSNode remove(int i){ return this.array.remove(i);}
	  @Override public  int indexOf(java.lang.Object o){ return this.array.indexOf(o);}
	  @Override public  int lastIndexOf(java.lang.Object o){ return this.array.lastIndexOf(o);}
	  @Override public  java.util.ListIterator<JSNode> listIterator(){ return this.array.listIterator();}
	  @Override public  java.util.ListIterator<JSNode> listIterator(int i){ return this.array.listIterator(i);}
	  @Override public  java.util.List<JSNode> subList(int a, int b){ return this.array.subList(a,b);}
	  
	  @Override 
	  public JSNode findById(String s)
		  {
		  if(s==null) return null;
		  if(getNodeId().equals(s)) return this;
		  for(JSNode c:this)
			  {
			  JSNode r=c.findById(s);
			  if(r!=null) return r;
			  }
		  return null;
		  }
	}
