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
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * JSVelocity
 * @author Pierre Lindenbaum
 *
 */
public class JSVelocity
	{
	private static final Logger LOG=LogManager.getLogger(JSVelocity.class);
	private VelocityContext context=new VelocityContext();
	
	
	
	
	public class MultiWriter
		extends Writer
		{
		private PrintWriter pw=null;
		public File filename=null;
		
		public File getFile()
			{
			return this.filename;
			}
		
		public void _open(String fName,boolean ignoreIfExists)throws IOException
			{
			close();
			
			this.filename=new File(JSVelocity.this.outDir,fName);
			if(ignoreIfExists && this.filename.exists())
					{
					return;
					}
			LOG.info("opening "+this.filename);
			if(this.filename.getParentFile()!=null)
				{
				this.filename.getParentFile().mkdirs();
				}
			this.pw=new PrintWriter(this.filename);
			}
		
		
		public void open(final String fName)throws IOException
			{
			_open(fName,false);
			}
			
		public void openIfMissing(final String fName)throws IOException
			{
			_open(fName,true);
			}
		
		@Override
		public void close() throws IOException
			{
			flush();
			if(pw!=null)
				{
				LOG.info("closing "+this.filename);
				pw.close();
				pw=null;	
				}
			filename=null;
			}
	
		@Override
		public void flush() throws IOException
			{
			if(pw!=null)
				{
				pw.flush();
				}
			else if(this.filename==null)
				{
				System.out.flush();
				}
			}
	
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException
			{
			if(pw!=null)
				{
				pw.write(cbuf, off, len);		
				}
			else if(this.filename==null)
				{
				System.out.print(new String(cbuf, off, len));
				}
			}
		@Override
		public String toString()
			{
			return filename==null?"null":filename.toString();
			}
		}
	
	
	private void put(final String key,final Object o)
		{
		LOG.info("adding key="+key+" as "+(o==null?"null object":o.getClass().getName()));
		if(context.containsKey(key))
			{
			LOG.warn("Key="+key+" defined twice");
			}
		context.put(key,o);
		}
	@Parameter(names= {"-h","--help"},description="Show Help",help=true)
	private boolean showHelp=false;
	@Parameter(description = "Files")
	private List<String> files=new ArrayList<>();
	@Parameter(names= {"-o","--output","--directory"},description = "Output directory")
	private File outDir=null;

	@Parameter(names= {"-C","--class"},arity=2,description = "Add this java Class into the context..")
	private List<String> inputJavaClasses = new ArrayList<>();
	@Parameter(names= {"-c","--instance"},arity=2,description = "Add this java instance into the context..")
	private List<String> inputJavaInstances = new ArrayList<>();
	@Parameter(names= {"-s","--string"},arity=2,description = "Add this String into the context..")
	private List<String> inputStrings= new ArrayList<>();
	@Parameter(names= {"-e","--expr"},arity=2,description = "Add this JSON-Expression into the context..")
	private List<String> inputJsonExpr= new ArrayList<>();
	@Parameter(names= {"-f","--json"},arity=2,description = "Add this JSON-File into the context..")
	private List<String> inputJsonFiles= new ArrayList<>();
	@Parameter(names= {"-gson","--gson"},description = "Do not convert json object to java. Keep the com.google.gson.* objects")
	private boolean keep_gson = false;
	@Parameter(names= {"-tsv","--tsv"},arity=2,description = "Read tab delimited table in file.")
	private List<String> inputTsvFiles= new ArrayList<>();
	@Parameter(names= {"-lenient","--lenient"},description = "Use a lenient json parser")
	private boolean lenient_json_parser = false;

	
	private static void close(Closeable c) {
		if(c==null) return;
		try {
			c.close();
		} catch(Exception err) {
			
		}
		}
	
	private List<List<String>> readDelim(String path,final Pattern delim) {
		BufferedReader r= null;
		try {
			r= new BufferedReader(new FileReader(path));
			return r.lines().map(L->Arrays.asList(delim.split(L))).collect(Collectors.toList());
			} 
		catch(final IOException err) {
			throw new RuntimeException(err);
			}
		finally {
			close(r);
			}
		}
	
	private List<Map<String,String>> readTable(String path,final Pattern delim) {
		BufferedReader r= null;
		try {
			r= new BufferedReader(new FileReader(path));
			final String first=r.readLine();
			if(first==null) throw new IOException("Cannot read first line of "+path);
			final String header[]=delim.split(first);
			return r.lines().map(L->{
				final String tokens[]=delim.split(L);
				final Map<String,String> map = new LinkedHashMap<>(header.length);
				for(int i=0;i< tokens.length && i< header.length;i++)
					{
					map.put(header[i], tokens[i]);
					}
				for(int i= tokens.length;i< header.length;i++)
					{
					map.put(header[i],"");
					}
				return map;
				}).collect(Collectors.toList());
			} 
		catch(final IOException err) {
			throw new RuntimeException(err);
			}
		finally {
			close(r);
			}
		}

	
	private Object convertJson(final JsonElement  E)
		{	
		if(this.keep_gson) return E;
		return json2java(E);
		}
	
	private Object json2java(final JsonElement  E){
		if(E.isJsonNull()) {
			return null;
			}
		else if(E.isJsonArray())
			{
			final JsonArray o = E.getAsJsonArray();
			final List<Object> L = new ArrayList<>(o.size());
			for(int i=0;i< o.size();i++)
				{
				L.add(json2java(o.get(i)));
				}
			return L;
			}
		else if(E.isJsonObject())
			{
			final JsonObject o = E.getAsJsonObject();
			final Map<String,Object> M = new LinkedHashMap<>();
			o.entrySet().stream().forEach(KV->M.put(KV.getKey(), json2java(KV.getValue())));
			return M;
			}
		else if(E.isJsonPrimitive())
			{
			final JsonPrimitive prim = E.getAsJsonPrimitive();
			if(prim.isString()) return prim.getAsString();
			if(prim.isBoolean()) return prim.getAsBoolean();
			if(prim.isNumber()) {
				try {return new Integer(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new Long(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new BigInteger(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new Double(prim.getAsString());} catch(NumberFormatException e) {}
				try {return new BigDecimal(prim.getAsString());} catch(NumberFormatException e) {}
				}
			}
		throw new IllegalStateException("Cannot convert "+E);
		};
	
	private <T>  List<AbstractMap.SimpleEntry<String,T>> mapKeyValues(
		final List<String> array,
		final Function<String,T> mapper)
		{
		final List<AbstractMap.SimpleEntry<String,T>> L = new ArrayList<>(array.size()/2);
		for(int i=0;i+1<array.size();i+=2)
			{
			System.err.println(">>"+array.get(i+1)+"\n"+array);
			L.add(new AbstractMap.SimpleEntry<String,T>(
					array.get(i),
					mapper.apply(array.get(i+1))
					));
			}
		return L;
		}
	
	private JsonElement parseJson(final Reader r) {
		JsonParser parser= new JsonParser();
		JsonReader jr = new JsonReader(r);
		if(this.lenient_json_parser) jr.setLenient(true);
		final JsonElement E= parser.parse(jr);
		return E;
		}
	

	
	private void run(final String args[]) throws Exception
		{ 
		final JCommander jcommander= new JCommander(this);
		jcommander.setProgramName("jsvelocity");
		jcommander.parse(args);
		if(this.showHelp)
			{
			jcommander.usage();
			return;
			}
		
		   
		
		if(this.files.size()!=1)
			{
			LOG.error("Illegal number of arguments. Expected one Velocity Template.\n");
			System.exit(-1);
			}
		
		
		this.mapKeyValues(this.inputJavaClasses,className->{
			try
				{
				return Class.forName(className);
				}
			catch(final Exception err)
				{
				LOG.error("Cannot load class "+className);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));
		
		this.mapKeyValues(this.inputJavaInstances,className->{
			try
				{
				return Class.forName(className).newInstance();
				}
			catch(final Exception err)
				{
				LOG.error("Cannot load new instance of class "+className);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));
		
		this.mapKeyValues(this.inputStrings,value->value).
			forEach(KV->put(KV.getKey(),KV.getValue()));

		this.mapKeyValues(this.inputJsonExpr,value->{
			try 
				{
				System.err.println(value);
				return  convertJson(parseJson(new StringReader(value)));
				}
			catch(Exception err)
				{
				LOG.error("Cannot parse expression "+value);
				System.exit(-1);
				return null;
				}
			
			}).
			forEach(KV->put(KV.getKey(),KV.getValue()));

		this.mapKeyValues(this.inputJsonFiles,value->{
			try
			{
			final FileReader r=new FileReader(value);
			final Object o= convertJson(parseJson(r));
			r.close();
			return o;
			} catch(final IOException err)
				{
				LOG.error("Cannot read file "+value);
				System.exit(-1);
				return null;
				}
			}).forEach(KV->put(KV.getKey(),KV.getValue()));

		
		
		final MultiWriter out=new MultiWriter();
		put("out",out);
		put("tool",new Tools());
		put("now",new java.sql.Timestamp(System.currentTimeMillis()));
		final File file = new File(this.files.get(0));
		LOG.info("Reading VELOCITY template from file "+file);
		final VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loader", "file");
		ve.setProperty("file.resource.loader.description", "Velocity File Resource Loader");
		ve.setProperty("file.resource.loader.class","org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		if(file.getParent()!=null)
			{	
			ve.setProperty("file.resource.loader.path",file.getParent());		
			}
		ve.init();
		final Template template = ve.getTemplate(file.getName());
		template.merge( this.context, out);
		out.flush();
		out.close();
	    }
	
	
	public static void main(final String args[])
		{
		try {
			new JSVelocity().run(args);
			}
		catch(Throwable err)
			{
			LOG.error(err);
			err.printStackTrace();
			System.exit(-1);
			}
		}
	
}
