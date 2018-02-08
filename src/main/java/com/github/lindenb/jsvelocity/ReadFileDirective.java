/*
The MIT License (MIT)

Copyright (c) 2018 Pierre Lindenbaum

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;


import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReadFileDirective extends Directive {
	private static final Logger LOG=LoggerFactory.getLogger(ReadFileDirective.class);

    public String getName() {
        return "readfile";
    }
    
    @Override
    public int getType() {
    	return Directive.LINE;
    	}
    @Override
    public boolean render(
    		final InternalContextAdapter ctx,final Writer w,final Node node) 
    		throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
    	LOG.debug("render "+getName()+" directive");
    	final List<Object> arguments = new ArrayList<>(node.jjtGetNumChildren());
    	
    	
        //loop through all "params"
        for(int i=0; i<node.jjtGetNumChildren(); i++) {
        	final Node child  = node.jjtGetChild(i);
        	arguments.add(child.value(ctx));
        	}
    final String varName = !arguments.isEmpty() && arguments.get(0)!=null?
    			String.valueOf(arguments.get(0)):
    			null;
    if(varName==null || varName.trim().isEmpty())
    	{
    	throw new ParseErrorException("No variable name specified for "+getName());
    	}    
        
    final String filename = arguments.size()>1 && arguments.get(1)!=null?
    			String.valueOf(arguments.get(1)):
    			null;
	final String modifiersStr = arguments.size()>2 && arguments.get(2)!=null?
			String.valueOf(arguments.get(2)):
			"";			
    
			
    if(filename==null || filename.trim().isEmpty())
    	{
    	throw new ParseErrorException("No filename name specified for "+getName());
    	}
    
    Predicate<String> lineFilter =(S)->!S.trim().isEmpty();
    int ignoreNLines=0;
    String method="list";
    Pattern delim = Pattern.compile("[\t]");
    String primaryKeyColumn= null;
    for(final String kv:modifiersStr.split("[;]"))
    	{
    	if(kv.trim().isEmpty()) continue;
    	int colon = kv.indexOf(":");
    	String key;
    	String value;
    	if(colon==-1) {
    		key=kv.trim().toLowerCase();
    		value="";
    		}
    	else
    		{
    		key=kv.substring(0,colon).trim().toLowerCase();
    		value=kv.substring(colon+1).trim();
    		}
    	
    	if(key.equals("delim") || key.equals("delimiter") || key.equals("sep") || key.equals("separator"))
    		{
    		if(value.equalsIgnoreCase("tab") || value.equals("\\t"))
    			{
    			delim = Pattern.compile("[\t]");
    			}
    		else if(value.equalsIgnoreCase("space"))
				{
				delim = Pattern.compile("[ ]");
				}
    		else if(value.equalsIgnoreCase("semicolon"))
				{
				delim = Pattern.compile("[;]");
				}
    		else if(value.equalsIgnoreCase("colon"))
				{
				delim = Pattern.compile("[\\:]");
				}
    		else if(value.equalsIgnoreCase("ws") || value.equals("whitespace"))
				{
				delim = Pattern.compile("[ \t]+");
				}
    		else if(value.equalsIgnoreCase(",") || value.equals("comma"))
				{
				delim = Pattern.compile("[,]");
				}
    		else
    			{
    			LOG.warn("unknown delim value "+kv);
    			}
    		}
    	else if(key.equals("skip")) {
    		ignoreNLines = Integer.parseInt(value);
    		if(ignoreNLines<0) throw new IllegalArgumentException("vad skip value :"+value);
    		}
    	else if(key.equals("pkey") || key.equals("primarykey")) {
    		primaryKeyColumn=value;
    		}
    	else if(key.equals("method")) {
    		method=value.trim().toLowerCase();
    		}
    	else
    		{
    		LOG.warn("unknown modifier key "+kv);
    		}
    	}
    
    
    final File file = new File(filename);
    if(!file.exists()) throw new ResourceNotFoundException("file doesn't exists: "+ file);
    final Reader fr = file.getName().endsWith(".gz")?
    		new InputStreamReader(new GZIPInputStream(new FileInputStream(file))):
    		new FileReader(file)
    		;
    final BufferedReader br = new BufferedReader(fr);
    final Object v;
    final Function<String, String> fixHeader = (S)->(S.startsWith("#")?S.substring(1):S);
    
    if(method.equalsIgnoreCase("table"))
    	{
    	final Pattern pat= delim;
    	v = br.lines().
    			skip(ignoreNLines).
    			filter(lineFilter).
    			map(S->Arrays.asList(pat.split(S))).
    			collect(Collectors.toCollection(ArrayList::new));
    	}
    else  if(method.equalsIgnoreCase("hashtable"))
		{
		final Pattern pat= delim;
		String line = br.readLine();
		if(line==null ) throw new IOException("Cannot find first line of "+file);
		final String header[]=pat.split(line);
		header[0]=fixHeader.apply(header[0]);
		v = br.lines().
			skip(ignoreNLines).
			filter(lineFilter).
			map(L->{
				final Map<String,String> row = new LinkedHashMap<>(header.length);
				final String tokens[]=pat.split(L);
				for(int x=0;x<header.length && x< tokens.length;++x)
					{
					row.put(header[x],tokens[x]);
					}
				for(int x=tokens.length;x<header.length;++x)
					{
					row.put(header[x],"");
					}
				return row;
				}).
			collect(Collectors.toCollection(ArrayList::new));
		}
    else  if(method.equalsIgnoreCase("hash"))
		{
    	if(primaryKeyColumn==null) throw new IllegalArgumentException("primary key column undefined");
		final Pattern pat= delim;
		String line = br.readLine();
		if(line==null ) throw new IOException("Cannot find first line of "+file);
		final String header[]=pat.split(line);
		header[0]=fixHeader.apply(header[0]);
		final int primaryKey0 = Arrays.asList(header).indexOf(primaryKeyColumn);
		if(primaryKey0<0)
			{
			throw new IllegalArgumentException("primary doesn't exists in "+Arrays.asList(header));
			}
		final Map<String,Map<String,String>> hash = new LinkedHashMap<>();
		v = hash;
		br.lines().
			skip(ignoreNLines).
			filter(lineFilter).
			forEach(L->{
				final Map<String,String> row = new LinkedHashMap<>(header.length);
				final String tokens[]=pat.split(L);
				if(primaryKey0>=tokens.length)
					{
					throw new IllegalArgumentException("primary key out of range (header contain "+header.length+" cols.)");
					}
				
				for(int x=0;x<header.length && x< tokens.length;++x)
					{
					row.put(header[x],tokens[x]);
					}
				for(int x=tokens.length;x<header.length;++x)
					{
					row.put(header[x],"");
					}
				if(hash.containsKey(tokens[primaryKey0])) {
					throw new RuntimeException("duplicate primary key in "+L);
					}
				hash.put(tokens[primaryKey0], row);
				});
		}
    else if(method.equalsIgnoreCase("list"))
    	{
    	v = br.lines().
			skip(ignoreNLines).
			filter(lineFilter).
			collect(Collectors.toCollection(ArrayList::new));
    	}
    else
    	{
    	throw new RuntimeException("unknown method "+method);
    	}
    
    br.close();
    fr.close();
    ctx.put(varName, v);
    return true;
    }
}
