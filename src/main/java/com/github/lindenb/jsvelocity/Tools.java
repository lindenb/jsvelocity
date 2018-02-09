/*
The MIT License (MIT)

Copyright (c) 2016 Pierre Lindenbaum

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
package com.github.lindenb.jsvelocity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

public class Tools
	{
	private static final Logger LOG=LoggerFactory.getLogger(Tools.class);

	private InputStream openStream(Object o) throws Exception
		{
		LOG.info("open stream "+o);
		if(o==null) return null;
		String uri=o.toString().trim();
		if(uri.isEmpty()) return null;
		InputStream in;
		if( uri.startsWith("https://") ||
			uri.startsWith("http://") ||
			uri.startsWith("ftp://"))
			{
			URL url=new URL(uri);
			in=url.openStream();
			}
		else
			{
			if(uri.startsWith("file://")) uri=uri.substring(7);
			in=new FileInputStream(uri);
			}
		
		return in;
		}

	public String capitalize(final Object o) {
		return o== null ?
				"":
				WordUtils.capitalize(String.valueOf(o))
				;
	}
	
	public boolean isBlank(final Object o) {
		return o==null || StringUtils.isBlank(String.valueOf(o));
	}
	
	public String escapeCsv(final Object o) {
		return o== null ?
			"":
			StringEscapeUtils.escapeCsv(String.valueOf(o))
			;
		}

	public String escapeHtml(final Object o) {
		return o== null ?
			"":
			StringEscapeUtils.escapeHtml4(String.valueOf(o))
			;
		}
	
	public String escapeJava(final Object o) {
		return o== null ?
			"":
			StringEscapeUtils.escapeJava(String.valueOf(o))
			;
		}
	
	public String escapeJson(final Object o) {
		return o== null ?
			"":
			StringEscapeUtils.escapeJson(String.valueOf(o))
			;
		}
	
	public String escapeXml(final Object o) {
		return o== null ?
			"":
			StringEscapeUtils.escapeXml11(String.valueOf(o))
			;
		}
	
	private Function<Object,Object> fieldExtractor(final Class<?> clazz,final String field) {
		final Optional<Method> method;
		try {
			method = 
			Arrays.stream(clazz.getMethods()).
			filter(M->
				{
				if(!Modifier.isPublic(M.getModifiers())) return false;
				if(Modifier.isStatic(M.getModifiers())) return false;
				if(M.getParameterCount()!=0) return false;
				if(M.getReturnType().equals(Void.class)) return false;
				String methodName = M.getName();
				if(!(methodName.equalsIgnoreCase("is"+field) || methodName.equalsIgnoreCase("get"+field)))
					{
					return false;
					}
				return true;
				}).findFirst();
			
			if(!method.isPresent())
				{
				final Optional<Field> of=Arrays.stream(clazz.getFields()).
						filter(F->{
							if(!Modifier.isPublic(F.getModifiers())) return false;
							if(Modifier.isStatic(F.getModifiers())) return false;
							String fieldName = F.getName();
							return fieldName.equals(field);
							}
						).findFirst();
				if(of.isPresent())
					{
					final Field fieldReflect = of.get();
					return (OBJ)->{
						try {
							return fieldReflect.get(OBJ);
							}
						catch(Exception err)
							{
							throw new RuntimeException(err);
							}
						};
					}
				}
			
			if(!method.isPresent() && clazz.isAssignableFrom(Map.class))
				{
				return (OBJ)->{
					final Map hash = (Map)OBJ;
					return hash.get(field);
					};
				}
			final Method m2=method.get();
			
			return (OBJ)->{
				try {
					return m2.invoke(OBJ);
					}
				catch(Exception err) {
					throw new RuntimeException(err);
					}
				};
			}
		catch(final Exception err) {
			LOG.error(err.getMessage(),err);
			}
		
		throw new ResourceNotFoundException("Cannot find field \""+field+"\" for class "+clazz);
		}
	
	private Object extractField(final Object o,final String field) {
		return o==null?null:fieldExtractor(o.getClass(),field).apply(o);
	}
	
	public Collection<?> extract(Collection<?> C,final String field) {
		if(C==null || C.isEmpty()) return Collections.emptySet();
		return C.stream().map(O->extractField(O, field)).collect(Collectors.toList());
		}
	
	public Set toSet(Collection o) {
		if(o==null || o.isEmpty()) return Collections.emptySet();
		return new HashSet<>(o);
		}
	
	
public String getContent(Object o) throws Exception
	{
	InputStream in=openStream(o);
	if(in==null) return "";
	InputStreamReader r=new InputStreamReader(in);
	StringBuilder b=new StringBuilder();
	int c;
	while((c=r.read())!=-1) b.append((char)c);
	r.close();
	return b.toString();
	}
public Object getJSon(final Object o) throws Exception
	{
	final JsonParser parser = new JsonParser();
	InputStream in=openStream(o);
	if(in==null) return null;
	Object n=parser.parse(new InputStreamReader(in));
	in.close();
	return n;
	}

	
	
	@Override
	public String toString()
		{
		return "jsvelocity.tool";
		}
}
